package com.dianping.algorithm.dp_review_mining.segment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HMM {
	
	private ArrayList<String> allFiles = new ArrayList<String>();
	private HashSet<String> allDelimiters = new HashSet<String>();
	
	
	//用于保存转移概率与发射概率
	private HashMap<String, HashMap<String, Double>> _trans = new HashMap<String, HashMap<String,Double>>();
	private HashMap<String, HashMap<String, Double>> _emit = new HashMap<String, HashMap<String,Double>>();
	private HashMap<String, HashMap<String, Double>> _emit_two = new HashMap<String, HashMap<String,Double>>();
	
	private HashMap<String, String> _customedDict = new HashMap<String, String>();
	
	private Pattern pattern = Pattern.compile("\\b(.+?\\/.+?)\\b", Pattern.DOTALL);
	StringBuffer sb = new StringBuffer();
	private HashSet<String> tmpEmitSet = null;  //保存添加自定义词典之前的_emit.keys()的副本
	private HashSet<String> tmpEmit2Set = null; //保存添加自定义词典之前的_emit2.keys()的副本
	private HashSet<String> tmpTransSet = null; //保存添加自定义词典之前的_trans.keys()的副本
	
	
	public HashMap<String, HashMap<String, Double>> get_trans() {
		return _trans;
	}

	public HashMap<String, HashMap<String, Double>> get_emit() {
		return _emit;
	}

	public HashMap<String, HashMap<String, Double>> get_emit_two() {
		return _emit_two;
	}

	/**
	 * 得到root目录（包含子目录）下所有的文件名，存入allFiles中。
	 * @param root
	 */
	private void getAllFilesInPath(String root) {
		if (root == null || root.trim().isEmpty())
			return;
	    File dir = new File(root); 
        File[] fileList = dir.listFiles(); 
        if (fileList == null) 
            return; 
        for (int i = 0; i < fileList.length; i++) { 
            if (fileList[i].isDirectory()) { 
                getAllFilesInPath(fileList[i].getAbsolutePath()); 
            } else { 
                String strFileName;
				try {
					strFileName = fileList[i].getCanonicalPath();
	                allFiles.add(strFileName); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}                 
            } 
        } 
	}
	
	public HashMap<String, String> get_customedDict() {
		return _customedDict;
	}

	public void loadFile(String fileName) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "gbk"));
			String line = "";
			ArrayList<String> termList = new ArrayList<String>();
			ArrayList<String> posList = new ArrayList<String>();
			int i = 0;
			pushTag(termList, posList, Const.BEG);
			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
				if (line.trim().isEmpty()) {
					continue;
				}
				line = line.substring(line.indexOf("  ") + 1);
//				System.out.println(line);
				
				Matcher matches = pattern.matcher(line);
				while (matches.find()) {
					String group = matches.group().replaceAll("\\[", "");
					group = group.replaceAll("\\].{1,4}", "");
					group = group.trim();
//					System.out.println(group);
					String[] items = group.split("\\/");
					assert(items.length == 2);
//					System.out.println(items[0] + "\t" + items[1]);
//					posList.add(items[1]);
//					termList.add(items[0]);
					if (items[0].length() == 1) {
						termList.add(items[0]);
						posList.add("S_" + items[1]);
					} else if (items[0].length() == 2) {
						termList.add(String.valueOf(items[0].charAt(0)));
						posList.add("B_" + items[1]);
						termList.add(String.valueOf(items[0].charAt(1)));
						posList.add("E_" + items[1]);
					} else if (items[0].length() >= 3) {
						termList.add(String.valueOf(items[0].charAt(0)));
						posList.add("B_" + items[1]);
						int nLen = items[0].length();
						for (i = 1; i < nLen - 1; i++) {
							termList.add(String.valueOf(items[0].charAt(i)));
							posList.add("M_" + items[1]);
						}
						termList.add(String.valueOf(items[0].charAt(i)));
						posList.add("E_" + items[1]);
					}
				}
				assert(termList.size() == posList.size());
				pushTag(termList, posList, Const.END);
