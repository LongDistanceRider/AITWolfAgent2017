/**
 * Topic列挙型 NLP用に改変
 */
package com.icloud.itfukui0922.nlp;

public enum Topic {
	ESTIMATE,
	COMINGOUT,
	DIVINATION,
	DIVINED,
	IDENTIFIED,
	GUARD,
	GUARDED,
	VOTE,
	ATTACK,
	AGREE,
	DISAGREE,
	OVER,
	SKIP,
	OPERATOR,
	UNTAG,
	DIV_INQ,
	QUESTION;

	public static Topic getTopic(String string) {
		Topic[] topics;
		int topicsLength = (topics = values()).length;	// Topic数を代入

		for (int i = 0; i < topicsLength; i++) {
			Topic topic = topics[i];
			if (topic.toString().equalsIgnoreCase(string)) {
				return topic;
			}

		}
		return null;
	}

}
