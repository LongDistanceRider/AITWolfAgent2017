/**
 *
 *
 * @author Fukurou
 * @version 1.0
 */
package com.icloud.itfukui0922.player;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.icloud.itfukui0922.dictionary.Chat;
import com.icloud.itfukui0922.nlp.NatulalLanguageProcessing;
import com.icloud.itfukui0922.nlp.Response;
import com.icloud.itfukui0922.nlp.Role;
import com.icloud.itfukui0922.nlp.Species;
import com.icloud.itfukui0922.nlp.Topic;
import com.icloud.itfukui0922.strategy.AgentInfo;
import com.icloud.itfukui0922.strategy.BoardSurface;
import com.icloud.itfukui0922.strategy.SeerReport;

public class AITWolfPlayer implements Player {

	/* ログ */
	Logger logger;
	/* 解析用スレッド待機時間 */
	final static int THREAD_LIMIT_TIME = 3000;
	/* マルチスレッド */
	Queue<Future<Boolean>> futureQueue;
	/* NLPキュー */
	Queue<NatulalLanguageProcessing> nlpQueue;
	/* ゲーム情報 */
	GameInfo currentGameInfo;
	/* talkList読み込みヘッド */
	int talkListHead;
	/* 盤面状況 */
	BoardSurface boardSurface;
	/* 発言キュー */
	Queue<String> talkQueue;
	/* 挨拶したか */
	boolean isGreeting;
	/* フィニッシュしたか（finish()が２度呼ばれるみたいで，不都合があるので） */
	boolean isfinish;
	/* 雑談回数を記録 */
	int chatTimes = 0;
	/* 雑談回数制限（1日のうちに何回発言させるか */
	final static int CHAT_LIMIT = 3;

	/**
	 * 襲撃対象を返すメソッド 占い師COした人を優先的に狙う．いない場合は生存プレイヤーから適当に返す 狂人COしたら候補者から外す
	 */
	@Override
	public Agent attack() {
		logger.config("attack()");

		List<Agent> aliveAgent = currentGameInfo.getAliveAgentList(); // 生存者リスト（自分抜き）
		aliveAgent.remove(currentGameInfo.getAgent());

		// ----- 占い師COした人がいるのか特定する -----
		for (Agent agent : aliveAgent) { // 生存者リストを操作
			AgentInfo agentInfo = boardSurface.getAgentInfo(agent); // エージェント情報取得
			Role role = agentInfo.getRole(); // カミングアウトした役職を取得
			if (role.equals(Role.SEER)) { // カミングアウトした役職が占い師
				logger.fine("占い師に噛み付いた：" + agent);
				logger.config("==========");
				return agent; // 占い師にAttack
			}
		}

		// ----- 候補者がいないため，生存プレイヤーから適当にアタック -----
		Agent agent = randomSelect(aliveAgent);
		logger.info("適当な人に噛み付いた" + agent);
		logger.config("==========");
		return agent; // 適当なプレイヤにアタック
	}

