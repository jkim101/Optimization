package batchBuildModel;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;

import configsToBOM.usageTool;
import utilities.FileReader;
import utilities.FileWriter;

public class solutionBO {

	public HashMap<String, ArrayList<String>> batchConfig;
	public HashMap<String, ArrayList<String>> batchPartsUsed;
	public HashMap<String, String> configToBatch;
	public ArrayList<String> batchedCommodities;
	public Double totalPotentialHurt;
	public Double savedHurt;
	public HashMap<String, ArrayList<String>> uniquePartsByBatch;
	public solutionBO(String solFileLocation, boolean solutionSummary)
	{
		batchConfig = new HashMap<String, ArrayList<String>>();
		uniquePartsByBatch = new HashMap<String, ArrayList<String>>();
		configToBatch = new HashMap<String, String>();
		batchedCommodities = new ArrayList<String>();
		batchPartsUsed = new HashMap<String, ArrayList<String>>();
		if(solutionSummary)
		{
			readSolutionFile(solFileLocation);		
		}
		
	}

	private void readSolutionFile(String solFileLocation) {
		System.out.println(solFileLocation);
		FileReader solReader = new FileReader().setFileFullPath(solFileLocation).open();
		
		String line;
		String[] vals;
		
		while((line = solReader.readLine())!=null)
		{
			if(line.startsWith("  <variable "))
			{
				vals = line.split("\\\"");
				String varName = vals[1].split("#")[0];
				String varValue = vals[5];
//				System.out.println(varName + "\t" + varValue);
				String varSubString = varName.substring(0, 4);
				if(varSubString.equals("X_B_"))
				{
					batchConfigVar(varName, varValue);
				}else if(varSubString.equals("Flag") && varName.substring(0, 6).equals("Flag_C"))
				{
					commodityFlag(varName, varValue);
				}else if(varSubString.equals("F_B_"))
				{
					batchPartFlag(varName, varValue);
				}
			}
		}
		solReader.close();
	}

	private void batchPartFlag(String varName, String varValue) {
		if(Math.round(Double.parseDouble(varValue)) == 0)
		{
//			System.out.println(varName + "\t" + varValue + "\tSkipped");
			return;
		}
		String[] varDetails = varName.split("_");
		String batchId = varDetails[2];
		String partName = varDetails[4] + "_" + varDetails[5] + "_" + varDetails[6];
		if(batchPartsUsed.containsKey(batchId))
		{
			batchPartsUsed.get(batchId).add(partName);
		}else{
			ArrayList<String> tmpLst = new ArrayList<String>();
			tmpLst.add(partName);
			batchPartsUsed.put(batchId, tmpLst);
		}
		
	}

	private void commodityFlag(String varName, String varValue) 
	{
		if(Math.round(Double.parseDouble(varValue)) == 1)
		{
//			System.out.println(varName + "\t" + varValue + "\tSkipped");
			return;
		}
		String[] varDetails = varName.split("_");
		String comName = varDetails[2];
		batchedCommodities.add(comName);
	}

	private void batchConfigVar(String varName, String varValue) {
		if(Math.round(Double.parseDouble(varValue)) == 0)
		{
//			System.out.println(varValue + "\tSkipped");
			return;
		}
			
		String[] varDetails = varName.split("_");
		String configId = varDetails[4];
		String batchId = varDetails[2];
		if(batchConfig.containsKey(batchId))
		{
			batchConfig.get(batchId).add(configId);
		}else{
			ArrayList<String> tmpLst = new ArrayList<String>();
			tmpLst.add(configId);
			batchConfig.put(batchId, tmpLst);
		}
		configToBatch.put(configId,batchId);
	}

