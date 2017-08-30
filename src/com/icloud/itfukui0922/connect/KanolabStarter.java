package com.icloud.itfukui0922.connect;

public class KanolabStarter extends Starter{
	public static void main(String[] args){
		try {
			startClient("com.icloud.itfukui0922.player.AITWolfPlayer", "AITWolf", "kachako.org", 10000, 1);
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
