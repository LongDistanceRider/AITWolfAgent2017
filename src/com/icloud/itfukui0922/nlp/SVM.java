package com.icloud.itfukui0922.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SVM implements Callable<String>{

	/* 解析する分類器を指定 */
	Classifier classifier;
	/* 解析する文　*/
	String text;
	/* モデルデータ */
	static Map<Classifier, svm_model> modelMap = new HashMap<Classifier, svm_model>();
	/* 単語ID */
	static Map<String, Integer> wordMap = new HashMap<>();
	/* タグID */
	static Map<Integer, String> tagMap = new HashMap<>();

	static {
		// 全てのモデルファイルと単語ID，タグIDファイルの読み込み
		try {
			svm_model svm_model = svm.svm_load_model("Topic.model");	// モデルファイルの読み込み
			modelMap.put(Classifier.Topic, svm_model);

			File file1 = new File("wordId.txt");	// 単語IDの読み込み
			BufferedReader bufferedReader1 = new BufferedReader(new FileReader(file1));
			String readLine1;
			while ((readLine1 = bufferedReader1.readLine()) != null) {
				String[] readArray = readLine1.split(",");	// 値とタグを分割
				wordMap.put(readArray[0], Integer.parseInt(readArray[1]));	// 単語がkey IDがvalue
			}
			bufferedReader1.close();

			File file2 = new File("tagId.txt");
			BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file2));
			String readLine2;
			while ((readLine2 = bufferedReader2.readLine()) != null) {
				String[] readArray = readLine2.split(",");	// 値とタグを分割
				tagMap.put(Integer.parseInt(readArray[1]), readArray[0]);	// 値がkey タグ名がvalue
			}
			bufferedReader2.close();

		} catch (IOException e) {
			System.err.println("SVMモデルファイル，単語IDファイル，タグIDファイルの読み込みに失敗");
			e.printStackTrace();
		}
	}

	public SVM(Classifier classifier, String text) {
		this.classifier = classifier;
		this.text = text;
	}

	@Override
	public String call() throws Exception {
		double start = System.nanoTime();//処理時間計測開始

		// テキストを単語へ分解後，ベクター化
		Tokenizer tokenizer = Tokenizer.builder().build();	// kuromoji準備
		Map<Integer, Integer> countMap = new TreeMap<>();	// 単語数カウント

		for (Token token : tokenizer.tokenize(text)) {	// 単語分割
	    	int wordId = wordMap.size();	// 単語IDファイルに単語がない場合は，使われていないIDを使う
			if (wordMap.containsKey(token.getSurfaceForm())) {	// リスト照会
				wordId = wordMap.get(token.getSurfaceForm());	// リストにあったため，IDを渡す
			}

	    	int value;	// 単語数カウント
	    	if (countMap.containsKey(wordId)) {	// 単語リストに入っているか
				value = countMap.get(wordId) + 1;	// 入っているためカウント++
			} else {
				value = 1;						// 入っていないためカウント１
			}
	    	countMap.put(wordId, value);
	    }

		//svm引き渡し用にベクター変換
		svm_node[] input = new svm_node[countMap.size()];
		int i = 0;
		for (Integer key : countMap.keySet()) {
			input[i] = new svm_node();
			input[i].index = key;
			input[i].value = countMap.get(key);
			i++;
		}

		//判別の実行
		double svmResult = svm.svm_predict(modelMap.get(Classifier.Topic),input);

		// 利用した分類器ごとに得られた値を変換する
		String result = null;
		switch (classifier) {
		case Topic:
			if (tagMap.containsKey((int)svmResult)) {
				result = tagMap.get((int)svmResult);
			} else {
				System.err.println("SVMから予期しない結果が返されました．");
			}
			break;

		default:
			break;
		}

		// 処理時間計測終了
		double end = System.nanoTime();
		double time = (end - start) / 1000000;
//		System.out.println("SVM結果取得時間：" + time + "ms，解析文：" + text + "，出力結果：" + result + "，SVM出力結果：" + svmResult);
		return String.valueOf(result);
	}


}
