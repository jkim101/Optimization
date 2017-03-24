package configsToBOM;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import utilities.FileWriter;
import utilities.TSVFileReader;

public class usageTool {
	public BitSet[] partWiseFtrBitSet;
	public ArrayList<String> fullOrderdPartList;
	public ArrayList<String> partListHeader;
	public HashMap<String, ArrayList<String>> partToFtrList;
	public  HashMap<String, Integer> featureBitSetIndex;
	public HashMap<String, ArrayList<String>> commodityToPartsMap;
	public HashMap <String, String> partToCommodityMap;
	public ArrayList<String> commodityList;
	public HashMap<String, Double> commodityToHurtScoreMap;
	public HashMap<String, ArrayList<String>> summaryFeatureMap;
	public ArrayList<Integer> uniqueFamiliesForUsage;
	public HashMap<String, String> commodityDescriptionMap;

	public usageTool(TSVFileReader usageFile, HashMap<String, Integer> featureBitSetHeaderIndex, String partUsageFilePath, String commodityToPartFilePath, String commodityHurtFilePath, String summaryFeatureFilePath)
	{
		this.partToFtrList = new HashMap<String, ArrayList<String>>();
		this.featureBitSetIndex = featureBitSetHeaderIndex;
		this.fullOrderdPartList = new ArrayList<String>();
		this.partListHeader = new ArrayList<String>();
		this.commodityList = new ArrayList<String>();
		this.commodityToPartsMap = new HashMap<String, ArrayList<String>>();
		this.partToCommodityMap = new HashMap<String, String>();
		this.commodityDescriptionMap = new HashMap<String, String>();
		this.commodityToHurtScoreMap = new HashMap<String, Double>();
		this.summaryFeatureMap = new HashMap<String, ArrayList<String>>();
		buildCommodityToPartMap(commodityToPartFilePath);
		buildSummaryToPartMap(summaryFeatureFilePath);
		buildPartFtrListMapping(usageFile);
		buildPartWiseFtrBitSet(partUsageFilePath);
		buildHurtScoreMap(commodityHurtFilePath);
		usageFile.close();
	}

	private void buildSummaryToPartMap(String summaryFeatureFilePath) {
		TSVFileReader inputFile = new TSVFileReader(summaryFeatureFilePath);
		String[] line;
		System.out.println("---- Reading Summary Feature Mapping -----");
		while((line = inputFile.readLine()) != null)
		{
			
			String family = line[0];
			if(family.length() == 2)
			{
				family = family + "-";
			}
			String mappings = line[1].replace("]", "");
			mappings = mappings.replace("[", "|");
			String[]  mappingLine = mappings.split("\\|");
			String realFeature = mappingLine[0];
			for(int summaryIndex = 1; summaryIndex < mappingLine.length; summaryIndex++)
			{
				String summaryCode = family + mappingLine[summaryIndex];
				if(summaryFeatureMap.containsKey(summaryCode))
				{
					ArrayList<String> tmpMap = summaryFeatureMap.get(summaryCode);
					tmpMap.add(realFeature);
					summaryFeatureMap.put(summaryCode, tmpMap);
				}else
				{
					ArrayList<String> tmpMap = new ArrayList<String>();
					tmpMap.add(realFeature);
					summaryFeatureMap.put(summaryCode, tmpMap);
				}
				System.out.println("Summary Feature: " + summaryCode + " Real Feature: " + realFeature);
			}
		}
		
	}

	private void buildHurtScoreMap(String commodityHurtFilePath) {
		TSVFileReader inputFile = new TSVFileReader(commodityHurtFilePath);
		String[] line;
		System.out.println("---- Reading Commodity Family Hurt Score Mapping -----");
		while((line = inputFile.readLine()) != null)
		{
			commodityToHurtScoreMap.put(line[0].replace("-", "m"), Double.parseDouble(line[1]));
			//add description map if file has description column
			if(line.length > 2)
			{
				commodityDescriptionMap.put(line[0], line[2]);				
			}
		}
		System.out.println("---- DONE Reading Commodity Family Hurt Score Mapping DONE -----");
	}

	private void buildCommodityToPartMap(String commodityToPartFilePath) {
		TSVFileReader inputFile = new TSVFileReader(commodityToPartFilePath);
		String[] line;
		System.out.println("---- Reading Commodity Family To Part Mapping -----");
		while((line = inputFile.readLine()) != null)
		{
			String commodityFamily = line[0];
			String partName = line[1];
			partToCommodityMap.put(partName, commodityFamily);
			if(commodityToPartsMap.containsKey(commodityFamily))
			{
				ArrayList <String> tmpMap = commodityToPartsMap.get(commodityFamily);
				if(tmpMap.contains(partName))
				{
					System.out.println("Warning: " + partName + " Already in Commodity Map.  Check Input Data Quality.");
				}
				tmpMap.add(partName);
				commodityToPartsMap.put(commodityFamily, tmpMap);
			}else{
				ArrayList<String> tmpMap = new ArrayList<String>();
				tmpMap.add(partName);
				commodityToPartsMap.put(commodityFamily, tmpMap);
				commodityList.add(commodityFamily);
			}
		}
		System.out.println("---- DONE Reading Commodity Family To Part Mapping DONE -----");

	}