	public void writeTableauFile(ArrayList<String> bitFtrSetHeader,
			BitSet[] configWiseBitSet,
			HashMap<String, ArrayList<String>> famFtrMap,
			HashMap<Integer, Double> configTakeRate,
			ArrayList<String> partListHeader,
			HashMap<String, ArrayList<String>> commodityToPartsMap,
			BitSet[] configByPartBitSet, String outputPath) {
		// TODO Auto-generated method stub
		
		FileWriter writer = new FileWriter().setFileFullPath(outputPath).open();
		HashMap<String, ArrayList<String>> mapOfProblemParts = new HashMap<String, ArrayList<String>>();
		writer.write("ConfigId\tBatchId\tRate\t");
		for(String fam: famFtrMap.keySet())
		{
			writer.write(fam + "\t");
		}
		writer.write("\t");
		for(String commodity : commodityToPartsMap.keySet())
		{
			if(batchedCommodities.contains(commodity.replace("-","m")))
			{
				writer.write(commodity +"**\t");	
			}else{
				writer.write(commodity +"\t");				
			}
		}
		writer.write("\n");
		for(int configId = 0; configId < configByPartBitSet.length; configId++)
		{
			String configAsString = String.valueOf(configId);
			String batchId = configToBatch.get(configAsString);
			writer.write(configId +"\t");
			writer.write(batchId + "\t");
			Double takeRate = 0d;
			if(configTakeRate.containsKey(configId))
			{
				takeRate = configTakeRate.get(configId);
			}
			writer.write(takeRate + "\t");
			for(String fam: famFtrMap.keySet())
			{
				famFtrMap.get(fam);
				for(String ftr : famFtrMap.get(fam))
				{
					int indexFtr = bitFtrSetHeader.indexOf(ftr);
					if(configWiseBitSet[configId].get(indexFtr))
					{
						writer.write(ftr +"\t");
						continue;
					}
				}
			}
			for(String commodity : commodityToPartsMap.keySet())
			{
				writer.write("\t");
				int countPartsFromCommodityOnOrder = 0;
				for(String part : commodityToPartsMap.get(commodity))
				{
					int indexPart = partListHeader.indexOf(part);
					if(configByPartBitSet[configId].get(indexPart))
					{
						writer.write(part +",");
						countPartsFromCommodityOnOrder++;
						updateUniquePartByBatchList(batchId, commodity, part);
						if(countPartsFromCommodityOnOrder > 1)
						{
							flagProblemCommodity(mapOfProblemParts, commodity,
									part);
						}
						continue;
					}
				}
				if(countPartsFromCommodityOnOrder < 1)
				{
					flagProblemCommodity(mapOfProblemParts, commodity,
							"No Part For Some Configs");
				}
			}
			writer.write("\n");
		}
		writer.close();
		
		writeProblemPartsToScreen(mapOfProblemParts);
	}

	private void updateUniquePartByBatchList(String batchId, String commodity,
			String part) {
		if(batchedCommodities.contains(commodity.replace("-","m")))
		{
			if(uniquePartsByBatch.containsKey(batchId))
			{
				ArrayList<String> tmpList = uniquePartsByBatch.get(batchId);
				if(!tmpList.contains(part))
				{
					tmpList.add(part);					
				}
				uniquePartsByBatch.put(batchId, tmpList);
			}else{
				
				ArrayList<String> tmpList = new ArrayList<String>();
				tmpList.add(part);
				uniquePartsByBatch.put(batchId, tmpList);
			}
		}
	}

	private void flagProblemCommodity(
			HashMap<String, ArrayList<String>> mapOfProblemParts,
			String commodity, String part) {
		if(mapOfProblemParts.containsKey(commodity))
		{
			mapOfProblemParts.get(commodity).add(part);
		}else{
			ArrayList<String> tmplst = new ArrayList<String>();
			tmplst.add(part);
			mapOfProblemParts.put(commodity, tmplst);
		}
	}

	private void writeProblemPartsToScreen(
			HashMap<String, ArrayList<String>> mapOfProblemParts) {
		for(String com : mapOfProblemParts.keySet())
		{
			System.out.println("!!!!!!!!!!!-\t" + com + "\t-!!!!!!!!!!!");
			for(String part : mapOfProblemParts.get(com))
			{
				System.out.print(part + "\t");
			}
			System.out.println("------------------------------------------");
		}
		
	}

	public void calculateHurtScore(usageTool usageBO)
	{
		totalPotentialHurt = 0.0;
		savedHurt = 0.0;
		for(String batchName :  usageBO.commodityToHurtScoreMap.keySet())
		{
			Double commodityHurt = usageBO.commodityToHurtScoreMap.get(batchName);
			totalPotentialHurt = totalPotentialHurt + commodityHurt;
//			System.out.print(batchName + "\t");
//			System.out.print(commodityHurt + "\t");
//			System.out.print(usageBO.commodityDescriptionMap.get(batchName) + "\t");
			if(batchedCommodities.contains(batchName))
			{
				savedHurt = savedHurt + commodityHurt;
//				System.out.println("YES");				
			}else{
//				System.out.println("NO");
			}
			
		}
	}

