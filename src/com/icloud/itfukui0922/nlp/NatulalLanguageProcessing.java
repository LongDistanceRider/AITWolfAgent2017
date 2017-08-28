/**
 * 自然言語処理を扱うクラス
 *
 * Callが返す値が，解析完了したかどうかを返すBooleanというのは少し勿体無い気がする
 */
package com.icloud.itfukui0922.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

public class NatulalLanguageProcessing implements Callable<Boolean> {

	/* ゲーム情報 */
	GameInfo currentGameInfo;
	/* フィルター情報 */
	static List<String> filterList = new ArrayList<>();
	/* Talk型 */
	volatile Talk talk;
	/* 発言を文を分解 addで追加すること */
	volatile List<String> separateTalks;
	/* 発言に含まれいていた話題（複数可能) Topic.UNTAGで初期化 */
	volatile Map<String, Topic> topics;
	/* 発言に含まれていた役職名（複数可能) Role.NOTROLEで初期化 */
	volatile Map<String, Role> roles;
	/* 発言に含まれていたAgent名（複数可能) */
	volatile Map<String, Agent> targets;
	/* 発言に含まれていたSpecies（複数可能） */
	volatile Map<String, Species> species;
	/* スレッド待機時間 */
	final static long LIMIT_TIME = 2000;

