package com.khnsoft.schperfectmap.DecisionTree;

import java.io.*;
import java.util.*;

public class train
{
	public static void main(String [] args) throws Exception
	{
		System.out.println("JAVA> training start");
		String dataPath = args[0];
		System.out.println("data path = " + dataPath);

		realDecisionTree m = new realDecisionTree("mymodel", dataPath, dataPath);

		m.generateItemSet();
		System.out.println("ItemSet generated");

		int totalAttrs = m.getNumberOfAttributes();
		int [] attrs = new int [totalAttrs - 1];
		for(int i = 0; i < attrs.length; ++i)
			attrs[i] = i;
		System.out.println("# of features = " + attrs.length);
		m.setTestAttributesByAttributeIndices(attrs);
		String targetLabel = "location";
		m.setGoalAttribute(targetLabel);
		System.out.println("Target label name = " + targetLabel);

		m.generateDecisionTree();
		System.out.println("Tree generated");

		double [] testAccuracy = m.TestWithTrainingData();
		for(int i = 0; i < testAccuracy.length; ++i) {
			System.out.println("Class " + i + "(" +
					//m.getClassName(i) + 
					") accuracy = " + testAccuracy[i]);
		}

		System.out.println("JAVA> training end");
		String modelPath = args[1];
		BufferedWriter bw = new BufferedWriter(new FileWriter(modelPath));
		bw.write(m.getExpressionOfDT());
		bw.close();
		System.out.println("JAVA> tree saved to " + modelPath);

		//BufferedReader br = new BufferedReader(new FileReader(modelPath));
		FileReader fr = new FileReader(modelPath);
		String m1_load = "";
		int tmp;
		while (true) {
			//String tempLine = br.readLine();
			tmp = fr.read();
			if(tmp < 0)
				break;
			m1_load += (char)tmp; //tempLine;
		}
		fr.close();
		System.out.println("m1_load ----------------------------");
		System.out.println(m1_load);
		System.out.println("------------------------------------");

		//realDecisionTree m2 = new realDecisionTree("m2", m.getExpressionOfDT());
		realDecisionTree m2 = new realDecisionTree("m2", m1_load);
		m2.setGoalAttribute();
		bw = new BufferedWriter(new FileWriter(modelPath + "2"));
		bw.write(m2.getExpressionOfDT());
		bw.close();

		m2.setTrainTestFile(dataPath, dataPath);
		//m2.generateItemSet();
		m2.setItemSet();
		m2.resetHash();
		testAccuracy = m2.TestWithTrainingData();
		for(int i = 0; i < testAccuracy.length; ++i) {
			System.out.println("Class " + i + "(" +
					//m.getClassName(i) + 
					") accuracy = " + testAccuracy[i]);
		}

		Vector<String> rawAttributes = 
			new Vector<String>(Arrays.asList(
			"a0:f8:49:ee:ed:6e",
			"88:36:6c:8e:4b:60",
			"a0:f8:49:ee:ec:80",
			"88:36:6c:34:3a:18",
			"88:36:6c:5d:19:38",
			"90:9f:33:d1:fc:a8",
			"86:25:19:16:1c:8c",
			"a0:f8:49:ee:ed:6f",
			"90:9f:33:e7:f1:56",
			"88:36:6c:6f:eb:58",
			"a0:f8:49:ee:ee:a0",
			"a0:f8:49:ee:ea:a1",
			"a0:f8:49:ee:ec:21",
			"f4:28:53:71:06:94",
			"88:36:6c:34:41:08",
			"a0:f8:49:ee:ee:af",
			"a0:f8:49:ee:e9:ee",
			"90:9f:33:a9:62:96",
			"a0:f8:49:ee:ec:8e",
			"a0:f8:49:ee:ef:c0",
			"90:9f:33:91:73:2c",
			"88:36:6c:48:66:5c",
			"a0:f8:49:ee:ec:81",
			"a0:f8:49:ee:ed:61",
			"a0:f8:49:ee:d7:00",
			"a0:f8:49:ee:ee:ae",
			"a0:f8:49:ee:d7:01",
			"88:36:6c:6e:5c:f4",
			"a0:f8:49:ee:e9:ef",
			"88:36:6c:8e:53:f8",
			"32:cd:a7:f1:02:1d",
			"a0:f8:49:ee:ee:a1",
			"90:9f:33:d4:20:04",
			"a0:f8:49:ee:ec:8f"
			)); 
		Vector<Double> rawValues = 
			new Vector<Double>(Arrays.asList(
			-85.0,
			-56.0,
			-67.0,
			-73.0,
			-57.0,
			-76.0,
			-58.0,
			-85.0,
			-88.0,
			-87.0,
			-81.0,
			-82.0,
			-93.0,
			-63.0,
			-74.0,
			-74.0,
			-78.0,
			-79.0,
			-67.0,
			-89.0,
			-68.0,
			-73.0,
			-66.0,
			-86.0,
			-95.0,
			-74.0,
			-95.0,
			-82.0,
			-78.0,
			-79.0,
			-72.0,
			-81.0,
			-87.0,
			-67.0
			)); 

		String ret = m2.TestWithRealTimeData(rawAttributes, rawValues);
		System.out.println(ret);

		/*
		 *
			"a0:f8:49:ee:ed:6e": -85,
			"88:36:6c:8e:4b:60": -56,
			"a0:f8:49:ee:ec:80": -67,
			"88:36:6c:34:3a:18": -73,
			"88:36:6c:5d:19:38": -57,
			"90:9f:33:d1:fc:a8": -76,
			"86:25:19:16:1c:8c": -58,
			"a0:f8:49:ee:ed:6f": -85,
			"90:9f:33:e7:f1:56": -88,
			"88:36:6c:6f:eb:58": -87,
			"a0:f8:49:ee:ee:a0": -81,
			"a0:f8:49:ee:ea:a1": -82,
			"a0:f8:49:ee:ec:21": -93,
			"f4:28:53:71:06:94": -63,
			"88:36:6c:34:41:08": -74,
			"a0:f8:49:ee:ee:af": -74,
			"a0:f8:49:ee:e9:ee": -78,
			"90:9f:33:a9:62:96": -79,
			"a0:f8:49:ee:ec:8e": -67,
			"a0:f8:49:ee:ef:c0": -89,
			"90:9f:33:91:73:2c": -68,
			"88:36:6c:48:66:5c": -73,
			"a0:f8:49:ee:ec:81": -66,
			"a0:f8:49:ee:ed:61": -86,
			"a0:f8:49:ee:d7:00": -95,
			"a0:f8:49:ee:ee:ae": -74,
			"a0:f8:49:ee:d7:01": -95,
			"88:36:6c:6e:5c:f4": -82,
			"a0:f8:49:ee:e9:ef": -78,
			"88:36:6c:8e:53:f8": -79,
			"32:cd:a7:f1:02:1d": -72,
			"a0:f8:49:ee:ee:a1": -81,
			"90:9f:33:d4:20:04": -87,
			"a0:f8:49:ee:ec:8f": -67
	
		*/
	}
}
