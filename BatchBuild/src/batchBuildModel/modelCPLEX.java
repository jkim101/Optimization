package batchBuildModel;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

import javax.swing.ListCellRenderer;

public class modelCPLEX {

	ArrayList<String> VariableNames = new ArrayList<String>();
	public IloCplex cplexModel;
	private double bigM = 99999;
	private int numConfigs;
	private int numBatches;
	private int numCommodities;
	private int numParts;

	public modelCPLEX(int numConfigs, int numBatches) throws IloException {
		cplexModel = new IloCplex();
		this.numBatches = numBatches;
		this.numConfigs = numConfigs;

	}

	public IloIntVar[] createConfigBatchVariables(HashMap<Integer,Double> configTakeRate) throws IloException {

		int numBatchConfigVars = 0;
		int numConfigsWithRate = 0;
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			numConfigsWithRate = 0;
			for (int configIndex = 0; configIndex < numConfigs; configIndex++) {
				numConfigsWithRate++;
				VariableNames.add("X_B_" + batchIndex + "_Cfg_" + configIndex);
				numBatchConfigVars++;
			}
		}
//		this.numConfigs = numConfigsWithRate;
		System.out.println("Total # Batch Config Variables: "
				+ numBatchConfigVars);
		String batchConfigNames[] = Arrays.copyOf(VariableNames.toArray(),
				VariableNames.size(), String[].class);
		return cplexModel.intVarArray(numBatchConfigVars, 0, 1,
				batchConfigNames);
	}

	public IloIntVar[] createBatchPartVariables(ArrayList<String> partListHeader) throws IloException {
		
		numParts = partListHeader.size();
		ArrayList<String> batchPartFlagVarNames = new ArrayList<String>();
		int numBatchPartVar = 0;
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) 
		{
			for (int partIndex = 0; partIndex < numParts; partIndex++) 
			{
				batchPartFlagVarNames.add("F_B_" + batchIndex + "_Part_" + partListHeader.get(partIndex));
				numBatchPartVar++;
			}
			
		}
		String batchPartNames[] = Arrays.copyOf(batchPartFlagVarNames.toArray(),
				batchPartFlagVarNames.size(), String[].class);
		return cplexModel.intVarArray(numBatchPartVar, 0, 1, batchPartNames);
	}
	
	
	public IloIntVar[] createBatchCommodityVaraibles(
			
			HashMap<String, ArrayList<String>> commodityToPartsMap,
			ArrayList<String> commodityList) throws IloException {
		int numBatchComVars = 0;
		numCommodities = commodityList.size();
		ArrayList<String> batchCommFlagVarNames = new ArrayList<String>();
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			for (int comIndex = 0; comIndex < numCommodities; comIndex++) {

				batchCommFlagVarNames.add("F_B_" + batchIndex + "_Com_"
						+ commodityList.get(comIndex));
				numBatchComVars++;
			}
		}
		System.out.println("Total # Batch Commodity Variables: "
				+ numBatchComVars);
		String batchComNames[] = Arrays.copyOf(batchCommFlagVarNames.toArray(),
				batchCommFlagVarNames.size(), String[].class);
		return cplexModel.intVarArray(numBatchComVars, 0, 1, batchComNames);
	}

	public IloIntVar[] createComFlagVar(ArrayList<String> commodityList)
			throws IloException {
		// TODO Auto-generated method stub
		ArrayList<String> commFlagVarNames = new ArrayList<String>();
		int numComVars = 0;
		for (int comIndex = 0; comIndex < numCommodities; comIndex++) {

			commFlagVarNames.add("Flag_Com_" + commodityList.get(comIndex));
			numComVars++;
		}
		System.out.println("Total # Commodity Flag Variables: " + numComVars);
		String comNames[] = Arrays.copyOf(commFlagVarNames.toArray(),
				commFlagVarNames.size(), String[].class);
		return cplexModel.intVarArray(numComVars, 0, 1, comNames);
	}

	public IloIntVar[] createBatchFlagVar(int numBatches) throws IloException {
		String[] batchVarNames = new String[numBatches];
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			batchVarNames[batchIndex] = "Flag_B_" + batchIndex;
		}

		return cplexModel.intVarArray(numBatches, 0, 1, batchVarNames);
	}

	public void addConfigBatchConsistencyConstraint(IloNumVar[] batchConfigVars)
			throws IloException {

		IloRange iloRange;
		int iRowIndex;
		final String constName = "Single_Batch_Selection";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchConfigVars);
		double sparceCoeff[];
		int sparceIndex[];

		for (int configIndex = 0; configIndex < numConfigs; configIndex++) {
			sparceCoeff = new double[numBatches];
			sparceIndex = new int[numBatches];
			for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
				int varIndex = batchIndex * numConfigs + configIndex;
				sparceIndex[batchIndex] = varIndex;
				sparceCoeff[batchIndex] = 1d;
			}

			iRowIndex = LPMatrix.addRow(1, 1, sparceIndex, sparceCoeff);
			LPMatrix.getRange(iRowIndex).setName(
					"Config_" + configIndex + "_Single_Batch_perConfig");
		}
	}

	public void addMinimumMixConstraint(double minMix,
			HashMap<Integer, Double> configTakeRate,
			IloIntVar[] batchConfigAndBatchFlagVars) throws IloException {
		int iRowIndex;
		final String constName = "Min_Mix_Constraints";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchConfigAndBatchFlagVars);
		double sparceCoeff[];
		int sparceIndex[];
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			sparceCoeff = new double[numConfigs + 1];
			sparceIndex = new int[numConfigs + 1];
			for (int configIndex = 0; configIndex < numConfigs; configIndex++) {
				int varIndex = batchIndex * numConfigs + configIndex;
				sparceIndex[configIndex] = varIndex;
				double coeff = 0;
				if(configTakeRate.containsKey(configIndex))
				{
					coeff = configTakeRate.get(configIndex);
				}
				sparceCoeff[configIndex] = coeff;
			}
			sparceIndex[numConfigs] = numBatches * numConfigs + batchIndex;
			sparceCoeff[numConfigs] = -minMix;
			iRowIndex = LPMatrix.addRow(0, Double.MAX_VALUE, sparceIndex, sparceCoeff);
			LPMatrix.getRange(iRowIndex).setName(
					"Batch_" + batchIndex + "_MinMix");
		}
	}

	/**
	 * 
	 * @param batchConfigAndBatchComFlagVars
	 * @param partToCommodityMap
	 * @param partListHeader
	 * @param partByConfigBitSet
	 * @param commodityList
	 * @throws IloException
	 */
	public void addBatchPartConstraint(
			IloIntVar[] batchConfigAndBatchComFlagVars,
			HashMap<String, String> partToCommodityMap,
			ArrayList<String> partListHeader, BitSet[] partByConfigBitSet,
			ArrayList<String> commodityList) throws IloException {
		int iRowIndex;
		final String constName = "BatchContainsPartValidationConstraint";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchConfigAndBatchComFlagVars);
		ArrayList<Double> arrCoeff;
		ArrayList<Integer> arrIndex;
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			for (int partIndex = 0; partIndex < partListHeader.size(); partIndex++) {
				arrCoeff = new ArrayList<Double>();
				arrIndex = new ArrayList<Integer>();
				// find all configs that match that part
				for (int configIndex = 0; configIndex < numConfigs; configIndex++) {
					if (partByConfigBitSet[partIndex].get(configIndex)) {
						int varIndex = batchIndex * numConfigs + configIndex;
						arrCoeff.add(1d);
						arrIndex.add(varIndex);
					}
				}
				String partName = partListHeader.get(partIndex);
				int batchPartVarIndex = numBatches * numConfigs + batchIndex
						* numParts + partIndex;
				arrCoeff.add(-bigM);
				arrIndex.add(batchPartVarIndex);

				int[] sparceVarIndex = ArrayToNativeInt(arrIndex);
				double[] sparceCoeff = ArrayToNativeDouble(arrCoeff);
				iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0,
						sparceVarIndex, sparceCoeff);
				LPMatrix.getRange(iRowIndex).setName(
						"B_" + batchIndex + "_P_" + partName +"_ConstConst");
			}
		}
	}

	public void addComFlagConstraint(
			IloIntVar[] batchComFlagVarsAndcomFlagVars,
			ArrayList<String> commodityList) throws IloException {

		int iRowIndex;
		final String constName = "CommodityFlagConstraint";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchComFlagVarsAndcomFlagVars);
		ArrayList<Double> arrCoeff;
		ArrayList<Integer> arrIndex;
		for (int comIndex = 0; comIndex < numCommodities; comIndex++) {
			int[] sparceVarIndex = new int[numBatches + 1];
			double[] sparceCoeff = new double[numBatches + 1];
			int index = 0;
			for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
				int varIndex = batchIndex * numCommodities + comIndex;
				sparceVarIndex[index] = varIndex;
				sparceCoeff[index] = 1;
				index++;
			}
			int varIndex = numBatches * numCommodities + comIndex;
			sparceVarIndex[index] = varIndex;
			sparceCoeff[index] = -bigM;
			String commodityName = commodityList.get(comIndex);
			iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0, sparceVarIndex,
					sparceCoeff);
			LPMatrix.getRange(iRowIndex).setName(
					"C_" + commodityName + "_ConstConst");
		}

	}
	public void addComFlagConstraintV2(
			IloIntVar[] batchPartFlagAndBatchAndcomFlagVar,
			HashMap<String, ArrayList<String>> commodityToPartsMap,
			ArrayList<String> commodityList, ArrayList<String> partListHeader) throws IloException {
		int iRowIndex;
		final String constName = "CommodityFlagConstraint";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchPartFlagAndBatchAndcomFlagVar);
		for (int comIndex = 0; comIndex < numCommodities; comIndex++) 
		{
			ArrayList<Double> arrCoeff =  new ArrayList<Double>();
			ArrayList<Integer> arrIndex = new ArrayList<Integer>();
			for(int batchIndex = 0; batchIndex < numBatches; batchIndex++)
			{
				for(String partName : commodityToPartsMap.get(commodityList.get(comIndex)))
				{
					int partIndex = partListHeader.indexOf(partName);
					if(partIndex == -1)
					{
						continue;
					}
					int batchPartVarIndex = batchIndex * numParts + partIndex;
					arrIndex.add(batchPartVarIndex);
					arrCoeff.add(1d);
				}
				int batchVarIndex = numBatches * numParts + batchIndex;
				arrIndex.add(batchVarIndex);
				arrCoeff.add(-1d);
				int comVarIndex = numBatches * numParts + numBatches + comIndex;
				arrIndex.add(comVarIndex);
				arrCoeff.add(-bigM);
				int[] sparceVarIndex = ArrayToNativeInt(arrIndex);
				double[] sparceCoeff = ArrayToNativeDouble(arrCoeff);
				iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0, sparceVarIndex, sparceCoeff);
				LPMatrix.getRange(iRowIndex).setName(
						"Flag_C_" + commodityList.get(comIndex) + "_Batch_" + batchIndex);
			}
		}
		
	}
	public void addBatchFlagConstraint(IloIntVar[] batchConfigAndBatchFlagVars) throws IloException {
	
		int iRowIndex;
		final String constName = "BatchFlagConst";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchConfigAndBatchFlagVars);
		double sparceCoeff[];
		int sparceIndex[];
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) 
		{
			sparceCoeff = new double[numConfigs + 1];
			sparceIndex = new int[numConfigs + 1];
			for (int configIndex = 0; configIndex < numConfigs; configIndex++) 
			{
				int varIndex = batchIndex * numConfigs + configIndex;
				sparceIndex[configIndex] = varIndex;
				sparceCoeff[configIndex] = 1;
			}
			sparceIndex[numConfigs] = numBatches * numConfigs + batchIndex;
			sparceCoeff[numConfigs] = -bigM;
			
			iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0, sparceIndex, sparceCoeff);
			LPMatrix.getRange(iRowIndex).setName(
					"Batch_" + batchIndex + "_Activation");
		}
	}

	
	public void addObjFn(IloIntVar[] comFlagVars,
			ArrayList<String> commodityList,
			HashMap<String, Double> commodityToHurtScoreMap)
			throws IloException {
		IloNumExpr objFnExpression = cplexModel.linearNumExpr();

		for (int comIndex = 0; comIndex < numCommodities; comIndex++) {
			String commodityName = commodityList.get(comIndex);
			objFnExpression = cplexModel.sum(objFnExpression,
					cplexModel.prod(commodityToHurtScoreMap.get(commodityName.replace("-", "m")), comFlagVars[comIndex]));
		}
		cplexModel.addMinimize(objFnExpression);

	}

	private int[] ArrayToNativeInt(ArrayList<Integer> arrVar) {
		int[] arrInt = new int[arrVar.size()];
		for (int i = 0; i < arrVar.size(); i++) {
			arrInt[i] = arrVar.get(i);
		}
		return arrInt;
	}

	private double[] ArrayToNativeDouble(ArrayList<Double> arrVar) {
		double[] arrDouble = new double[arrVar.size()];
		for (int i = 0; i < arrVar.size(); i++) {
			arrDouble[i] = arrVar.get(i);
		}
		return arrDouble;
	}

	public void writeOutput(String type, String path, String fileName)
			throws IloException {
		String filePath = path + "lpFiles/" + fileName + "." + type;
		if (type.equals("lp")) {
			cplexModel.exportModel(filePath);
		} else if (type.equals("sol")) {
			cplexModel.writeSolution(filePath);
		} else {
			System.out.println("ERROR Writing Output File: Unknown Type");
		}
	}

	public void addBatchPartandBatchFlagConsistConst(
			IloIntVar[] batchPartAndPartFlagVars, ArrayList<String> partListHeader) throws IloException {
		int iRowIndex;
		final String constName = "BatchPartandBatchFlagConsistConst";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchPartAndPartFlagVars);

		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			int[] arrIndex = new int[numParts + 1];
			double[] arrCoeff = new double[numParts + 1];
			for (int partIndex = 0; partIndex < numParts; partIndex++) 
			{
				int batchPartVarIndex = batchIndex * numParts + partIndex;
				arrIndex[partIndex] = batchPartVarIndex;
				arrCoeff[partIndex] = 1d;
			}
			arrIndex[numParts] = numBatches * numParts + batchIndex;
			arrCoeff[numParts] = -bigM;
			iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0, arrIndex, arrCoeff);
			LPMatrix.getRange(iRowIndex).setName(
					"Batch_" + batchIndex + "_Part_Consistency");
		}
		
		
	}

	public void addObjFnMaxConstraint(IloIntVar[] comFlagVars,
			ArrayList<String> commodityList,
			HashMap<String, Double> commodityToHurtScoreMap, double maxObj) throws IloException {
		
		int iRowIndex;
		final String constName = "MaxHurt-WarmStart";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(comFlagVars);
		double[] sparceCoef = new double[comFlagVars.length];
		int[] sparceVarIndex = new int[comFlagVars.length];
		for (int comIndex = 0; comIndex < numCommodities; comIndex++) {
			String commodityName = commodityList.get(comIndex);
			sparceCoef[comIndex] = commodityToHurtScoreMap.get(commodityName.replace("-", "m"));
			sparceVarIndex[comIndex] = comIndex;
		}
		LPMatrix.addRow(0, maxObj, sparceVarIndex, sparceCoef);
		
	}

	public IloIntVar[] createBatchPartVariablesWithExclusions(
			ArrayList<String> partListHeader,
			HashMap<String, String> partToCommodityMap,
			ArrayList<String> commoditiesToExclude) throws IloException {

		numParts = partListHeader.size();
		ArrayList<String> batchPartFlagVarNames = new ArrayList<String>();
		int numBatchPartVar = 0;
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) 
		{
			for (int partIndex = 0; partIndex < numParts; partIndex++) 
			{
				if(!commoditiesToExclude.contains(partToCommodityMap.get(partListHeader.get(partIndex))))
				{
					batchPartFlagVarNames.add("F_B_" + batchIndex + "_Part_" + partListHeader.get(partIndex));
					numBatchPartVar++;					
				}
			}
			
		}
		String batchPartNames[] = Arrays.copyOf(batchPartFlagVarNames.toArray(),
				batchPartFlagVarNames.size(), String[].class);
		return cplexModel.intVarArray(numBatchPartVar, 0, 1, batchPartNames);
	}

	public IloIntVar[] createBatchCommodityVaraiblesWithExclusions(
			HashMap<String, ArrayList<String>> commodityToPartsMap,
			ArrayList<String> commodityList,
			ArrayList<String> commoditiesToExclude) throws IloException {

		int numBatchComVars = 0;
		numCommodities = commodityList.size();
		ArrayList<String> batchCommFlagVarNames = new ArrayList<String>();
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			for (int comIndex = 0; comIndex < numCommodities; comIndex++) {
	
				if(!commoditiesToExclude.contains(commodityList.get(comIndex)))
				{
					batchCommFlagVarNames.add("F_B_" + batchIndex + "_Com_"
							+ commodityList.get(comIndex));
					numBatchComVars++;					
				}
			}
		}
		System.out.println("Total # Batch Commodity Variables: "
				+ numBatchComVars);
		String batchComNames[] = Arrays.copyOf(batchCommFlagVarNames.toArray(),
				batchCommFlagVarNames.size(), String[].class);
		return cplexModel.intVarArray(numBatchComVars, 0, 1, batchComNames);
	}

	public IloIntVar[] createComFlagVarWithExclusions(
			ArrayList<String> commodityList,
			ArrayList<String> commoditiesToExclude) throws IloException {
		ArrayList<String> commFlagVarNames = new ArrayList<String>();
		int numComVars = 0;
		for (int comIndex = 0; comIndex < numCommodities; comIndex++) {

			commFlagVarNames.add("Flag_Com_" + commodityList.get(comIndex));
			numComVars++;
		}
		System.out.println("Total # Commodity Flag Variables: " + numComVars);
		String comNames[] = Arrays.copyOf(commFlagVarNames.toArray(),
				commFlagVarNames.size(), String[].class);
		return cplexModel.intVarArray(numComVars, 0, 1, comNames);
	}

	public void addBatchPartConstraintWithExclusions(
			IloIntVar[] batchConfigAndBatchPartFlagVars,
			HashMap<String, String> partToCommodityMap,
			ArrayList<String> partListHeader, BitSet[] partByConfigBitSet,
			ArrayList<String> commodityList,
			ArrayList<String> commoditiesToExclude) throws IloException {
		int iRowIndex;
		final String constName = "BatchContainsPartValidationConstraint";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchConfigAndBatchPartFlagVars);
		ArrayList<Double> arrCoeff;
		ArrayList<Integer> arrIndex;
		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			for (int partIndex = 0; partIndex < partListHeader.size(); partIndex++) {
				if(commoditiesToExclude.contains(partToCommodityMap.get(partListHeader.get(partIndex))))
				{
					continue;
				}
				arrCoeff = new ArrayList<Double>();
				arrIndex = new ArrayList<Integer>();
				// find all configs that match that part
				for (int configIndex = 0; configIndex < numConfigs; configIndex++) {
					if (partByConfigBitSet[partIndex].get(configIndex)) {
						int varIndex = batchIndex * numConfigs + configIndex;
						arrCoeff.add(1d);
						arrIndex.add(varIndex);
					}
				}
				String partName = partListHeader.get(partIndex);
				int batchPartVarIndex = numBatches * numConfigs + batchIndex
						* numParts + partIndex;
				arrCoeff.add(-bigM);
				arrIndex.add(batchPartVarIndex);

				int[] sparceVarIndex = ArrayToNativeInt(arrIndex);
				double[] sparceCoeff = ArrayToNativeDouble(arrCoeff);
				iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0,
						sparceVarIndex, sparceCoeff);
				LPMatrix.getRange(iRowIndex).setName(
						"B_" + batchIndex + "_P_" + partName +"_ConstConst");
			}
		}
		
	}

	public void addBatchPartandBatchFlagConsistConstWithExclusions(
			IloIntVar[] batchPartAndPartFlagVars,
			ArrayList<String> partListHeader,
			HashMap<String,String> partToCommodityMap, ArrayList<String> commoditiesToExclude) throws IloException {
		int iRowIndex;
		final String constName = "BatchPartandBatchFlagConsistConst";
		final IloLPMatrix LPMatrix = cplexModel.addLPMatrix(constName);
		LPMatrix.addCols(batchPartAndPartFlagVars);

		for (int batchIndex = 0; batchIndex < numBatches; batchIndex++) {
			int[] arrIndex = new int[numParts + 1];
			double[] arrCoeff = new double[numParts + 1];
			for (int partIndex = 0; partIndex < numParts; partIndex++) 
			{
				if(commoditiesToExclude.contains(partToCommodityMap.get(partListHeader.get(partIndex))))
				{
					continue;
				}
				int batchPartVarIndex = batchIndex * numParts + partIndex;
				arrIndex[partIndex] = batchPartVarIndex;
				arrCoeff[partIndex] = 1d;
			}
			arrIndex[numParts] = numBatches * numParts + batchIndex;
			arrCoeff[numParts] = -bigM;
			iRowIndex = LPMatrix.addRow(-Double.MAX_VALUE, 0, arrIndex, arrCoeff);
			LPMatrix.getRange(iRowIndex).setName(
					"Batch_" + batchIndex + "_Part_Consistency");
		}
	}





	
}