	static {
		// フィルタ情報の読み込み
		try {
			File csv = new File("filterInformation.txt");

			BufferedReader bufferedReader = new BufferedReader(new FileReader(csv));
			String readLine;
			while ((readLine = bufferedReader.readLine()) != null) {
				filterList.add(readLine);
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			System.err.println("フィルタ情報読み込みでエラー" + e);
		} catch (IOException e) {
			System.err.println("フィルタ情報読み込みでエラー" + e);
		}

	}

	public Talk getTalk() {
		return talk;
	}

	public Map<String, Topic> getTopics() {
		return topics;
	}

	public Map<String, Role> getRoles() {
		return roles;
	}

	public Map<String, Agent> getTargets() {
		return targets;
	}

	public Map<String, Species> getSpecies() {
		return species;
	}

	public List<String> getSeparateTalks() {
		return separateTalks;
	}

	/**
	 * コンストラクタ
	 */
	public NatulalLanguageProcessing(GameInfo currentGameInfo, Talk talk) {
		this.currentGameInfo = currentGameInfo;
		this.talk = talk;
		topics = new HashMap<>();
		roles = new HashMap<>();
	}

	/**
	 * 言語解析開始
	 *
	 * 1.フィルタリング（雑談など解析不要な文を取り除く） 自分自身の発話，SKIP，OVERなど 2.文の分割 3.話題解析
	 * 4.話題別解析（誰に投票するか，なんの役職をCOしたのか，．．．）
	 */
	@Override
	public Boolean call() {
		Logger logger = Logger.getLogger("main" + currentGameInfo.getAgent());
		// ----- フィルタリング１ -----
		String text = talk.getText();
		if (talk.getAgent() == currentGameInfo.getAgent() || text.equals("") || text.equals("Over")
				|| text.equals("Skip") || isChat(text)) {
			// 解析する必要のない発話を除外（自分自身の発言，中身のない発言，Over・Skip発言，雑談
			//
			if (talk.getAgent() == currentGameInfo.getAgent()) {
				System.out.println(currentGameInfo.getDay() + "Agent[me]," + text + ",notNeed");
			} else if (!text.equals("Over") && isChat(text)) {
				System.out.println(currentGameInfo.getDay() + "" + talk.getAgent() + "," + text + ",notNeed");
			}
			//
			return false;
		}

		// ----- 文の分割 -----
		separateTalks = new ArrayList<>(separateText(text));
		// ----- TopicとRoleの初期化 -----
		for (String string : separateTalks) {
			initTopicsAndRoles(string);
		}

		// ----- 話題解析 -----
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		for (String string : separateTalks) {
			if (isChat(string)) {
				continue;
			}
			Future<String> future = executorService.submit(new SVM(Classifier.Topic, string));
			try {
				// ここでロジックエラー発生する可能性がある
				Topic topic = Topic.getTopic(future.get());
				topics.put(string, topic);
			} catch (InterruptedException | ExecutionException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			logger.fine("分割済み解析文：" + string + " 話題解析結果：" + topics.get(string));
		}

		// ----- 話題別解析（必要な情報が手に入らない場合は，話題をuntagへ変更する） -----

		for (String key : separateTalks) {
			Topic topic = topics.get(key);
			if (topic == null) {
				logger.warning("予期しない分岐に入りました[NLPクラス，話題別解析]");
				continue; // null回避
			}
			switch (topic) {
			case COMINGOUT:

				// 私，僕，俺，自分などの単語がない場合はカミングアウトとしない
				Role role = Role.NOTROLE;
				if (!watashiCheck(key)) {
					role = roleCheck(key);
					if (role == Role.NOTROLE) {
						topic = Topic.UNTAG;
						topics.put(key, Topic.UNTAG);
					} else {
						roles.put(key, role); // 役職
					}
				} else {
					topic = Topic.UNTAG;
					topics.put(key, Topic.UNTAG);
				}
				System.out.println(currentGameInfo.getDay() + "" + talk.getAgent() + "," + key + "," + topic + "," + role);
				break;
			case DIV_INQ:
				Agent target = targetCheck(key);
				Species species2 = speciesCheck(key);
				if (target == null || species2 == Species.UNKNOWN) {
					topic = Topic.UNTAG;
					topics.put(key, Topic.UNTAG);
				} else {
					targets.put(key, target);
					species.put(key, species2);
				}

				logger.fine("Species推定結果：" + species.get(key));
				if (targets.get(key) != null) {
					logger.fine("target推定結果：" + targets.get(key));
				} else {
					logger.fine("target推定結果：null");
				}
				System.out.println(currentGameInfo.getDay() + "" + talk.getAgent() + "," + key + "," + topic + "," + target + "," + species2);
				break;
			case QUESTION:
				Agent target2 = targetCheck(key);
				if (target2 == null) {
					topic = Topic.UNTAG;
					topics.put(key, Topic.UNTAG);
				} else {
					targets.put(key, targetCheck(key));
				}

				if (targets.get(key) != null) {
					logger.fine("target推定結果：" + targets.get(key));
				} else {
					logger.fine("target推定結果：null");
				}
				System.out.println(currentGameInfo.getDay() + "" + talk.getAgent() + "," + key + "," + topic + "," + target2);
			default:
				System.out.println(currentGameInfo.getDay() + "" + talk.getAgent() + "," + key + "," + topic);
				break;
			}

		}
		return true;
	}

	/*
	 * 一文に一人称の単語があるか
	 */
	private boolean watashiCheck(String text) {
		// 特定の単語があるかで役職を返す
		if (text.indexOf("私") != -1) {
			return true;
		} else if (text.indexOf("自分") != -1) {
			return true;
		} else if (text.indexOf("俺") != -1) {
			return true;
		} else if (text.indexOf("わたし") != -1) {
			return true;
		} else if (text.indexOf("あたし") != -1) {
			return true;
		}

		return false;
	}

	/*
	 * 一文になんの単語があるかで役職を返す
	 */
	private Role roleCheck(String text) {
		// 特定の単語があるかで役職を返す
		if (text.indexOf("占い師") != -1) {
			return Role.SEER;
		} else if (text.indexOf("人狼") != -1) {
			return Role.WEREWOLF;
		} else if (text.indexOf("村人") != -1) {
			return Role.VILLAGER;
		} else if (text.indexOf("裏切り者") != -1) {
			return Role.POSSESSED;
		} else if (text.indexOf("狂人") != -1) {
			return Role.POSSESSED;
		}

		return Role.NOTROLE;
	}

	/*
	 * 一文になんの単語があるかでSpeciesを返す
	 */
	private Species speciesCheck(String text) {
		if (text.indexOf("白") != -1) {
			return Species.HUMAN;
		} else if (text.indexOf("村人") != -1) {
			return Species.HUMAN;
		} else if (text.indexOf("人間") != -1) {
			return Species.HUMAN;
		} else if (text.indexOf("黒") != -1) {
			return Species.WEREWOLF;
		} else if (text.indexOf("人狼") != -1) {
			return Species.WEREWOLF;
		}
		return Species.UNKNOWN;
	}

	/*
	 * 誰に向けられた発言か（target)を調べる いない場合はnullが返却される
	 */
	private Agent targetCheck(String text) {
		List<Agent> agentList = currentGameInfo.getAgentList();
		for (Agent agent : agentList) {
			String agentName = agent.toString();
			if (text.indexOf(agentName) != -1) {
				return agent;
			}
		}
		return null;
	}

	/*
	 * 雑談かどうかを判定
	 */
	private boolean isChat(String text) {
		for (String filter : filterList) {
			if (text.indexOf(filter) != -1) {
				return false;
			}
		}
		return true;
	}

	/*
	 * 文の分割
	 */
	private static List<String> separateText(String text) {
		String[] stringArray = text.split("[!.。！]");
		List<String> strings = new ArrayList<>();
		for (int i = 0; i < stringArray.length; i++) {
			strings.add(stringArray[i]);
		}
		return strings;
	}

	/*
	 * topicsとrolesを初期化
	 */
	private void initTopicsAndRoles(String key) {
		topics.put(key, Topic.UNTAG);
		roles.put(key, Role.NOTROLE);
	}
}
