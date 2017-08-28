/**
 * 分類器名を分類器IDへ変換するために列挙型として定義
 */
package com.icloud.itfukui0922.nlp;

public enum Classifier {
	Topic;
	public static Classifier getTopic(String string) {
		Classifier[] classifiers;
		int classifierLength = (classifiers = values()).length;	// Topic数を代入

		for (int i = 0; i < classifierLength; i++) {
			Classifier classifier = classifiers[i];
			if (classifier.toString().equalsIgnoreCase(string)) {
				return classifier;
			}

		}
		return null;
	}

}
