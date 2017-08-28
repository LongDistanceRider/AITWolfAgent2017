package com.icloud.itfukui0922.nlp;

public enum Species {
	   HUMAN,
	   WEREWOLF,
	   UNKNOWN;

	public static Species getSpecies(String string) {
		Species[] speciess;
		int speciessLength = (speciess = values()).length;	// Topic数を代入

		for (int i = 0; i < speciessLength; i++) {
			Species species = speciess[i];
			if (species.toString().equalsIgnoreCase(string)) {
				return species;
			}

		}
		return null;
	}
}
