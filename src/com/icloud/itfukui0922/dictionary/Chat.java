/**
 * 挨拶や雑談を返すクラス
 *
 */
package com.icloud.itfukui0922.dictionary;

import java.util.ArrayList;
import java.util.List;

import com.icloud.itfukui0922.nlp.Role;

public class Chat {

	/* 挨拶文 */
	static List<String> greetingList = new ArrayList<>();
	/* いつでもできる雑談文 */
	static List<String> chatList = new ArrayList<>();
	/* 1日目に発言できる雑談文 */
	static List<String> firstDayChatList = new ArrayList<>();
	/* 2日目に発言できる雑談文 */
	static List<String> secondDayChatList = new ArrayList<>();
	/* 占い師が発言できる雑談文 */
	static List<String> seerChatList = new ArrayList<>();
	/* 人狼が発言できる雑談文 */
	static List<String> werewolfChatList = new ArrayList<>();
	/* 話した雑談文 */
	static List<String> spokenList = new ArrayList<>();

	static {
		greetingList.add("よろしくお願いします！");
		greetingList.add("初めまして、これからよろしくね！");
		greetingList.add("よろー。本気でいくよ。");
		greetingList.add("こんにちは。楽しく人狼しよう！");
		greetingList.add("今日こそは負けないよ！よろしく。");
		chatList.add("人狼って楽しいよね！");
		chatList.add("人狼がいるって？あなた疲れてるのよ。");
		chatList.add("人狼を強いられているだ！");
		chatList.add("荒ぶる人狼のポーズ");
		chatList.add("乗るしかない。このビックウェーブに");
		chatList.add("人狼は消毒だー！");
		chatList.add("人狼だって生きてるんだって？それサバンナでも同じこと言えんの？");
		chatList.add("今日も人狼がんばるぞい！");
		chatList.add("何もかもが疑わしい");
		chatList.add("これもうわかんねぇな");
		chatList.add("もし私に投票しなければ世界の半分をやろう");
		chatList.add("ただの人狼に興味ありません");
		chatList.add("一番いい人狼を頼む");
		chatList.add("全てが人狼色に染まっていく");
		chatList.add("人狼は一瞬が命なんです。");
		chatList.add("嫌なことがあっても、人狼やると楽しいな。");
		chatList.add("人狼って見てる人もやってる人も楽しめるなら、一石二鳥だね！");
		chatList.add("何が起こるかわからんから気をつけろ");
		chatList.add("生きてさえいれば、ちょっとやそっとのことは乗り越えられるよ");
		chatList.add("人狼を駆逐してやる！！");
		firstDayChatList.add("人狼がいるって？よろしいならば戦争だ");
		firstDayChatList.add("私の戦闘力は53万です");
		firstDayChatList.add("私を襲撃してもいいよ。ただし、その頃にはお前は八つ裂きになっているだろうがな。");
		firstDayChatList.add("目的はただひとつ、サーチアンドデストロイ、サーチアンドデストロイだ！");
		firstDayChatList.add("いいですか？追放していい相手は人狼と狂人だけです。");
		secondDayChatList.add("この村は人狼のためのハッピーセットかよ");
		secondDayChatList.add("諦めたらそこで人狼終了ですよ。");
		secondDayChatList.add("人狼が終わっても、ズッ友だよ！");
		secondDayChatList.add("人狼がいるって、はっきり湧かんだね");
		secondDayChatList.add("最後に追放すると言ったな。あれは嘘だ");
		secondDayChatList.add("ヘルメットがなければ即死だった");
		secondDayChatList.add("まだ慌てるような時間じゃない");
		secondDayChatList.add("予想の斜め上");
		secondDayChatList.add("ボスケテ");
		secondDayChatList.add("逃げちゃダメだ、逃げちゃダメだ");
		secondDayChatList.add("悲しんでも、襲撃された人は戻ってきませんから");
		secondDayChatList.add("だんないよー。村はきっと助かるって");
		secondDayChatList.add("まだだ、まだ終わらんよ");
		secondDayChatList.add("まだ俺の人狼は終了してないぜ");
		secondDayChatList.add("一瞬、家族の顔が頭をよぎったぜ。");
		seerChatList.add("真実はいつもひとつ");
		seerChatList.add("人狼が恐ろしい？大丈夫だ問題ない");
		seerChatList.add("人狼を見つけるときのコツ？考えるな、感じるんだ");
		seerChatList.add("スリルと引き換えに給料分の仕事はしてやるよ");
		werewolfChatList.add("いつから人狼だと錯覚していた？");
		werewolfChatList.add("君のような感のいい村人は嫌いだよ");
		werewolfChatList.add("君たちには速さが足りない");
		werewolfChatList.add("人間を騙すなんて、ちょろいもんだぜ");
	}

	/**
	 * 挨拶文を返す
	 * @return 挨拶文
	 */
	public static String retrunGreeting () {
		return randomSelect(greetingList);
	}

	/**
	 * 雑談を返す
	 * @param gameDay ゲーム内日
	 * @param role 自分の役職
	 * @return 雑談文
	 */
	public static String returnChat (int gameDay, Role role) {
		List<String> candidates = new ArrayList<>();
		candidates.addAll(chatList);
		if (gameDay == 1) {
			candidates.addAll(firstDayChatList);
		} else if (gameDay == 2) {
			candidates.addAll(secondDayChatList);
		}
		if (role == Role.SEER) {
			candidates.addAll(seerChatList);
		} else if (role == Role.WEREWOLF) {
			candidates.addAll(werewolfChatList);
		}

		candidates.removeAll(spokenList);
		String string = randomSelect(candidates);
		spokenList.add(string);

		return randomSelect(candidates);
	}


	/*
	 * リストからランダムに選んで返す
	 *
	 * @param list リスト（空リストも含む）
	 *
	 * @return リストから一つの要素をランダムに返す（空リストの場合はnullを返す）
	 */
	static <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}
}
