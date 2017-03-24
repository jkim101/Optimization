package configUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import utilities.FileWriter;
import utilities.TSVFileReader;

public class configTool {
	public HashMap<String,ArrayList<String>> famFtrMap;
	public HashMap<String, Integer> featureBitSetIndex;
	public ArrayList<String> bitFtrSetHeader;
	public BitSet[] ftrWiseBitSet;
	public BitSet[] configWiseBitSet;
	public HashMap<Integer, Double> configTakeRate;
	private int uniqueConfigs;
	private int numFeatures;
	private HashMap<String, Integer> pdoFamilyToVolumeFileFamilyIndexMap;
	private double totalavgWeeklyVol;
	public HashMap<String, String> ftrToFamMap;
	public HashMap<String, Double> countryTotalGBVolume;
	public HashMap<String, Double> countryUsedGBVolume;
	public configTool(TSVFileReader configFile, String fullFilePathConfig)
	{
		this.famFtrMap = new HashMap<String, ArrayList<String>>();
		this.ftrToFamMap = new HashMap<String, String>();
		this.featureBitSetIndex = new HashMap<String, Integer>();
		this.bitFtrSetHeader = new ArrayList<String>();
		this.configTakeRate = new HashMap<Integer, Double>();
		this.countryTotalGBVolume = new HashMap<String, Double>();
		this.countryUsedGBVolume = new HashMap<String, Double>();
		buildFeatureFamilyMap(configFile);
		buildBitSetHeader();
		buildConfigWiseBitSet(fullFilePathConfig);
		buildFtrWiseBitSet();
		configFile.close();
	}
	private void buildFeatureFamilyMapWithRates (TSVFileReader configFile)
	{
		famFtrMap = new HashMap<String, ArrayList<String>>();
		String[] families = configFile.getHeaders();
		for(int famIndex = 1; famIndex < families.length; famIndex++)
		{
			ArrayList<String> tempMap = new ArrayList<String>();
			famFtrMap.put(families[famIndex], tempMap);
		}
		String[] line;
		uniqueConfigs = 0;
		//Build Map of unique Features in Each Family
		while ((line = configFile.readLine()) != null)
		{
			for(int famIndex = 1; famIndex < line.length; famIndex++)
			{
				ArrayList<String> tempMap = famFtrMap.get(families[famIndex]);
				if(tempMap.contains(line[famIndex]) || line[famIndex].equals(""))
				{
					continue;
				}else{
					tempMap.add(line[famIndex]);
					famFtrMap.put(families[famIndex], tempMap);
				}
			}
			uniqueConfigs++;
		}
	}
	

	private void buildFeatureFamilyMap (TSVFileReader configFile)
	{
		famFtrMap = new HashMap<String, ArrayList<String>>();
		String[] families = configFile.getHeaders();
		for(int famIndex = 0; famIndex < families.length; famIndex++)
		{
			ArrayList<String> tempMap = new ArrayList<String>();
			famFtrMap.put(families[famIndex], tempMap);
		}
		String[] line;
		uniqueConfigs = 0;
		//Build Map of unique Features in Each Family
		while ((line = configFile.readLine()) != null)
		{
			for(int famIndex = 0; famIndex < line.length; famIndex++)
			{
				ArrayList<String> tempMap = famFtrMap.get(families[famIndex]);
				if(tempMap.contains(line[famIndex]) || line[famIndex].equals(""))
				{
					continue;
				}else{
					tempMap.add(line[famIndex]);
					famFtrMap.put(families[famIndex], tempMap);
					ftrToFamMap.put(line[famIndex], families[famIndex]);
				}
			}
			uniqueConfigs++;
		}
	}
	
	private void buildBitSetHeader()
	{
		int index = 0;
		numFeatures= 0;
 		for(String fam: famFtrMap.keySet())
		{
			for(String ftr : famFtrMap.get(fam))
			{
				featureBitSetIndex.put(ftr, index);
				bitFtrSetHeader.add(index, ftr);
				index++;
//				numFeatures++;
			}
		}
	}
	
	public void writeBitSetHeader (String fullOutputFilePath)
	{
		FileWriter writer = new FileWriter(fullOutputFilePath, false);
		for (int ftrIndex = 0; ftrIndex < bitFtrSetHeader.size(); ftrIndex++)
		{
			writer.write(bitFtrSetHeader.get(ftrIndex) + "\t");
		}
		writer.close();
	}

