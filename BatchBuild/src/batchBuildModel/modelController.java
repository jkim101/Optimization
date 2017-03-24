package batchBuildModel;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;

import configUtilities.configTool;
import configsToBOM.explodeTool;
import configsToBOM.usageTool;


public class modelController {
public IloIntVar[] batchConfigVars;
public IloIntVar[] batchComFlagVars;
public IloIntVar[] comFlagVars; 
public IloIntVar[] batchFlagVars; 
public IloIntVar[] batchPartFlagVars;

	public void execute(configTool configBO, explodeTool bomBO, usageTool usageBO, int numBatches, String rootPath, int minVol, String VLCode) throws IloException
	{

//		int numConfigsWithRate = configBO.configTakeRate.keySet().size();
		final modelCPLEX optimizationModel = new modelCPLEX(configBO.configWiseBitSet.length, numBatches);
//		final modelCPLEX optimizationModel = new modelCPLEX(numConfigsWithRate, numBatches);
		batchConfigVars = optimizationModel.createConfigBatchVariables(configBO.configTakeRate);
		batchPartFlagVars = optimizationModel.createBatchPartVariables(bomBO.partListHeader);
		batchComFlagVars = optimizationModel.createBatchCommodityVaraibles(usageBO.commodityToPartsMap, usageBO.commodityList);
		comFlagVars = optimizationModel.createComFlagVar(usageBO.commodityList);
		batchFlagVars = optimizationModel.createBatchFlagVar(numBatches);
		
		optimizationModel.addConfigBatchConsistencyConstraint(batchConfigVars);

		IloIntVar[] batchConfigAndBatchFlagVars = new IloIntVar[batchConfigVars.length + batchFlagVars.length];
		System.arraycopy(batchConfigVars, 0, batchConfigAndBatchFlagVars, 0, batchConfigVars.length);
		System.arraycopy(batchFlagVars, 0, batchConfigAndBatchFlagVars, batchConfigVars.length, batchFlagVars.length);
 
		optimizationModel.addMinimumMixConstraint(minVol, configBO.configTakeRate, batchConfigAndBatchFlagVars);
		optimizationModel.addBatchFlagConstraint(batchConfigAndBatchFlagVars);
		
		IloIntVar[] batchConfigAndBatchPartFlagVars = new IloIntVar[batchConfigVars.length + batchPartFlagVars.length];
		System.arraycopy(batchConfigVars, 0, batchConfigAndBatchPartFlagVars, 0, batchConfigVars.length);
		System.arraycopy(batchPartFlagVars, 0, batchConfigAndBatchPartFlagVars, batchConfigVars.length, batchPartFlagVars.length);
		
		optimizationModel.addBatchPartConstraint(batchConfigAndBatchPartFlagVars, usageBO.partToCommodityMap, usageBO.partListHeader, bomBO.partByConfigBitSet, usageBO.commodityList);
	
		IloIntVar[] batchPartAndPartFlagVars = new IloIntVar[batchPartFlagVars.length + batchFlagVars.length];
		System.arraycopy(batchPartFlagVars, 0, batchPartAndPartFlagVars, 0, batchPartFlagVars.length);
		System.arraycopy(batchFlagVars, 0, batchPartAndPartFlagVars, batchPartFlagVars.length, batchFlagVars.length);
		
		optimizationModel.addBatchPartandBatchFlagConsistConst(batchPartAndPartFlagVars, usageBO.partListHeader);
		
		IloIntVar[] batchPartFlagAndBatchAndcomFlagVar = new IloIntVar[batchPartFlagVars.length + batchFlagVars.length + comFlagVars.length];
		System.arraycopy(batchPartFlagVars, 0, batchPartFlagAndBatchAndcomFlagVar, 0, batchPartFlagVars.length);
		System.arraycopy(batchFlagVars, 0, batchPartFlagAndBatchAndcomFlagVar, batchPartFlagVars.length, batchFlagVars.length);
		System.arraycopy(comFlagVars, 0, batchPartFlagAndBatchAndcomFlagVar, batchPartFlagVars.length +  batchFlagVars.length, comFlagVars.length);
		optimizationModel.addComFlagConstraintV2(batchPartFlagAndBatchAndcomFlagVar, usageBO.commodityToPartsMap,  usageBO.commodityList, usageBO.partListHeader);
		
		
		
//		IloIntVar[] batchComFlagVarsAndcomFlagVars = new IloIntVar[batchComFlagVars.length + comFlagVars.length];
//		System.arraycopy(batchComFlagVars, 0, batchComFlagVarsAndcomFlagVars, 0, batchComFlagVars.length);
//		System.arraycopy(comFlagVars, 0, batchComFlagVarsAndcomFlagVars, batchComFlagVars.length, comFlagVars.length);
//		
//		optimizationModel.addComFlagConstraint(batchComFlagVarsAndcomFlagVars, usageBO.commodityList);
//		optimizationModel.addObjFnMaxConstraint(comFlagVars, usageBO.commodityList, usageBO.commodityToHurtScoreMap, 103);
		optimizationModel.addObjFn(comFlagVars, usageBO.commodityList, usageBO.commodityToHurtScoreMap);
		optimizationModel.writeOutput("lp", rootPath, VLCode + "_" + minVol);
				
//		optimizationModel.cplexModel.setParam(IloCplex.StringParam.IntSolFilePrefix,rootPath + "/sol/bestFound_" + VLCode + "_" + minVol);
		optimizationModel.cplexModel.solve();
		optimizationModel.writeOutput("sol", rootPath, VLCode + "_" + minVol);
		
	}
	
