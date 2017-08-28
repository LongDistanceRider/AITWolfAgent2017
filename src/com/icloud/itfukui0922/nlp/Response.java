/**
 * 返答処理
 * csvファイルにある返答ファイルとの照合をして返答文を返すクラス
 * Q文とA文がcsvファイルに記録されており，Q文と原文はレーベンシュタイン距離を用いて一定の近さが確認出来次第，そのQ文に対応するA文を返す
 * 一定の近さが確認できない場合は「へぇ？なんて？」みたいな文を返す
 */
package com.icloud.itfukui0922.nlp;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Response {

	static Map<String, String> responseMap = new HashMap<>();
	static final int THRESHOLD = 5;

	static {
		// ファイル読み込みしろよって思う。2018では直す
		responceMap.put("なぜAgentに投票しましたか？", "そんなに考えてなかった。あえて言うなら直感かな");

	}

	/**
	 * 応答を返す． 適切な応答がない場合は""を返すため，呼び出し先で適切な処理を行う必要がある
	 *
	 * @param string
	 *            質問文
	 * @return 応答（""あり)
	 */
	public static String responce(String question) {
		// レーベンシュタイン距離取得
		for (String key : responseMap.keySet()) {
			int levenshteinDistance = StringUtils.getLevenshteinDistance(question, key);
			if (levenshteinDistance < THRESHOLD) {
				return responseMap.get(key);
			}
		}
		return "";
	}
}