	/**
	 * 1日の始まりに呼び出されるメソッド 占い師は占い結果の読み込みをおこなう
	 */
	@Override
	public void dayStart() {
		logger.config("dayStart()");
		// ----- 1日目の処理（盤面初期化） -----
		if (currentGameInfo.getDay() == 1) {
			Map<Agent, AgentInfo> agentInfoMap = new HashMap<>(); // エージェント情報を保管するマップを作成
			for (Agent agent : currentGameInfo.getAgentList()) { // 全てのエージェントに対してエージェント情報インスタンス生成
				AgentInfo agentInfo = new AgentInfo(agent);
				agentInfoMap.put(agent, agentInfo); // Map保管
			}
			boardSurface = BoardSurface.getInstance(currentGameInfo, agentInfoMap); // 盤面情報生成（シングルトン）
		}

		// ----- 2日目の処理 -----
		if (currentGameInfo.getDay() == 2) {
			// 追放した人にお疲れ様
			Agent executedAgent = currentGameInfo.getExecutedAgent();
			talkQueue.offer(executedAgent + "さんお疲れ様ー");
			// 襲撃された人に追悼メッセ
			Agent attackedAgent = attackedAgent();
			if (attackedAgent != null) {
				talkQueue.offer(attackedAgent + "がいない！狼に食べられちゃったのかな");

			} else {
				talkQueue.offer("あれ？誰か居なくなった？"); // 襲撃された人がわかんない時（この文が発言されたらattackedAgent()が機能してない）
			}
			// 襲撃された人が黒判定受けて居たら，黒判定していた占い師は狂人
			if (attackedAgent != null) {
				if (boardSurface.getAgentInfo(attackedAgent).getTrustly() == Float.MAX_VALUE) { // 黒判定受けたたら
					Map<Agent, SeerReport> seerReportMap = boardSurface.getSeerReportMap();
					for (Agent key : seerReportMap.keySet()) {
						SeerReport seerReport = seerReportMap.get(key);
						if (seerReport.publishedBlackAgent() != null) {
							// 黒判定発見
							talkQueue.offer("襲撃された人は黒判定受けてたわけだから，黒判定してた" + key + "は狂人だね！");
						}
					}
				}
			}
		}

		// ----- 各役職の処理 -----
		switch (currentGameInfo.getRole()) {
		case SEER: // 占い結果の取り込みと発話生成
			Judge divined = currentGameInfo.getDivineResult(); // 占い結果取得
			if (divined != null) { // 0日目対策（0日目はnullが帰って来るので）（あとは予期しないnull返し食らった時のため）
				Agent target = divined.getTarget(); // 占い先取得
				Species result = Species.getSpecies(divined.getResult().toString()); // 結果取得（自Speciesと源Speciesとの変換）
				AgentInfo agentInfo = boardSurface.getAgentInfo(target); // エージェント情報取得
				agentInfo.setTrustly(result == Species.HUMAN ? Float.MAX_VALUE : Float.MIN_VALUE); // 白判定なら信用度を1に，黒判定なら信用度を0に設定
				logger.fine("占い対象者：" + target + "結果：" + result);

				// 占い結果の発言
				String resultString = (result == Species.HUMAN ? "人間" : "人狼");

				String talk = target + "は" + resultString + "でした！"; // 発言文
				if (currentGameInfo.getDay() == 1) {
					talkQueue.offer("私が占い師です！"); // comingout
					boardSurface.setIsCO(true);
				}
				talkQueue.offer(talk); // 発言キューに追加
				if (result == Species.WEREWOLF) {
					talkQueue.offer("人狼を見つけ出したよ！今晩は" + target + "に投票して！"); // 人狼を見つけた時だけ，投票要求を行う
				}
			}
			// 占い師2日目の発言
			if (currentGameInfo.getDay() == 2) {
				talkQueue.offer("占い師なのに生き残った！これで勝てる！");
			}
			break;
		case POSSESSED: // 偽占い結果報告の発話生成
			// ----- 1日目処理 -----
			if (currentGameInfo.getDay() == 1 && !boardSurface.getIsCO()
					&& boardSurface.getMyRole() == Role.POSSESSED) { // 狂人の場合は偽占CO
				boardSurface.setIsCO(true);
				talkQueue.offer("私が占い師です！"); // comingout

				List<Agent> aliveAgentList = currentGameInfo.getAliveAgentList(); // 生存者リスト取得
				aliveAgentList.remove(currentGameInfo.getAgent()); // 自分自身は除外
				Agent agent = randomSelect(aliveAgentList); // リストからランダム選択
				talkQueue.offer(agent + "は白でした！"); // 偽報告
				logger.fine("偽占い対象者：" + agent);

				// 偽報告したAgentの信頼度を変更する
				AgentInfo agentInfo = boardSurface.getAgentInfo(agent);
				agentInfo.setTrustly(Float.MAX_VALUE); // 信用度を白確定としておく
			}
			// ----- 2日目処理 -----
			if (currentGameInfo.getDay() == 2) {
				if (currentGameInfo.getRole().equals(org.aiwolf.common.data.Role.POSSESSED)) { // 狂人の場合
					// 灰色から偽占い結果報告をする
					Agent agent = randomSelect(returnGrayList()); // 自分以外の灰色リストの中からランダムに選択
					if (agent != null) {
						logger.fine("偽占い対象者：" + agent);
						talkQueue.offer(agent + "は白でした！"); // 偽報告
					}
				}
				// ２日目生存してるなら狂人COでいい気がするので，狂人COします
				talkQueue.offer("今までのは演技だったのさ！自分が狂人ですよ。ご主人");
			}

			break;
		case VILLAGER:// 役職ないですよアピールと適当な発言をかます
			if (currentGameInfo.getDay() == 1) {
				talkQueue.offer("自分はなんの役職もないですが、頑張って参加しますね");
			} else if (currentGameInfo.getDay() == 2) {
				talkQueue.offer("やばいな、どうしよう。生き残っちゃったよ");
			}
			break;
		case WEREWOLF: // どうせ解析されないのでドッキリな発言をかます
			if (currentGameInfo.getDay() == 1) {
				talkQueue.offer("どれも美味しそうだ。あ、人間は食べないよ！");
			} else if (currentGameInfo.getDay() == 2) {
				talkQueue.offer("今晩も美味しいディナーが待ってる");
			}
		default:
			break;
		}
		// ----- talkListHeadのリセット -----
		talkListHead = 0;
		// ----- chatTimesのリセット -----
		chatTimes = 0;
		logger.config("==========");
	}

