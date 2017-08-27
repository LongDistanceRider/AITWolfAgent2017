package com.icloud.itfukui0922.connect;

import java.net.SocketTimeoutException;

import org.aiwolf.common.data.Role;

public class LocalHostStarter extends Starter {
	public static void main(String[] args) {
		try {
			startServer(10001, 1, 5);

			startClient("com.icloud.itfukui0922.player.AITWolfPlayer", "AITWolf", "localhost", 10001, 1, Role.SEER);
			startClient("com.icloud.itfukui0922.player.DammyPlayer2", "Dammy2", "localhost", 10001, 3);
			startClient("com.icloud.itfukui0922.player.DammyPlayer", "Dammy", "localhost", 10001, 1);

//			startClient("com.icloud.itfukui0922.AITWolfPlayer", "AITWolf", "localhost", 10001, 4);
		} catch (SocketTimeoutException e) {
			System.err.println("ソケットタイムアウトが発生しました．通信環境を確認してください");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("指定したクラスが見つかりません");
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.err.println("インスタンス生成に失敗しました．Playerインターフェースを実装しているか確認してください");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("予想しないクラス定義があります．パッケージの動的変更やクラスがprivateになっていないか確認してください");
			e.printStackTrace();
		}

	}
}
