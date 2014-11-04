package com.dianping.algorithm.dp_review_mining.segment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Segmentation {
	private HMM _hmm = new HMM();
	
	private String delimiter = "≠！＃≤％≥＇的＆）（＋＊－，／．─：；＜＝四＞？\"▲△'≈★.-□■［±>·＼<］∥←↑→↓◇◆∶—_～●’◎‘×X○”“。、⑨⑩…⑤》⑥《⑦〉⑧〈』①￣『②③④〓‰\\″【′】〔〕⑵※⑶√";
	private String sentenceTerminalSymbol = ".。?？!！…";
//	private TreeMap<String, String> _customedWordMap = new TreeMap<String, String>(new LongerStringFirst());
	private Pattern sentPattern = Pattern.compile(".+?[,，、：“”\";；.。?？!！…]");
	
	//DEBUG
	private long runTime_atomSegmentation = 0;
	private long runTime_buildMatrix = 0;
	private long runTime_buildPath = 0;
	private long runTime_backTrack = 0;
	
	public Segmentation(boolean train, boolean addDict) {
		// TODO Auto-generated constructor stub
		if (train) {
			_hmm.train("./data/pd-corpus/");
		} else {
			_hmm.readModel();
		}
		if (addDict) {
			System.out.println("addDict");
			addDict("./data/segmenter/dish.txt", 10, 100, 1);
			System.out.println("done");
		}
	}
	
	
	private class Node {
		public String	_tag;
		public double _emit;
		public double _total;
		public int _best_prev;
		
		public Node () {
			
		}
		public Node(Node n) {
			// TODO Auto-generated constructor stub
			_tag = n._tag;
			_emit = n._emit;
			_total = n._total;
			_best_prev = n._best_prev;
		}
	}
	
	private ArrayList<ArrayList<Node>> _matrix = new ArrayList<ArrayList<Node>>();
	private ArrayList<String> _atomList = new ArrayList<String>();
	
	/**
	 * 切分句子
	 * @param sentence
	 * @return
	 */
	private void segmentSentence(String sentence, ArrayList<String> wordList, ArrayList<String> posList) {
		sentence = sentence.replaceAll("[\\s\\x85\\xA0\\u1680\\u180E\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000]+", " ").trim();
		if (sentence.isEmpty()) {
			return;
		}
		if (!sentence.endsWith("。")) {
			sentence += "。";
		}
//		String[] sentenceList = sentence.split("[,，、：“”\";；.。?？!！…]");
		
		Matcher matcher = sentPattern.matcher(sentence);
		while (matcher.find()) {
			String sent = matcher.group(0);
//			System.out.println(sent);
			_atomList.clear();
			_matrix.clear();
			long t1 = System.currentTimeMillis();
			atomSegmentation(sent); //结果保存在_atomList
			long t2 = System.currentTimeMillis();
			runTime_atomSegmentation += t2 - t1;
//			System.out.println();
//			printAtomList();
//			System.out.println();
			t1 = System.currentTimeMillis();
			buildMatrix();              //结果保存在_matrix
			t2 = System.currentTimeMillis();
			runTime_buildMatrix += t2 - t1;
//			printMatrix();
			t1 = System.currentTimeMillis();
			buildPath();
			t2 = System.currentTimeMillis();
			runTime_buildPath += t2 - t1;
//			printMatrix();
			t1 = System.currentTimeMillis();
			backTrack(wordList, posList);
			t2 = System.currentTimeMillis();
			runTime_backTrack += t2 - t1;
		}
	}
	
	/**
	 * 原子切分,连续的英文字母与数字不做切分
	 * @param sentence
	 */
	private void atomSegmentation(String sentence) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < Const.NGRAM - 1; i++) {
			_atomList.add(Const.BEG);
		}
		for (int i = 0; i < sentence.length(); ) {
			if (Character.isUpperCase(sentence.charAt(i)) || Character.isLowerCase(sentence.charAt(i)) || Character.isDigit(sentence.charAt(i)) 
			    || String.valueOf(sentence.charAt(i)).matches("[一二三四五六七八九零]") || (sb.length() > 0 && String.valueOf(sentence.charAt(i)).matches("[年月份日\\.\\%百千万亿元点]") )) {
				sb.append(sentence.charAt(i++)); 
				while (Character.isUpperCase(sentence.charAt(i)) || Character.isLowerCase(sentence.charAt(i)) || Character.isDigit(sentence.charAt(i))) {
					sb.append(sentence.charAt(i++));
				}
			} else {
				if (sb.length() > 0) {
//					System.out.println(sb.toString());
					_atomList.add(sb.toString());
					sb.setLength(0);
				} 
				_atomList.add(String.valueOf(sentence.charAt(i)));
				i++;
			}
		}
		for (int i = 0; i < Const.NGRAM - 1; i++) {
			_atomList.add(Const.END);
		}
	}
	
	/**
	 * 利用_hmm构建_matrix
	 */
	private void buildMatrix() {
		int i = 0;
		for (; i < Const.NGRAM - 1; i++) {
			assert(_atomList.get(i).equals(Const.BEG));
			Node node = new Node();
			node._tag = Const.BEG;
			ArrayList<Node> tmpList = new ArrayList<Node>();
			tmpList.add(node);
			_matrix.add(tmpList);
		}
		for (; i < _atomList.size() - Const.NGRAM + 1; i++) {
			if (_hmm.existFreq(i, _atomList.get(i))) {
				ArrayList<Node> tmpList = new ArrayList<Node>();
				HashMap<String, Double> entry = _hmm.get_emit().get(_atomList.get(i));
				for (Map.Entry<String, Double> tmpEntry : entry.entrySet()) {
					if (checkEmit(i, tmpEntry.getKey())) {
						Node node = new Node();
						node._tag = tmpEntry.getKey();
						node._emit = tmpEntry.getValue();
						tmpList.add(node);
					}
				}
				_matrix.add(tmpList);
			} else {
				guessOOVTag(_atomList.get(i).charAt(_atomList.get(i).length() - 1));
			}
		}
		
		for (; i < _atomList.size(); i++) {
			assert(_atomList.get(i).equals(Const.END));
			Node node = new Node();
			node._tag = Const.END;
			ArrayList<Node> tmpList = new ArrayList<Node>();
			tmpList.add(node);
			_matrix.add(tmpList);
		}
	}
	
	boolean checkEmit(int position, String tag) {
		return true;
//		for (int i = 0; i < _matrix.get(position-1).size(); i++) {
//			String key1 = _matrix.get(position-1).get(i)._tag + "_" + _atomList.get(position-1) + "_" + tag;
//			double freq = _hmm.getFreq(_hmm.get_emit_two(), key1, tag);
//			if (freq <= Const.MIN_FREQ) {
//				key1 = _matrix.get(position-1).get(i)._tag + "_" + tag;
//				freq = _hmm.getFreq(_hmm.get_emit_two(), key1, tag);
//			}
//			if (freq > Const.MIN_FREQ) {
//				return true;
//			}
//		}
//		return false;
	}
	
	private void printAtomList() {
		System.out.println("atomList:");
		for (String str : _atomList) {
			System.out.print(str + " ");
		}
		System.out.println();
	}
	
	private void printMatrix() {
		for (int i = 0; i < _matrix.size(); i++) {
			System.out.println(_atomList.get(i) + ":");
			for (int j = 0; j < _matrix.get(i).size(); j++) {
				Node node = _matrix.get(i).get(j);
				System.out.print(j + "\t");
				System.out.print("<tag: " + node._tag + ">\t");
				System.out.print("<emit: " + node._emit + ">\t");
				System.out.print("<total: " + node._total + ">\t");
				System.out.println("<best_prev: " + node._best_prev + ">");
			}
		}
	}
	
	private void printSegmentResult(ArrayList<String> wordList, ArrayList<String> posList) {
		if (wordList == null || posList == null || wordList.isEmpty() || posList.isEmpty()) {
			return;
		}
		assert(wordList.size() == posList.size());
		for (int i = 0; i < wordList.size(); i++) {
			if (wordList.get(i).equals("enter")) {
				System.out.println();
				continue;
			}
			if (posList.get(i).startsWith("nr") && i + 1 < posList.size() && posList.get(i+1).startsWith("nr")) {
				System.out.print(wordList.get(i) + wordList.get(i+1));
				System.out.print("/nr ");
				i++;
				continue;
			}
			System.out.print(wordList.get(i) + "/" + posList.get(i) + "  ");
		}
		System.out.println();
	}
	
	
	private void guessOOVTag(char character) 
	{
		Node node = new Node();
		ArrayList<Node> tmpList = new ArrayList<Node>();
		if(Character.isUpperCase(character) || Character.isLowerCase(character)) 
		{
			node._emit = Const.MIN_FREQ;
			node._tag = "S_n";
			tmpList.add(node);
			node._tag = "S_t";
			tmpList.add(node);
			node._tag = "S_m";
			tmpList.add(node);
			node._tag = "S_ns";
			tmpList.add(node);
			node._tag = "S_nt";
			tmpList.add(node);
			node._tag = "S_nr";
			tmpList.add(node);
//			node._tag = "B_n";
//			tmpList.add(node);
		} else if ( Character.isDigit(character) || String.valueOf(character).matches("[一二三四五六七八九零百千万亿元点\\%]")) {
	
			node._tag = "S_m";
			tmpList.add(node);
		} else if (String.valueOf(character).matches("[年月日份]")) {
			node._tag = "S_t";
			tmpList.add(node);
		}
		else
		{
			node._emit = Const.MIN_FREQ;
			node._tag = "E_nr";
			tmpList.add(node);
			node._tag = "B_nr";
			tmpList.add(node);
			node._tag = "B_n";
			tmpList.add(node);
			node._tag = "E_n";
			tmpList.add(node);
			node._tag = "S_n";
			tmpList.add(node);
			node._tag = "M_n";
			tmpList.add(node);
		}
		_matrix.add(tmpList);
	}
	
	private void buildPath() {
		switch (Const.NGRAM) {
		case 2:
			buildBiGramPath();
			break;
			
		case 3:
			buildTriGramPath();
			break;

		default:
			break;
		}
	}
	
	/**
	 * 构造切分路径
	 */
	private void buildTriGramPath() {
		StringBuffer key1 = new StringBuffer();
		double transFreq = 0.0;
		double emitFreq = 0.0;
		double emitFreq1, emitFreq2;
		double maxTotal = Const.MIN_FREQ;
		int i = 0;
		int j = 0;
		int i1 = 0;
		int i2 = 0;
		for (i = Const.NGRAM - 1; i < _matrix.size(); i++) {
			for (j = 0; j < _matrix.get(i).size(); j++) {
				maxTotal = Const.MIN_FREQ;
				for (i2 = 0; i2 < _matrix.get(i-2).size(); i2++) {
					for (i1 = 0; i1 < _matrix.get(i-1).size(); i1++) {
						key1.setLength(0);
						key1.append(_matrix.get(i-2).get(i2)._tag);
						key1.append("_");
						key1.append(_matrix.get(i-1).get(i1)._tag);
						transFreq = _hmm.getFreq(_hmm.get_trans(), key1.toString(), _matrix.get(i).get(j)._tag);
						
						key1.setLength(0);
						key1.append(_matrix.get(i-1).get(i1)._tag);
						key1.append("_");
						key1.append(_atomList.get(i-1));
						key1.append("_");
						key1.append(_matrix.get(i).get(j)._tag);
						emitFreq1 = _hmm.getFreq(_hmm.get_emit_two(), key1.toString(), _atomList.get(i));
						
						key1.setLength(0);
						key1.append(_matrix.get(i-1).get(i1)._tag);
						key1.append("_");
						key1.append(_matrix.get(i).get(j)._tag);
						emitFreq2 = _hmm.getFreq(_hmm.get_emit_two(), key1.toString(), _atomList.get(i));
						
						emitFreq = Math.max(emitFreq1, emitFreq2);
						
						if (transFreq + emitFreq + _matrix.get(i-1).get(i1)._total > maxTotal) {
							Node tmpNode = new Node(_matrix.get(i).get(j));
							maxTotal = transFreq + emitFreq + _matrix.get(i-1).get(i1)._total;
							tmpNode._total = maxTotal;
							tmpNode._best_prev = i1;
							_matrix.get(i).set(j, tmpNode);
						}
					}
				}
			}
		}
	}
	
	private void buildBiGramPath() {
		StringBuffer key1 = new StringBuffer();
		double transFreq = 0.0;
		double emitFreq = 0.0;
		double emitFreq1, emitFreq2;
		double maxTotal = Const.MIN_FREQ;
		int i = 0;
		int j = 0;
		int i1 = 0;
		for (i = Const.NGRAM - 1; i < _matrix.size(); i++) {
			for (j = 0; j < _matrix.get(i).size(); j++) {
				maxTotal = Const.MIN_FREQ;
				for (i1 = 0; i1 < _matrix.get(i-1).size(); i1++) {
					key1.setLength(0);
					key1.append(_matrix.get(i-1).get(i1)._tag);
					transFreq = _hmm.getFreq(_hmm.get_trans(), key1.toString(), _matrix.get(i).get(j)._tag);

					key1.setLength(0);
					key1.append(_matrix.get(i-1).get(i1)._tag);
					key1.append("_");
					key1.append(_atomList.get(i-1));
					key1.append("_");
					key1.append(_matrix.get(i).get(j)._tag);
					emitFreq1 = _hmm.getFreq(_hmm.get_emit_two(), key1.toString(), _atomList.get(i));

					key1.setLength(0);
					key1.append(_matrix.get(i-1).get(i1)._tag);
					key1.append("_");
					key1.append(_matrix.get(i).get(j)._tag);
					emitFreq2 = _hmm.getFreq(_hmm.get_emit_two(), key1.toString(), _atomList.get(i));

					emitFreq = Math.max(emitFreq1, emitFreq2);
					
					if (_matrix.get(i-1).get(i1)._total + transFreq + emitFreq > maxTotal) {
						Node tmpNode = new Node(_matrix.get(i).get(j));
						maxTotal = transFreq + emitFreq + _matrix.get(i-1).get(i1)._total;
						tmpNode._total = maxTotal;
						tmpNode._best_prev = i1;
						_matrix.get(i).set(j, tmpNode);
					}
				}
			}
		}
	}
	
	/**
	 * 回溯切分路径，输出最佳结果
	 */
	private void backTrack(ArrayList<String> wordList, ArrayList<String> posList) {
		int nPosition = _matrix.size() - Const.NGRAM + 1;
		int nBest = _matrix.get(nPosition).get(0)._best_prev;
		Stack<String> posStack = new Stack<String>();
		while(nBest >= 0  &&  nPosition >= Const.NGRAM - 1)
		{
			posStack.add(_matrix.get(nPosition-1).get(nBest)._tag);
			System.out.println(_matrix.get(nPosition-1).get(nBest)._tag);
			nBest = _matrix.get(nPosition-1).get(nBest)._best_prev;
			nPosition--;
		}
		int nBegin = Const.NGRAM - 1;
		int nEnd = nBegin;

		while(!posStack.empty())
		{
			String pos = posStack.pop();
			if(pos.startsWith("S") || pos.startsWith("E"))
			{
				StringBuffer word = new StringBuffer();
				while(nBegin < nEnd)
				{
					word.append(_atomList.get(nBegin));
					nBegin++;
				}
				wordList.add(word.toString());
				if (_hmm.get_customedDict().containsKey(word.toString())) {
					posList.add(_hmm.get_customedDict().get(word.toString()));
				} else {
					posList.add(pos.substring(2));
				}
//				System.out.print(word.toString() + "/" + pos.substring(2) + "  ");
			}
			nEnd++;
		}
	}
	
	/**
	 * 打印各个内部函数的运行时间
	 */
	private void printDebugInfo() {
		System.out.println("runtime of atomSegment: " + runTime_atomSegmentation);
		System.out.println("runtime of buildMatrix: " + runTime_buildMatrix);
		System.out.println("runtime of buildPath: " + runTime_buildPath);
		System.out.println("runtime of backTrack: " + runTime_backTrack);
	}
	
	/**
	 * 添加自定义词典
	 * @param dishFile
	 * @param innerWeight 自定义词语内部概率
	 * @param outerWeight 自定义词语与默认词典中词语间的概率
	 */
	private void addDict(String dishFile, int maxLine, double innerWeight, double outerWeight) {
//		_customedWordMap.clear();
//		FReader reader = new FReader(dishFile);
//		String line = "";
//		while ((line = reader.readLine()) != null) {
//			line = line.trim();
//			if (line.isEmpty()) {
//				continue;
//			}
//			String[] items = line.split("\\/");
//			assert(items.length == 2);
//			_customedWordMap.put(items[0], items[1]);
//		}
//		reader.close();
		_hmm.addDict(dishFile, maxLine, innerWeight, outerWeight);
	}
	
	/**
	 * 对外接口, 对句子分词，返回TermList
	 * @param sentence
	 * @return
	 */
	public List<Term> getTermList(String sentence) {
		List<Term> resultList = new ArrayList<Term>();
		ArrayList<String> termList = new ArrayList<String>();
		ArrayList<String> posList = new ArrayList<String>();
		segmentSentence(sentence, termList, posList);
		for (int i = 0; i < termList.size(); i++) {
			Term term = new Term(termList.get(i), posList.get(i), i);
			resultList.add(term);
		}
		return resultList;
	}
	
	/**
	 * 对外接口，打印TermList
	 * @param termList
	 * @param nType
	 */
	public void printTermList(List<Term> termList, int nType) {
		switch (nType) {
		case 1:
			 for (Term term : termList) {
				 System.out.print("<id: " + term.getPosition() + ">\t");
				 System.out.print("<word: " + term.getWord() + ">\t");
				 System.out.println("<pos: " + term.getPos() + ">\t");
			 }
			break;
		case 2:
			for (Term term : termList) {
				 System.out.print(term.getWord() + " ");
			 }
			System.out.println();
			break;
		case 3:
			for (Term term : termList) {
				 System.out.print(term.getWord() + "/" + term.getPos() + " ");
			 }
			System.out.println();
			break;

		default:
			break;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Segmentation segmentation = new Segmentation(false, true);
		String sentence = "温家宝同志喜欢吃鱼香肉丝锅包肉酱骨架";

		long startTime = System.currentTimeMillis();
		List<Term> termList = segmentation.getTermList(sentence);
		segmentation.printTermList(termList, 3);
		long stopTime = System.currentTimeMillis();
		System.out.println("total time : " + (stopTime - startTime) + " millisecond" );
	}
}