	private void buildConfigWiseBitSetWitRates(String fullFilePathConfig) {
		TSVFileReader configFile = new TSVFileReader(fullFilePathConfig);
		configWiseBitSet = new BitSet[uniqueConfigs];
		
		String[] line;
		int configIndex = 0;
		double rateSum = 0;
		//Build Map of unique Features in Each Family
		while ((line = configFile.readLine()) != null)
		{
			configWiseBitSet[configIndex] = new BitSet();
			configWiseBitSet[configIndex].set(0, bitFtrSetHeader.size(), false);
			configTakeRate.put(configIndex, Double.parseDouble(line[0]));
			rateSum = rateSum + Double.parseDouble(line[0]);
			for(int famIndex = 1; famIndex < line.length; famIndex++)
			{
				int bitSetIndex = bitFtrSetHeader.indexOf(line[famIndex]);
				configWiseBitSet[configIndex].set(bitSetIndex, true);
			}
			configIndex++;
		}
		if(rateSum != 1)
		{
			System.out.println("Warning: Sum of all config take rates equals: " + rateSum + " sum of rates should equal 1");
		}
	}
	
	private void buildConfigWiseBitSetWithRates(String fullFilePathConfig) {
		TSVFileReader configFile = new TSVFileReader(fullFilePathConfig);
		configWiseBitSet = new BitSet[uniqueConfigs];
		
		String[] line;
		int configIndex = 0;
		double rateSum = 0;
		//Build Map of unique Features in Each Family
		while ((line = configFile.readLine()) != null)
		{
			configWiseBitSet[configIndex] = new BitSet();
			configWiseBitSet[configIndex].set(0, bitFtrSetHeader.size(), false);
			rateSum = rateSum + Double.parseDouble(line[0]);
			for(int famIndex = 0; famIndex < line.length; famIndex++)
			{
				int bitSetIndex = bitFtrSetHeader.indexOf(line[famIndex]);
				configWiseBitSet[configIndex].set(bitSetIndex, true);
			}
			configIndex++;
		}
		if(rateSum != 1)
		{
			System.out.println("Warning: Sum of all config take rates equals: " + rateSum + " sum of rates should equal 1");
		}
	}
	
	private void buildConfigWiseBitSet(String fullFilePathConfig) {
		TSVFileReader configFile = new TSVFileReader(fullFilePathConfig);
		configWiseBitSet = new BitSet[uniqueConfigs];
		
		String[] line;
		int configIndex = 0;

		//Build Map of unique Features in Each Family
		while ((line = configFile.readLine()) != null)
		{
			configWiseBitSet[configIndex] = new BitSet();
			configWiseBitSet[configIndex].set(0, bitFtrSetHeader.size(), false);
			for(int famIndex = 0; famIndex < line.length; famIndex++)
			{
				int bitSetIndex = bitFtrSetHeader.indexOf(line[famIndex]);
				configWiseBitSet[configIndex].set(bitSetIndex, true);
			}
			configIndex++;
		}
	}
	
	private void buildFtrWiseBitSet()
	{
		ftrWiseBitSet = new BitSet[featureBitSetIndex.keySet().size()];
		for(String ftrName : featureBitSetIndex.keySet())
		{
			int ftrIndex = featureBitSetIndex.get(ftrName);
			ftrWiseBitSet[ftrIndex] = new BitSet();
			ftrWiseBitSet[ftrIndex].set(0, configWiseBitSet.length, false);
			for(int configIndex = 0; configIndex< configWiseBitSet.length; configIndex++)
			{
				if(configWiseBitSet[configIndex].get(ftrIndex))
				{
					ftrWiseBitSet[ftrIndex].set(configIndex,true);
//					System.out.println("Config: " + configIndex + " \tftrIndex\t" + ftrIndex + "\tftr Name\t" + ftrName);
				}
			}
		}
	}
	
