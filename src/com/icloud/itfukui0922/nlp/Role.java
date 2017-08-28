package com.icloud.itfukui0922.nlp;

import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Team;

public enum Role {
	POSSESSED(Team.WEREWOLF, Species.HUMAN),
	SEER(Team.VILLAGER, Species.HUMAN),
	VILLAGER(Team.VILLAGER, Species.HUMAN),
	WEREWOLF(Team.WEREWOLF, Species.WEREWOLF),
	NOTROLE(Team.VILLAGER, Species.HUMAN);

	private Team teamType;
	private Species species;

	private Role(Team teamType, Species species) {
		this.teamType = teamType;
		this.species = species;
	}

	public Team getTeam() {
		return this.teamType;
	}

	public Species getSpecies() {
		return this.species;
	}
	public static Role getRole(String string) {
		Role[] roles;
		int rolesLength = (roles = values()).length;	// Topic数を代入

		for (int i = 0; i < rolesLength; i++) {
			Role role = roles[i];
			if (role.toString().equalsIgnoreCase(string)) {
				return role;
			}

		}
		return null;
	}
}