	/**
	 * 占いたいAgentを返す 信用度情報から灰色（0より大きく1より小さい）プレイヤを候補に加え，候補からランダムに決定する． next:
	 * 占い希望を聞いて，その対象を占う
	 */
	@Override
	public Agent divine() {
		logger.config("divine()");
		// ----- 0日目処理 -----
		if (currentGameInfo.getDay() == 0) {
			return null;
		}
		// 灰色から対象者を占う
		Agent agent = randomSelect(returnGrayList());
		if (agent == null) {
			return null;
		} else {
			logger.fine("占い対象者：" + agent);
			logger.config("==========");
			return agent;
		}
	}

	@Override
	public void finish() {
		logger.config("finish()");

		if (!isfinish) {

			// BoardSurfaceReport
			boardSurface.report();

			// 利用中のスレッドを全開放
			futureQueue.clear();
			nlpQueue.clear();
			currentGameInfo = null;
			talkQueue = null;
			boardSurface = null;
			Runtime runtime = Runtime.getRuntime(); // GC
			runtime.gc(); // GC
			logger.info("システムメモリの空バイト数 : " + runtime.freeMemory());
			isfinish = true;
		}
		logger.config("==========");
	}

	/**
	 * プレイヤ名を返すだけ
	 */
	@Override
	public String getName() {
		logger.config("getName()");
		logger.config("==========");
		return "AITWolf";
	}

	/**
	 * このメソッドは使われない（未実装）
	 */
	@Override
	public Agent guard() {
		logger.config("guard()");
		logger.config("==========");
		return null;
	}

