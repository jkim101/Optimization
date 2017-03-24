package mains;


import java.io.File;

import utilities.TSVFileReader;
import configUtilities.configTool;
import configsToBOM.explodeTool;
import configsToBOM.usageTool;
import batchBuildModel.solutionBO;

public class setupSummaryMain {

	public static void main(String[] args) {

		String rootFilePath = "C:/Dev/workspace/BatchBuild/data/";
//		String VLCode = "P552";
//		String run = "5-Com-V2";
		String VLCode = "C346";
		String run = "MY17-GRL_old";
				
		boolean GCPGearboxData = true;
		
		
		rootFilePath = rootFilePath + VLCode +"/" + run + "/";
//		String rootFilePath = "C:/Users/ahenry36/Documents/Projects/Manufacturing/Batch Build/C519-InputFiles/";

		String comToPartMapInputFile = VLCode + " Commodity To Part Map.txt";
		String comToHurtScoreInputFile = VLCode + " Commodity Hurt Scores.txt";
		String configFileName = VLCode + " Configs PDO.txt";
		String usageCodeFileName = VLCode + " Usage.txt";
		String summaryCodeFileName = VLCode + " SummaryFeatureMap.txt";
		String orderVolumeFileName = "C346_Unique_Order_Count.txt";
		String configByPartBitSetOutputFileName = "output/C346ConfigPartBitSet.txt";
		String gearboxFilePath = VLCode + "-CleanedGCPFiles/";
		String tableauOutput = "Tableau.txt";
		
		
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

			solution = new solutionBO(null, false);
			solution.writeTableauFile(configBO.bitFtrSetHeader, configBO.configWiseBitSet, 
					configBO.famFtrMap, configBO.configTakeRate, usageBO.partListHeader, 
					usageBO.commodityToPartsMap, bomBO.configByPartBitSet, rootFilePath + "output/" + "SetupSummary" + "_" + tableauOutput);
			
	}

	
}