//				System.out.println("termList.size(): " + termList.size());
//				for (int j = 0; j < termList.size(); j++) {
//					System.out.println(termList.get(j) + "\t" + posList.get(j));
//				}
				updateModel(termList, posList);
				termList.clear();
				posList.clear();
				pushTag(termList, posList, Const.BEG);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新模型，每次从语料中读入一行后调用
	 * @param termList, 单词
	 * @param posList, 词性
	 */
	private void updateModel(ArrayList<String> termList, ArrayList<String> posList) {
		assert(termList.size() == posList.size());
		int size = termList.size();
		int i = Const.NGRAM - 1;
		
		for (; i < size - Const.NGRAM + 1; i++) {
			insertMap(_emit, termList.get(i), posList.get(i), 1.0); //
//			System.out.println("_emit\t" + posList.get(i) + "\t" + termList.get(i) + "\t" + 1.0);
//			StringBuffer sb = new StringBuffer();
			sb.setLength(0);
			sb.append(posList.get(i-1));
			sb.append("_");
			sb.append(posList.get(i));
			insertMap(_emit_two, sb.toString(), termList.get(i), 1.0);
//			System.out.println("_emit_two\t" + sb.toString() + "\t" + termList.get(i) + "\t" + 1.0);
			sb.delete(0, sb.length());
			sb.append(posList.get(i-1));
			sb.append("_");
			sb.append(termList.get(i-1));
			sb.append("_");
			sb.append(posList.get(i));
			insertMap(_emit_two, sb.toString(), termList.get(i), 1.0);
//			System.out.println("_emit_two\t" + sb.toString() + "\t" + termList.get(i) + "\t" + 1.0);
			sb.delete(0, sb.length());
			for (int j = i-Const.NGRAM+1; j < i; j++) {
				sb.append(posList.get(j));
				if (j < i - 1) {
					sb.append("_");
				}
			}
			insertMap(_trans, sb.toString(), posList.get(i), 1.0);
//			System.out.println("_trans\t" + sb.toString() + "\t" + posList.get(i) + "\t" + 1.0);
		}
		assert(termList.get(i).equals(Const.END) && termList.get(i+1).equals(Const.END));
		
		for(; i < posList.size(); i++)
		{
//			StringBuffer sb = new StringBuffer();
			sb.setLength(0);
			for(int j = i - Const.NGRAM + 1; j < i; j++)
			{
				sb.append(posList.get(j)); 
				if(j < i - 1)
					sb.append("_");
			}
			insertMap(_trans, sb.toString(), posList.get(i), 1.0);
//			System.out.println("_trans\t" + sb.toString() + "\t" + posList.get(i) + "\t" + 1.0);
		}
	}
	
	/**
	 * 
	 * @param dest
	 * @param key1
	 * @param key2
	 * @param freq
	 */
	private void insertMap(HashMap<String, HashMap<String, Double>> dest, String key1, String key2, double freq) {
		if (dest == null) 
			return;
		if (dest.containsKey(key1)) {
			HashMap<String, Double> tmpMap = dest.get(key1);
			if (tmpMap.containsKey(key2)) {
				tmpMap.put(key2, tmpMap.get(key2) + freq);
			} else {
				tmpMap.put(key2, freq);
			}
		} else {
			HashMap<String, Double> tmpMap = new HashMap<String, Double>();
			tmpMap.put(key2, freq);
			dest.put(key1, tmpMap);
		}
	}
	
	private void pushTag(ArrayList<String> termList, ArrayList<String> posList, String tag) {
		for (int i = 1; i < Const.NGRAM; i++) {
			termList.add(tag);
			posList.add(tag);
		}
	}
	
	
	private void normalization() {
		normalizeMap(_emit);
		normalizeMap(_emit_two);
		normalizeMap(_trans);
		
	}
	
	private void normalizeMap(HashMap<String, HashMap<String, Double>> map) {
		for (Map.Entry<String, HashMap<String, Double>> entry1 : map.entrySet()) {
			double total = 0.0;
			for (double  freq : entry1.getValue().values()) {
				total += freq;
			}
			for (String  key2 : entry1.getValue().keySet()) {
				entry1.getValue().put(key2, entry1.getValue().get(key2)/total);
			}
		}
	}
	
	/**
	 * 从语料生成模型
	 * @param root, 存放语料的根目录
	 */
	public void train(String corpusRoot/*, String dictFileName, double weight*/) {
		getAllFilesInPath(corpusRoot);
		for (String fileName : allFiles) {
			System.out.println(fileName);
			loadFile(fileName);
		}
		System.out.print("trainning");
		normalization();
		writeModel();
		System.out.println("done!");
	}
	
	/**
	 * 读入模型文件
	 * @param fileName
	 */
	public void readModel() {
		this._emit.clear();
		this._emit_two.clear();
		this._trans.clear();
		System.out.print("readModel()");
		readFromFile(_emit, "src/main/resources/data/emit.dat");
		readFromFile(_emit_two, "src/main/resources/data/emit_two.dat");
		readFromFile(_trans, "src/main/resources/data/trans.dat");
		tmpEmitSet = new HashSet<String>(_emit.keySet());
		tmpEmit2Set = new HashSet<String>(_emit_two.keySet());
		tmpTransSet = new HashSet<String>(_trans.keySet());
		System.out.println(" done!");
	}
	
	public void readFromFile(HashMap<String, HashMap<String, Double>> map, String fileName) {
		DataInputStream dis = null;
		long oldTime = System.currentTimeMillis();
		long currTime;
		try {
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			while (dis.available() > 0) {
				currTime = System.currentTimeMillis();
				if (currTime - oldTime > 500) {
					System.out.print(".");
					oldTime = currTime;
				}
				String key1 = dis.readUTF();
				String key2 = dis.readUTF();
				double freq = dis.readDouble();
				insertMap(map, key1, key2, freq);
			}
			dis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 写入模型文件
	 * @param fileName
	 */
	public void writeModel() {
		writeToFile(_emit, ".\\data\\emit.dat");
		writeToFile(_emit_two, ".\\data\\emit_two.dat");
		writeToFile(_trans, ".\\data\\trans.dat");
	}
	
	private void writeToFile(HashMap<String, HashMap<String, Double>> map, String fileName) {
		DataOutputStream dos = null;
		long oldTime = System.currentTimeMillis();
		try {
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			for (Map.Entry<String, HashMap<String, Double>> entry1 : map.entrySet()) {
				for (Map.Entry<String, Double>  entry2 : entry1.getValue().entrySet()) {
//					System.out.print(fileName + "\t");
//					System.out.println(entry1.getKey() + "\t" + entry2.getKey() + "\t" + Math.log(entry2.getValue()));
					long currTime = System.currentTimeMillis();
					if (currTime - oldTime > 1000) {
						System.out.print(".");
						oldTime = currTime;
					}
					dos.writeUTF(entry1.getKey());
					dos.writeUTF(entry2.getKey());
					dos.writeDouble(Math.log(entry2.getValue()));
				}
			}
			dos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	public double getFreq(HashMap<String, HashMap<String, Double>> taget, String key1, String key2) {
		if (taget == null || taget.isEmpty()) {
			return Const.SMOOTHING_FREQ;
		}
		if (taget.containsKey(key1)) {
			HashMap<String, Double> entry = taget.get(key1);
			if (taget.get(key1).containsKey(key2)) {
				return entry.get(key2);
			} else {
				return Const.SMOOTHING_FREQ;
			}
		} else {
			return Const.SMOOTHING_FREQ;
		}
//		try {
//			return taget.get(key1).get(key2);
//		} catch (Exception e) {
//			// TODO: handle exception
//			return MIN_FREQ;
//		}
	}
	
	public boolean existFreq(int position, String key) {
		return _emit.containsKey(key);
	}
	
	/**
	 * 添加用户词典
	 * @param dictFile
	 */
	public void addDict(String dictFile, int maxLine, double innerWeight, double outerWeight) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dictFile)), "utf-8"));
			String line = "";
			ArrayList<String> termList = new ArrayList<String>();
			ArrayList<String> posList = new ArrayList<String>();
			int i = 0;
			pushTag(termList, posList, Const.BEG);
			int nLine = 0;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					return;
				}
				nLine++;
				System.out.println(nLine);
				if (maxLine > 0 && nLine >= maxLine) {
					break;
				}
				String[] items = line.split("\\/");
				assert(items.length == 2);
				_customedDict.put(items[0], items[1]);
				if (items[0].length() == 1) {
					termList.add(items[0]);
					posList.add("S_" + items[1]);
				} else if (items[0].length() == 2) {
					termList.add(String.valueOf(items[0].charAt(0)));
					posList.add("B_" + items[1]);
					termList.add(String.valueOf(items[0].charAt(1)));
					posList.add("E_" + items[1]);
				} else if (items[0].length() >= 3) {
					termList.add(String.valueOf(items[0].charAt(0)));
					posList.add("B_" + items[1]);
					int nLen = items[0].length();
					for (i = 1; i < nLen - 1; i++) {
						termList.add(String.valueOf(items[0].charAt(i)));
						posList.add("M_" + items[1]);
					}
					termList.add(String.valueOf(items[0].charAt(i)));
					posList.add("E_" + items[1]);
				}
				assert(termList.size() == posList.size());
				pushTag(termList, posList, Const.END);