	public void generateUniqueFtrsByBatch(BitSet[] configWiseBitSet,
			HashMap<String, String> ftrToFamMap,
			ArrayList<String> bitFtrSetHeader) {
			
		for(String batchID : batchConfig.keySet())
		{
			int batchIndex = Integer.parseInt(batchID);
			int firstConfigId = Integer.parseInt(batchConfig.get(batchID).get(0));
			BitSet tmpFtrBitSet = (BitSet) configWiseBitSet[firstConfigId];
			for(String configID : batchConfig.get(batchID))
			{
				int configIndex = Integer.parseInt(configID);
				BitSet tmpBitset2 = (BitSet) configWiseBitSet[configIndex];
				tmpFtrBitSet.and(tmpBitset2);
			}
			if(tmpFtrBitSet.cardinality()>0)
			{				
				for (int ftrIndex = tmpFtrBitSet.nextSetBit(0); ftrIndex >= 0; ftrIndex = tmpFtrBitSet.nextSetBit(ftrIndex+1)) 
				{
					String featureName = bitFtrSetHeader.get(ftrIndex);
					System.out.println("Batch: " + batchIndex + " All configs have feature Index: " + featureName + " From Family: " + ftrToFamMap.get(featureName));
				}
			}
			
		}

		
	}

	public void combineBatches() {
	
		HashMap<Integer, ArrayList<String>> uniqueBatchMap = new HashMap<Integer, ArrayList<String>>();
		ArrayList<String> alreadyGrouped = new ArrayList<String>();
		ArrayList<String> lstOfBatchId  = new ArrayList<String>(batchConfig.keySet());
		int superBatchId = 0;
		for(int index1 = 0; index1 < lstOfBatchId.size(); index1++)
		{
			if(alreadyGrouped.contains(lstOfBatchId.get(index1)))
			{
				continue;
			}
			for(int index2 = index1 + 1; index2 < lstOfBatchId.size(); index2++)
			{
				ArrayList<String> batch1Parts = uniquePartsByBatch.get(lstOfBatchId.get(index1));
				ArrayList<String> batch2Parts = uniquePartsByBatch.get(lstOfBatchId.get(index2));
				int matchingParts = 0;
				for(String partsBatch1 : batch1Parts)
				{
					if(batch2Parts.contains(partsBatch1))
					{
						matchingParts++;
						continue;
					}else{
						break;
					}
				}
				if(matchingParts == batch1Parts.size() && batch1Parts.size() == batch2Parts.size())
				{
					System.out.println("Batch: " + lstOfBatchId.get(index1) + " and Batch: " + lstOfBatchId.get(index2) + " EQUAL");
					if(!alreadyGrouped.contains(lstOfBatchId.get(index1)) && !alreadyGrouped.contains(lstOfBatchId.get(index2)))
					{
						ArrayList<String> tmplst = new ArrayList<String>();
						tmplst.add(lstOfBatchId.get(index1));
						tmplst.add(lstOfBatchId.get(index2));
						alreadyGrouped.add(lstOfBatchId.get(index1));
						alreadyGrouped.add(lstOfBatchId.get(index2));
						uniqueBatchMap.put(superBatchId, tmplst);
						superBatchId++;
					}
					if(!alreadyGrouped.contains(lstOfBatchId.get(index2)))
					{
						for(int superBatch : uniqueBatchMap.keySet())
						{
							if(uniqueBatchMap.get(superBatch).contains(lstOfBatchId.get(index1)))
							{
								uniqueBatchMap.get(superBatch).add(lstOfBatchId.get(index2));
								alreadyGrouped.add(lstOfBatchId.get(index2));
							}
						}
					}
				}
			}
		}
		System.out.println("Batch ID:\tSuper Batch ID:");
		for(int superBatch : uniqueBatchMap.keySet())
		{
			for(String id : uniqueBatchMap.get(superBatch))
			{
				System.out.println(id + "\t" + superBatch);
			}
		}
		
	}
}