	private void buildPartFtrListMapping(TSVFileReader usageFile) {
		String[] line;
		int usageLine = 0;
		while ((line = usageFile.readLine()) != null)
		{
			String partPreBaseSuf = line[0] + "_" + line[1] + "_" + line[2];
			if(partToCommodityMap.containsKey(partPreBaseSuf))
			{
				String partPreBaseSufIndex = line[0] + "_" + line[1] + "_" + line[2] + usageLine;
				ArrayList<String> tempMap = new ArrayList<String>();
				partToFtrList.put(partPreBaseSufIndex, tempMap);
				for(int colIndex = 4; colIndex < line.length; colIndex++)
				{
					if(!line[colIndex].equals(""))
					{
						
						String partFromUsage = line[colIndex];
						
						tempMap.add(partFromUsage);
					}
				}				
			}else{
				System.out.println("Part: " + partPreBaseSuf + " Not in Commodity To Part Map");
			}
			usageLine++;
		}
	}
	
	

//	private String[] expandSummaryCodes(ArrayList<String> tempMap) {
//		boolean containsSummary = false;
//		HashMap <Integer, ArrayList<String>> validLines = new HashMap<Integer, ArrayList<String>>();
//		int expandedLine = 0;
//		for(int ftrIndex = 0; ftrIndex < tempMap.size(); ftrIndex++)
//		{
//			if(summaryFeatureMap.containsKey(tempMap.get(ftrIndex)))
//			{
//				containsSummary = true;
//				for(String subFtr : summaryFeatureMap.get(tempMap.get(ftrIndex)))
//				{
//					ArrayList<String> tempLst2 = new ArrayList<String>();
//					for(int ftrIndex2 = 0; ftrIndex2 < tempMap.size(); ftrIndex2++)
//					{
//						if(ftrIndex == ftrIndex2)
//						{
//							tempLst2.add(subFtr);
//						}else{							
//							tempLst2.add(tempMap.get(ftrIndex2));
//						}
//					}
//					validLines.put(expandedLine, tempMap);
//					expandedLine++;
//				}
//			}
//		}
//		
//	}

	private void buildPartWiseFtrBitSet(String usageFilePath) {
		TSVFileReader usageFile = new TSVFileReader(usageFilePath);
		partWiseFtrBitSet = new BitSet[partToFtrList.size()];
		int usageLine = 0; 
		int uniqueFtrIndex = 0;
		String[] line;
		uniqueFamiliesForUsage = new ArrayList<Integer>();
		while((line = usageFile.readLine()) != null)
		{
			String partPreBaseSuf = line[0] + "_" + line[1] + "_" + line[2];
			if(partToCommodityMap.keySet().contains(partPreBaseSuf))
			{
				
				partWiseFtrBitSet[usageLine] = new BitSet();
				partWiseFtrBitSet[usageLine].set(0, featureBitSetIndex.keySet().size(), false);
				String partPreBaseSufIndex = line[0] + "_" + line[1] + "_" + line[2] + "_" + usageLine;
				fullOrderdPartList.add(partPreBaseSufIndex);
				if(!partListHeader.contains(line[0] + "_" + line[1] + "_" + line[2]))
				{
					partListHeader.add(line[0] + "_" + line[1] + "_" + line[2]);
					uniqueFtrIndex++;
				}
				int uniqueFamilies = 0;
				for(int colIndex = 4; colIndex < line.length; colIndex++)
				{
					if(!line[colIndex].equals(""))
					{
						uniqueFamilies++;
						int bitSetHeaderIndex =0;
						if(featureBitSetIndex.containsKey(line[colIndex]))
						{
							bitSetHeaderIndex = featureBitSetIndex.get(line[colIndex]);
							partWiseFtrBitSet[usageLine].set(bitSetHeaderIndex, true);						
						}else{
							System.out.println("Ftr not found in feature dictionary\t" + line[colIndex] + "\t used in part\t" + partPreBaseSufIndex);
							
							if(summaryFeatureMap.containsKey(line[colIndex]))
							{
								System.out.println(line[colIndex] + " is summaryCode");
								for(String subFtr : summaryFeatureMap.get(line[colIndex]))
								{
									if(featureBitSetIndex.containsKey(subFtr))
									{
										bitSetHeaderIndex = featureBitSetIndex.get(subFtr);
										partWiseFtrBitSet[usageLine].set(bitSetHeaderIndex, true);									
									}
								}
								
							}else{
								partWiseFtrBitSet[usageLine].set(0, featureBitSetIndex.keySet().size(), false);
								break;
							}
							
						}
					}
				}
				uniqueFamiliesForUsage.add(uniqueFamilies);
				usageLine++;	
			}
		}
		usageFile.close();
	}
	
	public void writePartWiseFtrBitSet(String fullOutputFilePath)
	{
		FileWriter writer = new FileWriter(fullOutputFilePath, false);
		writer.write("partId\t");
		for(String ftrName : featureBitSetIndex.keySet())
		{
			writer.write(ftrName +"\t");
		}
		writer.write("\n");
		for(int partIndex = 0; partIndex < partWiseFtrBitSet.length; partIndex++)
		{
			writer.write(partIndex + "\t");
			for(String ftrName : featureBitSetIndex.keySet())
			{
				int ftrIndex = featureBitSetIndex.get(ftrName);
				if(partWiseFtrBitSet[partIndex].get(ftrIndex))
				{
					writer.write("1" +"\t");
				}else{
					writer.write("0" +"\t");
				}
			}
		writer.write("\n");
		}
		writer.close();
	}
}
