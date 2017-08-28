/**
 * Singleton（インスタンスはgetInstanceで取得）
 * ゲームの盤面を管理
 * 自身のエージェントのフラグ，ゲーム状況の保持
 *
 */
package com.icloud.itfukui0922.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.net.GameInfo;

import com.icloud.itfukui0922.nlp.Role;
import com.icloud.itfukui0922.nlp.Species;

public class BoardSurface {

	/* ゲーム情報 */
	GameInfo currentGameInfo;
	/* プレイヤー情報 */
	Map<Agent, AgentInfo> agentInfoMap;
	/* 自分自身の役職 */
	Role myRole;
	/* カミングアウト済みか */
	boolean isCO = false;
	/* 占い結果を占い師の数だけ用意 */
	Map<Agent, SeerReport>  seerReportMap;

	/* シングルトン */
	private static BoardSurface boardSurface = new BoardSurface();
	private BoardSurface(){}
	public static BoardSurface getInstance(GameInfo gameInfo, Map<Agent, AgentInfo> agentInfoMap) {
		boardSurface.currentGameInfo = gameInfo;
		boardSurface.agentInfoMap = agentInfoMap;
		boardSurface.myRole = Role.getRole(gameInfo.getRole().toString());
		boardSurface.seerReportMap = new HashMap<>();
		return boardSurface;
	}

	public boolean getIsCO () {
		return isCO;
	}

	public void setIsCO (boolean isCO) {
		this.isCO = isCO;
	}

	public AgentInfo getAgentInfo(Agent agent) {
		return agentInfoMap.get(agent);
	}

	public void setReportSpecies(Agent agent, Agent target, Species species) {
		if (seerReportMap.containsKey(agent)) {
			// 占い報告を追記
			SeerReport seerReport = seerReportMap.get(agent);
			seerReport.publishReport(target, species);
		} else {
			// 新しい占い師が出たので，インスタンス作成と占い報告を追加
			SeerReport seerReport = new SeerReport();
			seerReport.publishReport(target, species);
			seerReportMap.put(agent, seerReport);
		}
	}
	public Role getMyRole () {
		return myRole;
	}

	public Map<Agent, SeerReport> getSeerReportMap () {
		return seerReportMap;
	}

	/**
	 *  プレイヤー情報の更新と盤面更新
	 * @param agentInfoMap
	 */
	public void update(GameInfo gameInfo, Map<Agent, AgentInfo> agentInfoMap) {
		this.currentGameInfo = gameInfo;
		this.agentInfoMap = agentInfoMap;
	}

	/**
	 * 占い師の数を返す
	 */
	public int countSeer () {
		int count = 0;
		for (Agent key : agentInfoMap.keySet()) {
			AgentInfo agentInfo = agentInfoMap.get(key);
			Role role = agentInfo.getRole();
			if (role.equals(Role.SEER)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 自分自身と同じ役職を持っているエージェントを返す
	 * いない場合はnullを返す
	 * @return　自分自身と同じ役職をCOしたAgentまたはnull
	 */
	public Agent sameRole () {
		for (Agent key : agentInfoMap.keySet()) {
			AgentInfo agentInfo = agentInfoMap.get(key);
			Role targetRole = agentInfo.getRole();
			if (myRole.equals(targetRole)) {
				return agentInfo.getAgent();
			}
		}
		return null;
	}

	/**
	 * 現在のBoardSurfaceのフィールドをレポートします
	 *
	 */

	public void report () {
		System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		System.out.println("   B o a r d   S u r f a c e   R e p o r t");
		System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		System.out.println("=-= AgentInfo =-=");
		for (Agent agent : agentInfoMap.keySet()) {
			AgentInfo agentInfo = agentInfoMap.get(agent);
			System.out.println(agentInfo.getAgent() + "," + agentInfo.getRole() + "," + agentInfo.getTrustly() + "," + agentInfo.getIsAlive());
		}
		System.out.println("=-= myRole =-=");
		System.out.println(myRole);
		System.out.println("=-= isCO =-=");
		System.out.println(isCO);
		System.out.println("=-= seerReport =-=");
		try {
		for (Agent key : seerReportMap.keySet()) {
			SeerReport seerReport = seerReportMap.get(key);
			List<Map<Agent, Species>> reportList = seerReport.getReportList();
			for (Map<Agent, Species> map : reportList) {
				for (Agent key2 : map.keySet()) {
					System.out.println(key + "," + key2 + "," + map.get(key2));
				}
			}
		}
		} catch (NullPointerException e) {
			System.out.println("null");
		}
		System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}
}
