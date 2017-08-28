/**
 * 何もしないエージェント
 */
package com.icloud.itfukui0922.player;

import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class DammyPlayer2 implements Player {

	GameInfo currentGameInfo;
	@Override
	public Agent attack() {
		// TODO 自動生成されたメソッド・スタブ
		// ----- 候補者がいないため，生存プレイヤーから適当にアタック -----
		List<Agent> candidates = currentGameInfo.getAliveAgentList();
		candidates.remove(currentGameInfo.getAgent());
		Agent agent = randomSelect(candidates);
		return agent;
	}

	@Override
	public void dayStart() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public Agent divine() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent guard() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void initialize(GameInfo arg0, GameSetting arg1) {
		this.currentGameInfo = arg0;
	}

	@Override
	public String talk() {
		return Talk.OVER;
	}

	@Override
	public void update(GameInfo arg0) {
		this.currentGameInfo = arg0;

	}

	@Override
	public Agent vote() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String whisper() {
		// TODO 自動生成されたメソッド・スタブ
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


}