//				System.out.println("termList.size(): " + termList.size());
//				for (int j = 0; j < termList.size(); j++) {
//					System.out.println(termList.get(j) + "\t" + posList.get(j));
//				}
				updateModelEx(termList, posList, innerWeight, outerWeight);
				termList.clear();
				posList.clear();
				pushTag(termList, posList, Const.BEG);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 打印Map信息，调试时使用。
	 * @param dest
	 * @param key1
	 * @param key2
	 */
	public void printMap(HashMap<String, HashMap<String, Double>> dest, String key1, String key2) {
		if (key1 == null || key1.isEmpty()) {
			return;
		}
		HashMap<String, Double> theMap = dest.get(key1);
		if (key2 == null || key2.isEmpty()) {
			System.out.println(key1 + ":");
			for (Map.Entry<String, Double> entry: theMap.entrySet()) {
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			}
			System.out.println();
		} else {
			System.out.println(key1 + "\t" + key2 + "\t" + theMap.get(key2));
		}
	}
	
	
	/**
	 * dest 中存放的是实际次数取log
	 * @param dest
	 * @param key1
	 * @param key2
	 * @param freq 
	 */
	private void insertMapEx(HashMap<String, HashMap<String, Double>> dest, String key1, String key2, double freq) {
		if (dest == null) 
			return;
		if (dest.containsKey(key1)) {
			HashMap<String, Double> tmpMap = dest.get(key1);
			if (tmpMap.containsKey(key2)) {
//				System.out.println("before: " + tmpMap.get(key2));
				tmpMap.put(key2, Math.log(Math.exp(tmpMap.get(key2)) + freq));
//				System.out.println("after: " + Math.log(Math.exp(tmpMap.get(key2)) + freq));
			} else {
				tmpMap.put(key2, Math.log(freq));
			}
		} else {
			HashMap<String, Double> tmpMap = new HashMap<String, Double>();
			tmpMap.put(key2, Math.log(freq));
			dest.put(key1, tmpMap);
		}
	}
	
	/**
	 * 添加用户词典时调用
	 * @param termList
	 * @param posList
	 * @param weight
	 */
	private void updateModelEx(ArrayList<String> termList, ArrayList<String> posList, double innerWeight, double outerWeight) {
		assert(termList.size() == posList.size());
		int size = termList.size();
		int i = Const.NGRAM - 1;
		int position = -1;
		//设置从训练集中其他词语到自定义词语首的概率
//		for (String key1 : tmpEmit2Set) {
//			if (key1.startsWith("S") || key1.startsWith("E")) {
//				String str = key1.substring(0, key1.lastIndexOf("_") - 1) + posList.get(i);
//				insertMapEx(_emit_two, str, termList.get(i), outerWeight);
////				System.out.println("_emit_two\t" + str + "\t" + termList.get(i) + "\t" + outerWeight);
//			}
//		}
//		for (String key1 : tmpTransSet) {
//			insertMapEx(_trans, key1, posList.get(i), outerWeight);
//			position = key1.lastIndexOf("S");
//			position = Math.max(position, key1.lastIndexOf("E"));
//			if (position > 0) {
//				key1 = key1.substring(position);
//				insertMapEx(_trans, key1, posList.get(i), outerWeight);
//			}
//		}
		//更新自定义词语内部的概率
		for (; i < size - Const.NGRAM + 1; i++) {
			insertMapEx(_emit, termList.get(i), posList.get(i), innerWeight); //
//			System.out.println("_emit\t" + posList.get(i) + "\t" + termList.get(i) + "\t" + innerWeight);
			
			sb.setLength(0);
			sb.append(posList.get(i-1));
			sb.append("_");
			sb.append(posList.get(i));
			insertMapEx(_emit_two, sb.toString(), termList.get(i), innerWeight);
//			System.out.println("_emit_two\t" + sb.toString() + "\t" + termList.get(i) + "\t" + innerWeight);
			sb.setLength(0);
			sb.append(posList.get(i-1));
			sb.append("_");
			sb.append(termList.get(i-1));
			sb.append("_");
			sb.append(posList.get(i));
			insertMapEx(_emit_two, sb.toString(), termList.get(i), innerWeight);
//			System.out.println("_emit_two\t" + sb.toString() + "\t" + termList.get(i) + "\t" + innerWeight);
			
			sb.setLength(0);
			for (int j = i-Const.NGRAM+1; j < i; j++) {
				sb.append(posList.get(j));
				if (j < i - 1) {
					sb.append("_");
				}
			}
			insertMapEx(_trans, sb.toString(), posList.get(i), innerWeight);
//			System.out.println("\t" + "_trans\t" + sb.toString() + "\t" + posList.get(i) + "\t" + innerWeight);
		}
//		assert(termList.get(i).equals(Const.END) && termList.get(i+1).equals(Const.END));
		//设置从自定义词尾到训练集中其他词语的概率
//		for (String outerTerm: tmpEmitSet) {
//			for (String outerPos : _emit.get(outerTerm).keySet()) {
//				if (!outerPos.startsWith("S") && !outerPos.startsWith("B")) {
//					continue; 
//				}
//				String key = posList.get(i-1) + "_" + outerPos;
//				insertMapEx(_emit_two, key, outerTerm, outerWeight);
////				System.out.println("_emit_two" + key + "\t" + outerTerm + "\t" + outerWeight); 
//				key = posList.get(i-1) + "_" + termList.get(i-1) + "_" + outerPos;
//				insertMapEx(_emit_two, key, outerTerm, outerWeight);
////				System.out.println("_emit_two" + key + "\t" + outerTerm + "\t" + outerWeight); 
//			}
//		}
//		
//		sb.setLength(0);
//		for (int j = i-Const.NGRAM+1; j < i; j++) {
//			sb.append(posList.get(j));
//			if (j < i - 1) {
//				sb.append("_");
//			}
//		}
//		position = -1;
//		for (String key2 : tmpTransSet) {
//			if (!key2.startsWith("S") && !key2.startsWith("B")) {
//				continue; 
//			}
//			position = key2.indexOf('_', key2.indexOf('_')+1);
//			if (position < 0) {
//				continue;
//			}
//			key2 = key2.substring(0, position); 
//			insertMapEx(_trans, sb.toString(), key2, outerWeight);
////			System.out.println("_trans\t" + sb.toString() + "\t" + key2+ "\t" + outerWeight);
//		}
//		
//		for(; i < posList.size(); i++)
//		{
//			sb.setLength(0);
//			for(int j = i-Const.NGRAM+1; j < i; j++)
//			{
//				sb.append(posList.get(j)); 
//				if(j < i - 1)
//					sb.append("_");
//			}
//			insertMapEx(_trans, sb.toString(), posList.get(i), outerWeight);
////			System.out.println("_trans\t" + sb.toString() + "\t" + posList.get(i) + "\t" + outerWeight);
//		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		HMM hmm = new HMM();
//		hmm.train(".\\data\\pd-corpus\\"/*, ".\\data\\testDishName.txt", 100000.0*/);
//		HashMap<String, HashMap<String, Double>> _emit = new HashMap<String, HashMap<String,Double>>();
//		hmm.readFromFile(_emit, ".\\data\\emit.dat");

		
//		HMM hmm = new HMM();
//		hmm.readModel();
//		hmm.printMap(hmm.get_emit(), "和", "S_p");
		
		Segmentation segmentation = new Segmentation(false, true);
		String sentence = "温家宝同志喜欢吃鱼香肉丝、锅包肉和酱骨架";
		long startTime = System.currentTimeMillis();
		List<Term> termList = segmentation.getTermList(sentence);
		segmentation.printTermList(termList, 1);
		long stopTime = System.currentTimeMillis();
		System.out.println("total time : " + (stopTime - startTime) + " millisecond" );

	}
}
