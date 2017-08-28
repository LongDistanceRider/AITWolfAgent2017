/**
 * 参加プレイヤの情報を保持するクラス
 */
package com.icloud.itfukui0922.strategy;

import org.aiwolf.common.data.Agent;

import com.icloud.itfukui0922.nlp.Role;

public class AgentInfo {

	/** Agent */
	Agent agent;
	/** 生きているか */
	boolean isAlive;
	/** 信用度（確定白はMAX_VALUE，確定黒はMIN_VALUE） */
	float trustly = 0;
	/** 信用度ロック */
	boolean trustlyLock;
	/** カミングアウトした役職　*/
	Role role;

	public AgentInfo(Agent agent) {
		this.agent = agent;
		isAlive= true;
		trustly = 0.5f;
		role = Role.NOTROLE;
		trustlyLock = false;
	}

	public Agent getAgent() {
		return agent;
	}

	public boolean getIsAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public float getTrustly() {
		return trustly;
	}

	public void setTrustly(float trustly) {
		if (!trustlyLock) {
			this.trustly = trustly;
		}

		// MAX_VALUE MIN_VALUEのどちらかをセットされたらtrustlyをロックする
		if (trustly == Float.MAX_VALUE || trustly == Float.MIN_VALUE) {
			trustlyLock = true;
		}
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}



}
