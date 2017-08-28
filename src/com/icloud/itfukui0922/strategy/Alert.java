package com.icloud.itfukui0922.strategy;

import com.icloud.itfukui0922.nlp.Role;

public class Alert {

	/**
	 * 自分の役職と相手の役職を比較し，問題がないかチェックする
	 * 現時点では占占のみアラートを返す
	 * @param myrole
	 * @param opponent
	 * @return 問題がある場合はtrueを返す
	 */
	public static boolean comingoutAlert(Role myrole, Role opponent) {
		// 占占
		switch (myrole) {
		case SEER:
			if (opponent.equals(Role.SEER)) {
				return true;
			}
			break;

		default:
			break;
		}
		return false;
	}

}