	/**
	 * 初期化
	 */
	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		// ## ログ設定 ##
		logger = Logger.getLogger("main" + gameInfo.getAgent());
		try {
			Date date = new Date();
			String nowTime = date.toString();
			String logFileName = "log/" + nowTime + gameInfo.getAgent() + "LoggerLog.txt";
			FileHandler fileHandler = new FileHandler(logFileName, false);
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);
			logger.setLevel(Level.SEVERE);
		} catch (SecurityException | IOException e) {
			System.err.println("ログ設定でエラー");
			e.printStackTrace();
		}

		logger.config("initialize()"); // ログファイルに出力されない（理由不明）
		// -----
		// フィールドの初期化（boardSurfaceはdayStart()で1日目に初期化）（executorServiceは毎回update時に初期化される）
		// -----
		nlpQueue = new ArrayDeque<>();
		futureQueue = new ArrayDeque<>();
		currentGameInfo = gameInfo;
		talkListHead = 0;
		talkQueue = new ArrayDeque<>();
		isGreeting = false;
		isfinish = false;
		logger.config("==========");
	}

	/**
	 * talk()は次の3つに分割される 1. 解析 時間がかかるためにtalk()に処理を委託しているものを処理 発言することがあればキューに入れて３．へ
	 * 3. 発言 キューにあるものを順々に発言する
	 */
	@Override
	public String talk() {
		double start = System.nanoTime();
		System.out.println("-----=====-- TALK --=====-----");
		logger.config("talk()");
		// ----- 0日目処理 -----
		if (currentGameInfo.getDay() == 0) {
			if (isGreeting) {
				return Talk.OVER; // 挨拶し終わったのでOVERを返す
			} else {
				isGreeting = true;
				return Chat.retrunGreeting(); // 挨拶
			}
		}

		// ----- nlpQueueの同期処理 -----
		while (!nlpQueue.isEmpty()) {
			NatulalLanguageProcessing nlp = nlpQueue.poll();
			Future<Boolean> future = futureQueue.poll();
			boolean isFinish = false;
			try {
				isFinish = future.get(THREAD_LIMIT_TIME, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (isFinish) {
				for (String key : nlp.getSeparateTalks()) {
					// --- 話題ごとに処理 ---
					Topic topic = nlp.getTopics().get(key);
					Agent agent = nlp.getTalk().getAgent(); // 発言者
					switch (topic) {
					case COMINGOUT:
						// エージェント情報の更新
						AgentInfo agentInfo = boardSurface.getAgentInfo(agent); // 発言者の情報
						Role role = nlp.getRoles().get(key); // 発言した役職名
						agentInfo.setRole(role); // エージェント情報へ登録

						// 発言
						if (role == Role.SEER) { // 占い師COの場合
							talkQueue.offer("占い師が出てきたね");
							if (boardSurface.getMyRole().equals(Role.SEER)
									|| boardSurface.getMyRole().equals(Role.POSSESSED)) {
								talkQueue.offer(agent + "は偽物だよ！気をつけて！"); // 自分が占い師か狂人の時は抗議する
							}
						} else if (role == Role.POSSESSED) {
							talkQueue.offer(">>" + agent + " 狂人だったの！？");
						} else if (role == Role.WEREWOLF) {
							talkQueue.offer(">>" + agent + " 今，狼の声がしなかった？");
						} else if (role == Role.VILLAGER) {
							talkQueue.offer(">>" + agent + " 村勝利のために頑張ろう");
						}

						break;
					case DIV_INQ:
						// 盤面情報の更新
						Agent target = nlp.getTargets().get(key);
						Species species = nlp.getSpecies().get(key);
						boardSurface.setReportSpecies(agent, target, species);

						// 占い結果の復唱
						if (species == Species.HUMAN) {
							talkQueue.offer(target + "は白なんだね。白確定かな？");
						} else if (species == Species.WEREWOLF) {
							talkQueue.offer(target + "は黒なんだね。やった！人狼を見つけたよ");
						}

						// 発言
						if (target.equals(currentGameInfo.getAgent())) { // 自分に対して判定が出た場合
							if (species == Species.HUMAN) {
								talkQueue.offer("これで白確定かな？");
							} else if (species == Species.WEREWOLF) {
								talkQueue.offer("ちょっと待ってよ！自分は人間だって！");
							}
						}

						break;
					case QUESTION:
						Agent target2 = nlp.getTargets().get(key);
						// 自分自身に発言がされた場合は何か言い返す
						if (target2.equals(currentGameInfo.getAgent())) {
							String response = Response.responce(key);
							if (response.equals("")) {
								talkQueue.offer(">>" + agent + " そんなに考えてなかったな");
							} else {
								talkQueue.offer(">>" + agent + response);
							}
						}
						break;
					default:
						break;
					}
				}
			}
		}

		// ここまでの処理を計測しログファイル出力
		double end = System.nanoTime();
		double time = (end - start) / 1000000;
		logger.info("talk() time spent:" + time + "ms");

		// 発言キューが空になるまで発言し続ける
		if (!talkQueue.isEmpty()) {
			logger.config("==========");
			return talkQueue.poll();
		} else {
			// 発言キューが空になったので，雑談文を"CHAT_LIMIT"つほど入れ込む
			while (chatTimes < CHAT_LIMIT) {
				chatTimes++;
				return Chat.returnChat(currentGameInfo.getDay(), boardSurface.getMyRole());
			}
		}
		logger.config("==========");
		return Talk.OVER;
	}

	@Override
	public void update(GameInfo gameInfo) {
		logger.config("update()");
		// currentGameInfo をアップデート
		currentGameInfo = gameInfo;
		ExecutorService executorService = Executors.newFixedThreadPool(3); // singleじゃないとメモリリークする？対処法模索必要

		// GameInfo.talkListからカミングアウト・占い報告・霊媒報告を抽出
		for (int i = talkListHead; i < currentGameInfo.getTalkList().size(); i++) {
			// 発話内容取得
			Talk talk = currentGameInfo.getTalkList().get(i);
			// 自然言語解析のスレッド開始
			NatulalLanguageProcessing nlp = new NatulalLanguageProcessing(gameInfo, talk);
			futureQueue.offer(executorService.submit(nlp));
			// nlpQueueに追加
			nlpQueue.add(nlp);
		}
		executorService.shutdown();

		talkListHead = currentGameInfo.getTalkList().size();
		// ログ出力
		logger.config("==========");
	}

	/**
	 * 信用度が低いものから投票をする
	 */
	@Override
	public Agent vote() {
		logger.config("vote()");

		// 信用度が低い生きているエージェントに対して投票をおこなう

		for (Agent agent : currentGameInfo.getAliveAgentList()) { // 全てのエージェントを走査
			AgentInfo agentInfo = boardSurface.getAgentInfo(agent);
			if (agentInfo.getTrustly() == Float.MIN_VALUE) { // 確定黒へ投票
				logger.fine("投票先：" + agent);
				logger.config("==========");
				return agent;
			}
		}
		List<Agent> agentList = returnGrayList(); // 灰色からランダムに選ぶ
		if (!agentList.isEmpty()) { // リストがからの場合があるのでnull回避
			Agent agent = randomSelect(agentList);
			logger.fine("投票先：" + agent);
			logger.config("==========");
			return agent;
		}

		logger.fine("投票先：null");
		logger.config("==========");
		return null;
	}

	/**
	 * 5人対戦のためこのメソッドは呼ばれない（未実装）
	 */
	@Override
	public String whisper() {
		logger.config("whisper()");
		logger.config("==========");
		return null;
	}

	/*
	 * リストからランダムに選んで返す
	 *
	 * @param list リスト（空リストも含む）
	 *
	 * @return リストから一つの要素をランダムに返す（空リストの場合はnullを返す）
	 */
	<T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	/*
	 * 襲撃された人を特定 （このままだと５人村2日目しか対応してなくね？）
	 */
	Agent attackedAgent() {
		List<Agent> agentList = currentGameInfo.getAgentList();
		List<Agent> aliveAgentList = currentGameInfo.getAliveAgentList();
		// agentList - aliveAgentList
		for (Agent agent : agentList) {
			if (!aliveAgentList.contains(agent) && !currentGameInfo.getExecutedAgent().equals(agent)) {
				return agent;
			}
		}
		return null;
	}

	/*
	 * このメソッドを呼び出したメソッドの呼び出し元のメソッド名、ファイル名、行数の情報を取得します
	 *
	 * @return メソッド名、ファイル名、行数の情報文字列
	 */
	static String calledFrom() {
		StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
		if (steArray.length <= 3) {
			return "";
		}
		StackTraceElement ste = steArray[3];
		StringBuilder sb = new StringBuilder();
		sb.append(ste.getMethodName()) // メソッド名取得
				.append("(").append(ste.getFileName()) // ファイル名取得
				.append(":").append(ste.getLineNumber()) // 行番号取得
				.append(")");
		return sb.toString();
	}

	/*
	 * ログ出力する際に定型文を返したいためmethodにしておく
	 *
	 * @return ログに出力する文字列
	 */
	String getBasicLogString() {
		return " PlayerName: " + currentGameInfo + " Method: " + calledFrom() + "\t|";
	}

	/*
	 * 自分以外の灰リストを返す
	 */
	List<Agent> returnGrayList() {
		List<Agent> aliveAgentList = currentGameInfo.getAliveAgentList(); // 生存者リスト取得
		aliveAgentList.remove(currentGameInfo.getAgent()); // 自分自身は除外
		List<Agent> candidates = new ArrayList<>();
		for (Agent agent : aliveAgentList) {
			Float trustly = boardSurface.getAgentInfo(agent).getTrustly();
			if (trustly != Float.MAX_VALUE && trustly != Float.MIN_VALUE) {
				candidates.add(agent); // 候補に加える
			}
		}
		return candidates;
	}
}
