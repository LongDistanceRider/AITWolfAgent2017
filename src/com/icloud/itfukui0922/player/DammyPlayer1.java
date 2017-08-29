
package com.icloud.itfukui0922.player;

import java.util.ArrayDeque;
import java.util.Queue;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class DammyPlayer1 implements Player {

	boolean isCO = false;
	/* 発言キュー */
	Queue<String> talkQueue;
	GameInfo currentGameInfo;

	@Override
	public Agent attack() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
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
		currentGameInfo = arg0;
		talkQueue = new ArrayDeque<>();
		talkQueue.offer("初めましてこれからよろしくね！");
		talkQueue.offer(">>Agent[01] なんでそう思うの？");
		// talkQueue.offer("人狼なんていないさ");
		// talkQueue.offer("あれ？占い師二人でた？");
		// talkQueue.offer("Agent[1]はちょっと嘘っぽいな");
		// talkQueue.offer("そんなことないよ");

	}

	@Override
	public String talk() {
		if (!isCO) {
			isCO = true;
			return "はい！ぼく占い師！Agent[02]の結果は白だったよ。";
		}

		if (currentGameInfo.getDay() >= 1) {
			// 発言キューが空になるまで発言し続ける
			if (!talkQueue.isEmpty()) {
				String talk = talkQueue.poll();
				return talk;
			}
		}
		return Talk.OVER;
	}

	@Override
	public void update(GameInfo arg0) {
		// TODO 自動生成されたメソッド・スタブ
		currentGameInfo = arg0;
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

}
