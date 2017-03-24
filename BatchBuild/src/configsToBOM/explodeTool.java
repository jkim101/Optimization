package configsToBOM;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import utilities.FileWriter;
import utilities.TSVFileReader;

public class explodeTool {

	public ArrayList<String> fullOrderdPartList;
	public  ArrayList<String> partListHeader;
	public BitSet[] configByPartBitSet;
	public BitSet[] partByConfigBitSet;
	
	public explodeTool(ArrayList<String> fullOrderdPartList, ArrayList<String> partListHeader) {
		this.fullOrderdPartList = fullOrderdPartList;
		this.partListHeader = partListHeader;
		
	}

	public boolean partOnConfig (BitSet configWiseBitSet, BitSet partWiseBitSet)
	{
		BitSet tempConfig = (BitSet) configWiseBitSet.clone();
		BitSet tempPart = (BitSet) partWiseBitSet.clone();
		int partCard = tempPart.cardinality();
		if (partCard == 0)
		{
//			System.out.println("Part Has No Features");
			return false;
		}else{
			
			tempPart.and(tempConfig);
			if(tempPart.cardinality() == partCard)
			{
//				System.out.println(true);

				return true;
			}else{
//				System.out.println(false);
				return false;
			}
		}
	}
		public boolean partOnConfigV2 (BitSet configWiseBitSet, BitSet partWiseBitSet, int uniqueFamilies)
		{
			BitSet tempConfig = (BitSet) configWiseBitSet.clone();
			BitSet tempPart = (BitSet) partWiseBitSet.clone();
			int partCard = tempPart.cardinality();
			if (partCard == 0)
			{
//				System.out.println("Part Has No Features");
				return false;
			}else{
				
				tempPart.and(tempConfig);
				if(tempPart.cardinality() == uniqueFamilies)
				{
//					System.out.println(true);

					return true;
				}else{
//					System.out.println(false);
					return false;
				}
			}
	}
	
	public void writeExplodedConfig (BitSet[] configWiseBitSet, BitSet[] partWiseFtrBitSet, String fullOutPathExplodedConfig, ArrayList<Integer> uniqueFamiliesByUsage)
	{
		
		configByPartBitSet = new BitSet[configWiseBitSet.length];
		partByConfigBitSet = new BitSet[ partListHeader.size()];
		
		initializePartWiseBitSet(configWiseBitSet);
		
		FileWriter writer = new FileWriter(fullOutPathExplodedConfig, false);
		String[] configExplodedLine = new String[partListHeader.size() + 1];
		configExplodedLine[0] = "configId";
		for(int col = 1; col < configExplodedLine.length; col++)
		{
			configExplodedLine[col] = partListHeader.get(col -1);
		}
		
		writeLine(writer, configExplodedLine);
		
		for(int configIndex = 0; configIndex < configWiseBitSet.length; configIndex++)
		{
			configByPartBitSet[configIndex] = new BitSet();
			configByPartBitSet[configIndex].set(0, partListHeader.size(), false);
			
			configExplodedLine = new String[partListHeader.size() + 1];
			configExplodedLine[0] = String.valueOf(configIndex);
			for(int partUsageLine = 0; partUsageLine < partWiseFtrBitSet.length; partUsageLine++)
			{
				if(this.partOnConfigV2(configWiseBitSet[configIndex], partWiseFtrBitSet[partUsageLine], uniqueFamiliesByUsage.get(partUsageLine)))
				{
					String partIndexKey = fullOrderdPartList.get(partUsageLine);
					String[] splitPartIndexKey = partIndexKey.split("_");
					String partName = splitPartIndexKey[0] + "_" + splitPartIndexKey[1] + "_" + splitPartIndexKey[2];
					int headerIndexForPart = partListHeader.indexOf(partName);
					configExplodedLine[headerIndexForPart + 1] = "1";
					configByPartBitSet[configIndex].set(headerIndexForPart, true);
					partByConfigBitSet[headerIndexForPart].set(configIndex, true);
				}	
			}
			writeLine(writer, configExplodedLine);
		}
		writer.close();
	}
//
//	public void calcExplodedConfig (BitSet[] configWiseBitSet, BitSet[] partWiseFtrBitSet, int[] uniqueFamiliesByUsage)
//	{
//		
//		configByPartBitSet = new BitSet[configWiseBitSet.length];
//		partByConfigBitSet = new BitSet[ partListHeader.size()];
//		
//		initializePartWiseBitSet(configWiseBitSet);
//		
//		String[] configExplodedLine = new String[partListHeader.size() + 1];
//		configExplodedLine[0] = "configId";
//		for(int col = 1; col < configExplodedLine.length; col++)
//		{
//			configExplodedLine[col] = partListHeader.get(col -1);
//		}
//		
//		
//		for(int configIndex = 0; configIndex < configWiseBitSet.length; configIndex++)
//		{
//			configByPartBitSet[configIndex] = new BitSet();
//			configByPartBitSet[configIndex].set(0, partListHeader.size(), false);
//			
//			configExplodedLine = new String[partListHeader.size() + 1];
//			configExplodedLine[0] = String.valueOf(configIndex);
//			for(int partUsageLine = 0; partUsageLine < partWiseFtrBitSet.length; partUsageLine++)
//			{
//				if(this.partOnConfigV2(configWiseBitSet[configIndex], partWiseFtrBitSet[partUsageLine], uniqueFamiliesByUsage[partUsageLine]))
//				{
//					String partIndexKey = fullOrderdPartList.get(partUsageLine);
//					String[] splitPartIndexKey = partIndexKey.split("_");
//					String partName = splitPartIndexKey[0] + "_" + splitPartIndexKey[1] + "_" + splitPartIndexKey[2];
//					int headerIndexForPart = partListHeader.indexOf(partName);
//					configExplodedLine[headerIndexForPart + 1] = "1";
//					configByPartBitSet[configIndex].set(headerIndexForPart, true);
//					partByConfigBitSet[headerIndexForPart].set(configIndex, true);
//				}	
//			}
//		}
//	}
//	
	
