package mains;


import java.io.File;

import utilities.TSVFileReader;
import configUtilities.configTool;
import configsToBOM.explodeTool;
import configsToBOM.usageTool;
import batchBuildModel.solutionBO;

public class resultsSummaryMain {

	public static void main(String[] args) {

		String rootFilePath = "C:/Dev/workspace/BatchBuild/data/";
		String VLCode = "C346";
		String run ="MY17-GRL_w_PAA_1";
//		String VLCode = "P552";
//		String run = "5-Com-V2";
		boolean GCPGearboxData = true;
		

		String hpcRootPath = "S:/C346_GRL/C346_30/";

		
		rootFilePath = rootFilePath + VLCode +"/" + run + "/";

		String comToPartMapInputFile = VLCode + " Commodity To Part Map.txt";
		String comToHurtScoreInputFile = VLCode + " Commodity Hurt Scores.txt";
		String configFileName = VLCode + " Configs PDO.txt";
		String usageCodeFileName = VLCode + " Usage.txt";
		String summaryCodeFileName = VLCode + " SummaryFeatureMap.txt";
		
//		String comToPartMapInputFile = "C346 Commodity To Part Map V4 - Manual.txt";
//		String comToHurtScoreInputFile = "C346 Commodity Hurt Scores V4 - Manual.txt";
//		String configFileName = "C519 Configs.txt";
//		String usageCodeFileName = "C519 Usage Falcon.txt";
//		

		String configByPartBitSetOutputFileName = "output/" + VLCode + " C346ConfigPartBitSet.txt";
		String gearboxFilePath = VLCode + "-CleanedGCPFiles/";
		String orderVolumeFileName = "C346_Unique_Order_Count.txt";
		String tableauOutput = "ResultTable.txt";
//		String hpcRootPath = "S:/BatchTesting/P552/V1/P552_12/";

		
		TSVFileReader configFile = new TSVFileReader(rootFilePath + configFileName);
		configTool configBO = new configTool(configFile, rootFilePath + configFileName);
		
		if(GCPGearboxData)
		{
			configBO.establishRatesFromGearboxFiles(rootFilePath + gearboxFilePath);
			configBO.writeVolumeByCountries();
		}else{
			
			TSVFileReader orderVolumeFile = new TSVFileReader(rootFilePath + orderVolumeFileName);
			configBO.establishVolumesFromOrderData(orderVolumeFile);
		}
		
		

		
		TSVFileReader usageFile = new TSVFileReader(rootFilePath + usageCodeFileName);
		usageTool usageBO = new usageTool(usageFile, configBO.featureBitSetIndex,
				rootFilePath + usageCodeFileName, rootFilePath + comToPartMapInputFile,
				rootFilePath + comToHurtScoreInputFile, rootFilePath + summaryCodeFileName);
		
		explodeTool bomBO = new explodeTool(usageBO.fullOrderdPartList, usageBO.partListHeader);
		bomBO.writeExplodedConfig(configBO.configWiseBitSet, usageBO.partWiseFtrBitSet, 
				rootFilePath + configByPartBitSetOutputFileName, usageBO.uniqueFamiliesForUsage);
		solutionBO solution;
		
		
		File dir = new File(hpcRootPath);
		File[] directoryListing = dir.listFiles();
		String bestSolutionFileName = "";
		double bestSolutionObj = 0.0;
		for(File file : directoryListing)
		{
			solution = new solutionBO(hpcRootPath + file.getName(), true);
			solution.calculateHurtScore(usageBO);
			solution.writeTableauFile(configBO.bitFtrSetHeader, configBO.configWiseBitSet, 
					configBO.famFtrMap, configBO.configTakeRate, usageBO.partListHeader, 
					usageBO.commodityToPartsMap, bomBO.configByPartBitSet, rootFilePath + "output/" + file.getName() + "_" + tableauOutput);
			if(solution.savedHurt > bestSolutionObj)
			{
				bestSolutionFileName = file.getName();
				bestSolutionObj = solution.savedHurt;
			}
			
		}
		solution = new solutionBO(hpcRootPath + bestSolutionFileName, true);
		writeCommoditySummaryToScreen(rootFilePath, hpcRootPath, usageBO,
				solution, bestSolutionFileName);
		solution.writeTableauFile(configBO.bitFtrSetHeader, configBO.configWiseBitSet, 
				configBO.famFtrMap, configBO.configTakeRate, usageBO.partListHeader, 
				usageBO.commodityToPartsMap, bomBO.configByPartBitSet, rootFilePath + "output/" + "BEST_" + tableauOutput);
		solution.combineBatches();
		solution.generateUniqueFtrsByBatch(configBO.configWiseBitSet, configBO.ftrToFamMap, configBO.bitFtrSetHeader);
		

		
	}

	private static void writeCommoditySummaryToScreen(String rootFilePath,
			String hpcRootPath, usageTool usageBO, solutionBO solution,
			String fileName) {
		System.out.println();
		System.out.println();
		System.out.println("------------------ " + hpcRootPath + fileName + " ------------------");
		System.out.println("Commodity\tHurtScore\tDescription\tBatched");
		Double totalHurt = 0.0;
		Double savedHurt = 0.0;
		for(String batchName :  usageBO.commodityToHurtScoreMap.keySet())
		{
			Double commodityHurt = usageBO.commodityToHurtScoreMap.get(batchName);
			totalHurt = totalHurt + commodityHurt;
			System.out.print(batchName + "\t");
			System.out.print(commodityHurt + "\t");
			System.out.print(usageBO.commodityDescriptionMap.get(batchName) + "\t");
			if(solution.batchedCommodities.contains(batchName))
			{
				savedHurt = savedHurt + commodityHurt;
				System.out.println("YES");				
			}else{
				System.out.println("NO");
			}
			
		}
		System.out.println("Total Hurt:\t" + totalHurt + "\tHurt Saved:\t" + savedHurt + "\tHurt Reduction:\t" + savedHurt / totalHurt);
		System.out.println("Batches Created:\t" + solution.batchConfig.keySet().size());
		System.out.println("------------------ " + rootFilePath + fileName + " ------------------");
	}

	
}
