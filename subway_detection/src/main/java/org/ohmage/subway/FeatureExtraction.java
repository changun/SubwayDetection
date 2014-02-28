package org.ohmage.subway;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FeatureExtraction {
	static public List<DataPoint> getAllActivePoints(List<DataPoint> data){
		List<DataPoint> ret = new ArrayList<DataPoint>();
		for(DataPoint dp: data){
			if(dp.getMode().isActive()){
				ret.add(dp);
			}
		}
		return ret;
	}
	static public List<DataPoint> getActivePointsBeforeSubway(List<DataPoint> data){
		List<DataPoint> ret = new ArrayList<DataPoint>();
		for(DataPoint dp: data){
			if(dp.prev != null && !dp.prev.isSubwayEvent() && dp.isSubwayEvent()){
				// we found the beginning of a subway event
				// get all the active points within 10 minutes before that
				List<DataPoint> beforeSubwayPoints = dp.getPointsInPrevNSeconds(Constants.LOOKBACK_TIME);
				for(DataPoint p: beforeSubwayPoints){
					if(p.getMode().isActive())
						ret.add(p);
				}
			}
		}
		return ret;
	}
	
	static public Set<String> getStrongestNWiFiInAllPoints(List<DataPoint> data, int n){
		Set<String> ret = new HashSet<String>();
		for(DataPoint point : data){
		        ValueComparator bvc =  new ValueComparator(point.getWifis());
		        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
		        sorted_map.putAll(point.getWifis());
		        int i = 0;
		        for(String wifi: sorted_map.keySet()){
		        	//if(wifi.equals("00:00:00:00:00:00"))
		        	//	continue;
		        	if(i<n){
		        		ret.add(wifi);
		        		i++;
		        	}
		        	else{
		        		break;
		        	}
		        }
		}
		return ret;
	}
	static public Map<String, Double> getWiFiStrengthFeatures(DataPoint data, Set<String> wifis){
		Map<String, Double> ret = new HashMap<String, Double>();
		for(String wifi: wifis){
			if(data.getWifis().containsKey(wifi)){
				ret.put(wifi, 1.0);
				//ret.put(wifi, data.getWifis().get(wifi));
			}
			else{
				ret.put(wifi, 0.0);
				//ret.put(wifi, Constants.NO_SIGNAL_STRENGTH);
			}
		}
		return ret;
	}
	static public Map<String, Double> getWiFiStrengthDifferenceFeatures(DataPoint data, Set<String> wifis){
		Map<String, Double> ret = new HashMap<String, Double>();
		for(String wifi: wifis){
			double prevStrength = Constants.NO_SIGNAL_STRENGTH;
			double curStrength = Constants.NO_SIGNAL_STRENGTH;
			if(data.getWifis().containsKey(wifi)){
				curStrength = data.getWifis().get(wifi);
			}
			if(data.getPrev().getWifis().containsKey(wifi)){
				prevStrength = data.getPrev().getWifis().get(wifi);
			}
			int change = curStrength-prevStrength > 0  ? 1 : -1;
			if(curStrength-prevStrength == 0)
				change= 0 ;
			ret.put(wifi, (double) change  );
		}
		return ret;
	}
	static public String getTimeslice(DataPoint data){
		return ((Integer)(data.getTime().hourOfDay().get() / (24 / Constants.TIME_SLICES_IN_A_DAY))).toString();
	}
	static public Boolean isWeekday(DataPoint data){
		return data.getTime().dayOfWeek().get() < 6;
	}
	static class ValueComparator implements Comparator<String> {

	    Map<String, Double> base;
	    public ValueComparator(Map<String, Double> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
}
