package configUtilities;

import java.util.ArrayList;
import java.util.BitSet;

import configsToBOM.explodeTool;
import configsToBOM.usageTool;

public class preProcessUtility {

	public preProcessUtility()
	{
		
	}

	public ArrayList<String> identifyCommoditiesToExclude(explodeTool bomBO,
			usageTool usageBO, configTool configBO, double minBatch) {
		// TODO Auto-generated method stub
		ArrayList<String> commoditiesToExclude = new ArrayList<String>();
		for(String commodity : usageBO.commodityToPartsMap.keySet())
		{
			for(String part : usageBO.commodityToPartsMap.get(commodity))
			{
				int bsHeaderIndex = bomBO.partListHeader.indexOf(part);
				BitSet tmpBitSet = (BitSet) bomBO.partByConfigBitSet[bsHeaderIndex].clone();
				double totalForecastVolumeForPart = 0;
				for(int i = tmpBitSet.nextSetBit(0); i > 0; tmpBitSet.nextSetBit(i + 1))
				{
					totalForecastVolumeForPart = totalForecastVolumeForPart + configBO.configTakeRate.get(i);
				}
				if(totalForecastVolumeForPart < minBatch)
				{
					commoditiesToExclude.add(commodity);
					break;
				}
			}
			
		}
		return commoditiesToExclude;
	}
}