	public void executeWithPreFilter(configTool configBO, explodeTool bomBO, usageTool usageBO, int numBatches, String rootPath, int minVol, String VLCode, ArrayList<String> commoditiesToExclude) throws IloException
	{

		final modelCPLEX optimizationModel = new modelCPLEX(configBO.configWiseBitSet.length, numBatches);
		batchConfigVars = optimizationModel.createConfigBatchVariables(configBO.configTakeRate);
		batchPartFlagVars = optimizationModel.createBatchPartVariablesWithExclusions(bomBO.partListHeader, usageBO.partToCommodityMap, commoditiesToExclude);
		batchComFlagVars = optimizationModel.createBatchCommodityVaraiblesWithExclusions(usageBO.commodityToPartsMap, usageBO.commodityList, commoditiesToExclude);
		comFlagVars = optimizationModel.createComFlagVarWithExclusions(usageBO.commodityList, commoditiesToExclude);
		batchFlagVars = optimizationModel.createBatchFlagVar(numBatches);
		
		optimizationModel.addConfigBatchConsistencyConstraint(batchConfigVars);

		IloIntVar[] batchConfigAndBatchFlagVars = new IloIntVar[batchConfigVars.length + batchFlagVars.length];
		System.arraycopy(batchConfigVars, 0, batchConfigAndBatchFlagVars, 0, batchConfigVars.length);
		System.arraycopy(batchFlagVars, 0, batchConfigAndBatchFlagVars, batchConfigVars.length, batchFlagVars.length);
 
		optimizationModel.addMinimumMixConstraint(minVol, configBO.configTakeRate, batchConfigAndBatchFlagVars);
		optimizationModel.addBatchFlagConstraint(batchConfigAndBatchFlagVars);
		
		IloIntVar[] batchConfigAndBatchPartFlagVars = new IloIntVar[batchConfigVars.length + batchPartFlagVars.length];
		System.arraycopy(batchConfigVars, 0, batchConfigAndBatchPartFlagVars, 0, batchConfigVars.length);
		System.arraycopy(batchPartFlagVars, 0, batchConfigAndBatchPartFlagVars, batchConfigVars.length, batchPartFlagVars.length);
		
		optimizationModel.addBatchPartConstraintWithExclusions(batchConfigAndBatchPartFlagVars, usageBO.partToCommodityMap, usageBO.partListHeader, bomBO.partByConfigBitSet, usageBO.commodityList, commoditiesToExclude);
	
		IloIntVar[] batchPartAndPartFlagVars = new IloIntVar[batchPartFlagVars.length + batchFlagVars.length];
		System.arraycopy(batchPartFlagVars, 0, batchPartAndPartFlagVars, 0, batchPartFlagVars.length);
		System.arraycopy(batchFlagVars, 0, batchPartAndPartFlagVars, batchPartFlagVars.length, batchFlagVars.length);
		
		optimizationModel.addBatchPartandBatchFlagConsistConstWithExclusions(batchPartAndPartFlagVars, usageBO.partListHeader,  usageBO.partToCommodityMap, commoditiesToExclude);
		
		IloIntVar[] batchPartFlagAndBatchAndcomFlagVar = new IloIntVar[batchPartFlagVars.length + batchFlagVars.length + comFlagVars.length];
		System.arraycopy(batchPartFlagVars, 0, batchPartFlagAndBatchAndcomFlagVar, 0, batchPartFlagVars.length);
		System.arraycopy(batchFlagVars, 0, batchPartFlagAndBatchAndcomFlagVar, batchPartFlagVars.length, batchFlagVars.length);
		System.arraycopy(comFlagVars, 0, batchPartFlagAndBatchAndcomFlagVar, batchPartFlagVars.length +  batchFlagVars.length, comFlagVars.length);
		optimizationModel.addComFlagConstraintV2(batchPartFlagAndBatchAndcomFlagVar, usageBO.commodityToPartsMap,  usageBO.commodityList, usageBO.partListHeader);
		
		
		
//		IloIntVar[] batchComFlagVarsAndcomFlagVars = new IloIntVar[batchComFlagVars.length + comFlagVars.length];
//		System.arraycopy(batchComFlagVars, 0, batchComFlagVarsAndcomFlagVars, 0, batchComFlagVars.length);
//		System.arraycopy(comFlagVars, 0, batchComFlagVarsAndcomFlagVars, batchComFlagVars.length, comFlagVars.length);
//		
//		optimizationModel.addComFlagConstraint(batchComFlagVarsAndcomFlagVars, usageBO.commodityList);
//		optimizationModel.addObjFnMaxConstraint(comFlagVars, usageBO.commodityList, usageBO.commodityToHurtScoreMap, 103);
		optimizationModel.addObjFn(comFlagVars, usageBO.commodityList, usageBO.commodityToHurtScoreMap);
		optimizationModel.writeOutput("lp", rootPath, VLCode + "_" + minVol);
				
		optimizationModel.cplexModel.setParam(IloCplex.StringParam.IntSolFilePrefix,rootPath + "/sol/bestFound_" + VLCode + "_" + minVol);
		optimizationModel.cplexModel.solve();
		optimizationModel.writeOutput("sol", rootPath, VLCode + "_" + minVol);
		
	}
	
}
