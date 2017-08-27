package com.icloud.itfukui0922.connect;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Random;

import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.TcpipClient;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.net.TcpipServer;
import org.aiwolf.server.util.FileGameLogger;

public class Starter {

	/**
	 * ローカルホストのサーバを立ち上げます
	 * また，ゲームログをlogディレクトリ下に保存します
	 *
	 * @param port	接続先ポート番号
	 * @param gameNum	ゲーム試行回数
	 * @param participant_players	ゲーム参加者数
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public static void startServer(int port, int gameNum, int participant_players) throws SocketTimeoutException {
		GameSetting gameSetting = GameSetting.getDefaultGame(participant_players);
		gameSetting.setValidateUtterance(false);
		gameSetting.setTalkOnFirstDay(true);
		gameSetting.setTimeLimit(5000);

		new Thread() {
			public void run() {
				try {
					TcpipServer gameServer = new TcpipServer(port, participant_players, gameSetting);
					gameServer.waitForConnection();
					AIWolfGame game = new AIWolfGame(gameSetting, gameServer);

					for(int i = 0; i < gameNum; i++){
						game.setRand(new Random(i));
						Calendar calendar = Calendar.getInstance();
						game.setGameLogger(new FileGameLogger(new File("log/" + calendar.getTime() + "ServerLog.txt")));
						game.start();
					}
				} catch (IOException e) {
					System.err.println("ローカルサーバ立ち上げでIOException発生");
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * クライアントを接続します
	 * 正しいパラメータを設定しない場合は例外をスローします
	 *
	 * @param classPass	接続するエージェントのクラスパスを指定します（Playerインターフェースを実装していること）
	 * @param playerName	プレイヤー名を指定します
	 * @param host	接続先ホスト名を指定します
	 * @param port	接続先ポート番号を指定します
	 * @param numConnectiuons	接続数を指定します
	 * @throws ClassNotFoundException	指定したclassPassが正しくない場合にスローします
	 * @throws InstantiationException	クラスのインスタンス生成に失敗するとスローします
	 * @throws IllegalAccessException	アクセスに失敗するとスローします
	 */
	public static void startClient(String classPass, String playerName, String host, int port, int numConnectiuons) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		for (int i = 0; i < numConnectiuons; i++) {
			TcpipClient client = new TcpipClient(host, port);
			Class<?> class1 = Class.forName(classPass);
			Player player = (Player) class1.newInstance();
			client.connect(player);
			client.setName(playerName);
			System.out.println(playerName + "が接続されました．");
		}
	}

	/**
	 * クライアント接続します
	 * 正しいパラメータを設定しない場合は例外をスローします．
	 *
	 * 役職の希望提出が可能
	 * @param classPass
	 * @param playerName
	 * @param host
	 * @param port
	 * @param numConnectiuons
	 * @param role
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void startClient(String classPass, String playerName, String host, int port, int numConnectiuons, org.aiwolf.common.data.Role role) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		for (int i = 0; i < numConnectiuons; i++) {
			TcpipClient client = new TcpipClient(host, port, role);
			Class<?> class1 = Class.forName(classPass);
			Player player = (Player) class1.newInstance();
			client.connect(player);
			client.setName(playerName);
			System.out.println(playerName + "が接続されました．");
		}
	}
}
