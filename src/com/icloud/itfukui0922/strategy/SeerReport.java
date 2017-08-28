/**
 * 占い師の報告を保管する構造体
 */
package com.icloud.itfukui0922.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;

import com.icloud.itfukui0922.nlp.Species;

public class SeerReport {

	List<Map<Agent, Species>> reportList;

	public List<Map<Agent, Species>> getReportList () {
		return reportList;
	}
	public SeerReport() {
	}

	/**
	 * レポート追加
	 * @param target
	 * @param species
	 */
	public void publishReport (Agent target, Species species) {
		Map<Agent, Species> report = new HashMap<>();
		report.put(target, species);
		reportList.add(report);
	}
	/**
	 * 黒判定を受けたAgentを返す
	 *
	 * @return 黒判定を受けたAgentを返す（nullあり）
	 */
	public Agent publishedBlackAgent () {
		for (Map<Agent, Species> report : reportList) {
			for (Agent agent : report.keySet()) {
				Species species = report.get(agent);
				if (species == Species.WEREWOLF) {
					return agent;
				}
			}
		}
		return null;
	}


}