	public void writeConfigBitSet(String fullOutputFilePath)
	{
		FileWriter writer = new FileWriter(fullOutputFilePath, false);
		for (int ftrIndex = 0; ftrIndex < bitFtrSetHeader.size(); ftrIndex++)
		{
			writer.write(bitFtrSetHeader.get(ftrIndex) + "\t");
		}
		writer.write("\n");
		for(BitSet configBitSet:configWiseBitSet)
		{
			for (int ftrIndex = 0; ftrIndex < bitFtrSetHeader.size(); ftrIndex++)
			{
				if(configBitSet.get(ftrIndex))
				{
					writer.write( "1"+ "\t");					
				}else{
					writer.write( "0"+ "\t");
				}
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	public void establishVolumesFromOrderData(TSVFileReader orderReader)
	{
		double marketAWV = getAvgWeekVolFromCompressedOrderFile(orderReader);
		totalavgWeeklyVol = totalavgWeeklyVol + marketAWV;
		orderReader.close();
		System.out.println("Market:\t" +  "\tVol:\t" + marketAWV);
	}
	/**
	 * 
	 * @param folderPath
	 */
	public void establishRatesFromGearboxFiles(String folderPath) {
		// TODO Auto-generated method stub
		for(String countryCode : famFtrMap.get("SUP"))
		{
			countryTotalGBVolume.put(countryCode, 0.0);
			countryUsedGBVolume.put(countryCode, 0.0);
		}
		File dir = new File(folderPath);
		File[] directoryListing = dir.listFiles();
		totalavgWeeklyVol = 0;
		for(File file : directoryListing)
		{
			String market = "";
			String vlCode = "";
			String[] parseFileName = file.getName().split("_");
			if(parseFileName.length < 2)
			{
				System.out.println("File Name: " + file.getName() + " is in different format than expected.");
				System.out.println("Expected VLC_MARKET");
			}else{
				market = parseFileName[1];
				vlCode = parseFileName[0];
			}
			TSVFileReader gbReader = new TSVFileReader(folderPath + file.getName());
			double marketAWV = getAvgWeekVolFromGCPFile(gbReader);
			totalavgWeeklyVol = totalavgWeeklyVol + marketAWV;
			gbReader.close();
			System.out.println("Market:\t" + market + "\tVol:\t" + marketAWV);
		}
		
		System.out.println("Average Weekly Vol. Across All Markets:\t" + totalavgWeeklyVol);

		//keep running total of rates
		
		//create a bitset from gearbox
		//match bitset gearbox to pdo config
		//create a map configID to sum of rates
		
	}
	private double getAvgWeekVolFromGCPFile(TSVFileReader gbReader) {
		ArrayList<String> gearboxHeaderIndex = new ArrayList<String>();
		double gbRateTotal = 0;
		double unUsedGBRateTotal = 0;
		for(String headerVal : gbReader.getHeaders())
		{
			gearboxHeaderIndex.add(headerVal);
		}
		int indexOfWeeklyVol = gearboxHeaderIndex.indexOf("wt");
		pdoFamilyToVolumeFileFamilyIndexMap = new HashMap<String, Integer>();
		for(String pdoFamily: famFtrMap.keySet())
		{
			String modifiedFamilyName = pdoFamily.replace("-", "_");
			modifiedFamilyName = modifiedFamilyName + "_";
			int indexInGearbox = gearboxHeaderIndex.indexOf(modifiedFamilyName);
			if(indexInGearbox == -1)
			{
				System.out.println("!!!!!!!!!!!!!! FAMILY NOT FOUND IN GEARBOX FILE!!!!!!!!!!!!!");
				System.out.println("pdo family: " + pdoFamily + " Modified: " + modifiedFamilyName);
			}
			pdoFamilyToVolumeFileFamilyIndexMap.put(pdoFamily, indexInGearbox);
		}
		
		String[] line;
		ArrayList<String> lstFeaturesMissingInPDO = new ArrayList<String>();
		int indexOfSUPinGB = pdoFamilyToVolumeFileFamilyIndexMap.get("SUP");
		int gearboxLine = 0;
		while ((line = gbReader.readLine()) != null)
		{
			BitSet gbConfigBitSet = new BitSet();
			gbConfigBitSet.set(0, numFeatures, false);
			// keep running total of weights in GB
			gbRateTotal = gbRateTotal + Double.parseDouble(line[indexOfWeeklyVol]);
			double gbRate = Double.parseDouble(line[indexOfWeeklyVol]);
			String countryCode = line[indexOfSUPinGB];
			updateRunningTotalByCountry(countryCode, gbRate, countryTotalGBVolume);
			// convert gb config into relevant bit set
			for(String fam : pdoFamilyToVolumeFileFamilyIndexMap.keySet())
			{
				int indexOfFam = pdoFamilyToVolumeFileFamilyIndexMap.get(fam);
				if (indexOfFam == -1)
				{
						continue;
				}
				String ftrInGB = line[indexOfFam].replace("_", "-");

//				System.out.println(ftrInGB);
				if(!featureBitSetIndex.containsKey(ftrInGB))
				{
					if(!lstFeaturesMissingInPDO.contains(ftrInGB))
					{
						lstFeaturesMissingInPDO.add(ftrInGB);
					}
					continue;
				}
				int ftrHeaderIndex = featureBitSetIndex.get(ftrInGB);
				gbConfigBitSet.set(ftrHeaderIndex, true);
			}
			int gbBitSetCard = gbConfigBitSet.cardinality();
			int configBitSetCard = configWiseBitSet[0].cardinality();
			//identiy which pdo configs match the gb confing
			ArrayList<Integer> matchedConfigs = configsMatchingGBConfig(gbConfigBitSet);
			if(matchedConfigs.size()==0)
			{
				unUsedGBRateTotal = unUsedGBRateTotal + gbRate;
				if(gbBitSetCard == configBitSetCard)
				{
//					System.out.println("config buildable but not marketable");
				}else{
//					System.out.println("config excluded from PDO");
					updateRunningTotalByCountry(countryCode, -gbRate, countryTotalGBVolume);
				}

			}
			if(matchedConfigs.size()>1)
			{
//				System.out.println("1GB to multiple PDO");
				gbRate = gbRate / matchedConfigs.size();
			}
			for(int configId : matchedConfigs)
			{
				if(configTakeRate.containsKey(configId))
				{
					double currentRateTotal = configTakeRate.get(configId);
					configTakeRate.put(configId, gbRate + currentRateTotal);
				}else{
					configTakeRate.put(configId, gbRate);
				}
				updateRunningTotalByCountry(countryCode, gbRate, countryUsedGBVolume);
			}
//			System.out.println("-----");
			gearboxLine++;
		}
		System.out.println("Features Missing In Config Header");
		for(String ftrMissing : lstFeaturesMissingInPDO)
		{
			System.out.println(ftrMissing);
		}
		System.out.println("Un-Used GB Volume: " + unUsedGBRateTotal);
		return gbRateTotal;
	}
	
	private double getAvgWeekVolFromCompressedOrderFile(TSVFileReader orderReader) {
		ArrayList<String> orderFamilyHeaderIndex = new ArrayList<String>();
		double orderVolumeTotal = 0;
		double unUsedGBRateTotal = 0;
		int indexOfVolumeInFile = orderReader.getHeaders().length - 1;
		for(String headerVal : orderReader.getHeaders())
		{
			orderFamilyHeaderIndex.add(headerVal);
		}
		pdoFamilyToVolumeFileFamilyIndexMap = new HashMap<String, Integer>();
		
		for(String pdoFamily: famFtrMap.keySet())
		{
//			String modifiedFamilyName = pdoFamily.replace("-", "_");
			int indexInOrderFile = orderFamilyHeaderIndex.indexOf(pdoFamily);
//			if(indexInOrderFile == -1)
//			{
//				System.out.println("!!!!!!!!!!!!!! FAMILY NOT FOUND IN ORDER FILE!!!!!!!!!!!!!");
//				System.out.println("pdo family: " + pdoFamily + " Modified: " + modifiedFamilyName);
//			}
			pdoFamilyToVolumeFileFamilyIndexMap.put(pdoFamily, indexInOrderFile);
		}
		
		String[] line;
		ArrayList<String> lstFeaturesMissingInPDO = new ArrayList<String>();
		int orderFileLine = 0;
		while ((line = orderReader.readLine()) != null)
		{
			BitSet orderConfigBitSet = new BitSet();
			orderConfigBitSet.set(0, numFeatures, false);
			// keep running total of weights in GB
			orderVolumeTotal = orderVolumeTotal + Double.parseDouble(line[indexOfVolumeInFile]);
			double orderVolume = Double.parseDouble(line[indexOfVolumeInFile]);

//			updateRunningTotalByCountry(countryCode, gbRate, countryTotalGBVolume);
			// convert gb config into relevant bit set
			for(String fam : pdoFamilyToVolumeFileFamilyIndexMap.keySet())
			{
				int indexOfFam = pdoFamilyToVolumeFileFamilyIndexMap.get(fam);
				if (indexOfFam == -1)
				{
						continue;
				}
				String ftrInOrdFile = line[indexOfFam].replace("_", "-");

//				System.out.println(ftrInGB);
				if(!featureBitSetIndex.containsKey(ftrInOrdFile))
				{
					if(!lstFeaturesMissingInPDO.contains(ftrInOrdFile))
					{
						lstFeaturesMissingInPDO.add(ftrInOrdFile);
					}
					continue;
				}
				int ftrHeaderIndex = featureBitSetIndex.get(ftrInOrdFile);
				orderConfigBitSet.set(ftrHeaderIndex, true);
			}
			int gbBitSetCard = orderConfigBitSet.cardinality();
			int configBitSetCard = configWiseBitSet[0].cardinality();
			//identiy which pdo configs match the gb confing
			ArrayList<Integer> matchedConfigs = configsMatchingGBConfig(orderConfigBitSet);
			if(matchedConfigs.size()==0)
			{
				unUsedGBRateTotal = unUsedGBRateTotal + orderVolume;
				if(gbBitSetCard == configBitSetCard)
				{
//					System.out.println("config buildable but not marketable");
				}else{
//					System.out.println("config excluded from PDO");
//					updateRunningTotalByCountry(countryCode, -gbRate, countryTotalGBVolume);
				}

			}
			if(matchedConfigs.size()>1)
			{
				System.out.println("1 Order to multiple PDO");
				orderVolume = orderVolume / matchedConfigs.size();
			}
			for(int configId : matchedConfigs)
			{
				if(configTakeRate.containsKey(configId))
				{
					double currentRateTotal = configTakeRate.get(configId);
					configTakeRate.put(configId, orderVolume + currentRateTotal);
				}else{
					configTakeRate.put(configId, orderVolume);
				}
//				updateRunningTotalByCountry(countryCode, gbRate, countryUsedGBVolume);
			}
//			System.out.println("-----");
			orderFileLine++;
		}
		System.out.println("Features Missing In Config Header");
		for(String ftrMissing : lstFeaturesMissingInPDO)
		{
			System.out.println(ftrMissing);
		}
		System.out.println("Un-Used GB Volume: " + unUsedGBRateTotal);
		return orderVolumeTotal;
	}
	
	private void updateRunningTotalByCountry(String countryCode,double gbRate, HashMap<String,Double> hashMap) {
		if(!hashMap.containsKey(countryCode))
		{
			return;
		}
		Double tmpDbl = hashMap.get(countryCode) + gbRate;
		hashMap.put(countryCode, tmpDbl);
	}
		
	
	
	public void writeConfigTakeRateToFile(String filePath)
	{
		FileWriter writer = new FileWriter(filePath, false);
		for(int configId : configTakeRate.keySet())
		{
			writer.write(configId + "\t" + configTakeRate.get(configId) + "\n");
		}
		writer.close();
	}
	private ArrayList<Integer> configsMatchingGBConfig(BitSet gbConfigBitSet) {
		ArrayList<Integer> matchedConfigIDs = new ArrayList<Integer>();
		for(int configID = 0; configID < configWiseBitSet.length; configID++)
		{
			int gbBitSetCard = gbConfigBitSet.cardinality();
			int configBitSetCard = configWiseBitSet[configID].cardinality();
			if(gbBitSetCard < configBitSetCard)
			{
//				System.out.println("GB and PDO bit set not same card");
				continue;
			}
			BitSet tmpBitSet = (BitSet) configWiseBitSet[configID].clone();
			tmpBitSet.and(gbConfigBitSet);
			if(tmpBitSet.cardinality() == gbBitSetCard)
			{
				matchedConfigIDs.add(configID);
			}
		}
		return matchedConfigIDs;
	}
	public void reWeightConfigs() 
	{
		for(String country : countryTotalGBVolume.keySet())
		{
			double countryTotal = countryTotalGBVolume.get(country);
			double countryUsed = countryUsedGBVolume.get(country);
			double scale = countryTotal / countryUsed;
			System.out.println(country + "\t" + scale);
			int bitsetIndex = featureBitSetIndex.get(country);
			BitSet tmpCountryBitSet = (BitSet) ftrWiseBitSet[bitsetIndex].clone();
			
			for (int configId = tmpCountryBitSet.nextSetBit(0); configId >= 0; configId = tmpCountryBitSet.nextSetBit(configId+1)) 
			{
			    double originalRate = (configTakeRate.containsKey(configId)) ? configTakeRate.get(configId) : 0.0;
			    double scaledRate = originalRate * scale;
			    configTakeRate.put(configId, scaledRate);
			}
		}
	}
	
	public void writeVolumeByCountries()
	{
		for(String country : countryTotalGBVolume.keySet())
		{
			System.out.println("G" + country + "\t" + countryTotalGBVolume.get(country));
			int bitsetIndex = featureBitSetIndex.get(country);
			BitSet tmpCountryBitSet = (BitSet) ftrWiseBitSet[bitsetIndex].clone();;
			double runningTotal = 0.0;
			for (int configId = tmpCountryBitSet.nextSetBit(0); configId >= 0; configId = tmpCountryBitSet.nextSetBit(configId+1)) 
			{
				double configRate = (configTakeRate.containsKey(configId)) ? configTakeRate.get(configId) : 0.0;
				runningTotal = runningTotal + configRate;
			}
			System.out.println(country + "\t" + runningTotal);
		}
		System.out.println("---------");
	}
	
	
}
