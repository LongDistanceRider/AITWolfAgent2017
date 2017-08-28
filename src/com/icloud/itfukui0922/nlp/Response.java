/**
 * 返答処理
 * csvファイルにある返答ファイルとの照合をして返答文を返すクラス
 * Q文とA文がcsvファイルに記録されており，Q文と原文はレーベンシュタイン距離を用いて一定の近さが確認出来次第，そのQ文に対応するA文を返す
 * 一定の近さが確認できない場合は「へぇ？なんて？」みたいな文を返す
 */
package com.icloud.itfukui0922.nlp;

import org.aiwolf.common.data.Agent;

public class Response {

	Agent agent;	// 発言者Agent
	Agent target;	// 発言先Agent
	String text;	/// 原文
}
