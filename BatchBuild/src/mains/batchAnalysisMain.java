package mains;

import java.util.ArrayList;

import ilog.concert.IloException;
import batchBuildModel.modelController;
import utilities.TSVFileReader;
import configUtilities.configTool;
import configUtilities.preProcessUtility;
import configsToBOM.explodeTool;
import configsToBOM.usageTool;

public class batchAnalysisMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	//.......... Provide File Locations ................ //
		String rootFilePath = "C:/Dev/workspace/BatchBuild/data/";
//		String VLCode = "P552";
//		String run = "5-Com-V2";
		String VLCode = "C346";
		String run = "MY17-GRL";
		boolean GCPGearboxData = true;
		
		
		
		rootFilePath = rootFilePath + VLCode +"/" + run + "/";
//		String rootFilePath = "C:/Users/ahenry36/Documents/Projects/Manufacturing/Batch Build/C519-InputFiles/";

		String comToPartMapInputFile = VLCode + " Commodity To Part Map.txt";
		String comToHurtScoreInputFile = VLCode + " Commodity Hurt Scores.txt";
		String configFileName = VLCode + " Configs PDO.txt";
		String usageCodeFileName = VLCode + " Usage.txt";
		String summaryCodeFileName = VLCode + " SummaryFeatureMap.txt";
		String configByPartBitSetOutputFileName = "output/C346ConfigPartBitSet.txt";
//		
		String bitSetHeaderOutputFileName = "output/" + VLCode + " BitSetHeader.txt";
		String configAsBitSetOutputFileName ="output/" + VLCode + " ConfigBitSet.txt";
		String usageAsBitSetOutputFileName = "output/" + VLCode + " UsageBitSet.txt";
		String configPartBOMOutputFileName = "output/" + VLCode + " ConfigPartBOM.txt";
		String partByConfigBitSetOutputFileName = "output/" + VLCode + " PartByConfigBitSet.txt";
		
		
		String orderVolumeFileName = "C346_Unique_Order_Count.txt";
		String gearboxFilePath = VLCode + "-CleanedGCPFiles/";

		String configRateOutput = "output/" + VLCode + " configRate.txt";

		int maxNumberBatches = 50;
		
		int minBatchSize = 30;
//		int minBatchSize = 12;
//		int minBatchSize = 48;
//		int minBatchSize = 72;
		
	//..........Generate ConfigBO and BitSets..........//
		TSVFileReader configFile = new TSVFileReader(rootFilePath + configFileName);
		configTool configBO = new configTool(configFile, rootFilePath + configFileName);
		configBO.writeBitSetHeader(rootFilePath + bitSetHeaderOutputFileName);
		configBO.writeConfigBitSet(rootFilePath + configAsBitSetOutputFileName);

		if(GCPGearboxData)
		{
			configBO.establishRatesFromGearboxFiles(rootFilePath + gearboxFilePath);
			configBO.writeVolumeByCountries();
		}else{
			
			TSVFileReader orderVolumeFile = new TSVFileReader(rootFilePath + orderVolumeFileName);
			configBO.establishVolumesFromOrderData(orderVolumeFile);
		}
	//.................................................//
		
	//...........Generate Usage BO.....................//
		TSVFileReader usageFile = new TSVFileReader(rootFilePath + usageCodeFileName);
		usageTool usageBO = new usageTool(usageFile, configBO.featureBitSetIndex,
				rootFilePath + usageCodeFileName, rootFilePath + comToPartMapInputFile,
				rootFilePath + comToHurtScoreInputFile, rootFilePath + summaryCodeFileName);
		
		usageBO.writePartWiseFtrBitSet(rootFilePath + usageAsBitSetOutputFileName);
	//.................................................//
		
	//...........Generate Config BOM...................//
		explodeTool bomBO = new explodeTool(usageBO.fullOrderdPartList, usageBO.partListHeader);
		bomBO.writeExplodedConfig(configBO.configWiseBitSet, usageBO.partWiseFtrBitSet, rootFilePath + configPartBOMOutputFileName, usageBO.uniqueFamiliesForUsage);
		bomBO.writeConfigByPartBitSet(rootFilePath + configByPartBitSetOutputFileName);
		bomBO.writePartByConfigBitSet(rootFilePath + partByConfigBitSetOutputFileName);
	//.................................................//
		
		preProcessUtility preProc = new preProcessUtility();
//		ArrayList<String> commoditiesToExclude = preProc.identifyCommoditiesToExclude(bomBO, usageBO , configBO, minBatchSize);
		modelController controller = new modelController();
		try {
			controller.execute(configBO, bomBO, usageBO, maxNumberBatches, rootFilePath, minBatchSize,VLCode);
//			controller.executeWithPreFilter(configBO, bomBO, usageBO, maxNumberBatches, rootFilePath, minBatchSize, VLCode, commoditiesToExclude);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