	private void initializePartWiseBitSet(BitSet[] configWiseBitSet) {
		for(int partIndex = 0; partIndex< partListHeader.size(); partIndex++)
		{
			partByConfigBitSet[partIndex] = new BitSet();
			partByConfigBitSet[partIndex].set(0, configWiseBitSet.length, false);
		}
	}

	private void writeLine(FileWriter writer, String[] configExplodedLine) {
		for(String val : configExplodedLine)
		{
			if(val == null)
			{
				writer.write("0" + "\t");
			}else{
				writer.write(val + "\t");
			}
		}
		writer.write("\n");
	}
	
	public void writeConfigByPartBitSet(String fullOutputFilePath)
	{
		FileWriter writer = new FileWriter(fullOutputFilePath, false);
		writer.write("configIndex\t");
		for(int partIndex = 0; partIndex < partListHeader.size(); partIndex++)
		{
			writer.write(partListHeader.get(partIndex) +"\t");
		}
		writer.write("\n");
		
		for(int configIndex = 0; configIndex < configByPartBitSet.length; configIndex++)
		{
			writer.write(configIndex + "\t");
			for(int partIndex = 0; partIndex < partListHeader.size(); partIndex++)
			{
				if(configByPartBitSet[configIndex].get(partIndex))
				{
					writer.write("1" + "\t");
				}else{
					writer.write("0" + "\t");
				}
			}
			writer.write("\n");
		}
		writer.close();
	}	
	
	public void writePartByConfigBitSet(String fullOutputFilePath)
	{
		FileWriter writer = new FileWriter(fullOutputFilePath, false);
		writer.write("configIndex\t");
		for(int configIndex = 0; configIndex < configByPartBitSet.length; configIndex++)
		{
			writer.write(configIndex +"\t");
		}
		writer.write("\n");
		
		for(int partIndex = 0; partIndex < partListHeader.size(); partIndex++)
		{
			writer.write(partListHeader.get(partIndex) + "\t");
			for	(int configIndex = 0; configIndex < configByPartBitSet.length; configIndex++)
			{
				if(partByConfigBitSet[partIndex].get(configIndex))
				{
					writer.write("1" + "\t");
				}else{
					writer.write("0" + "\t");
				}
			}
			writer.write("\n");
		}
		writer.close();
	}	
}
