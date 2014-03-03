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

import org.ohmage.subway.WiFi;

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
					if(p.getMode().isActive() && !p.isSubwayEvent())
						ret.add(p);
				}
			}
		}
		return ret;
	}
	
	static public Set<WiFi> getStrongestNWiFiInAllPoints(List<LabeledDataPoint> data, int n){
		Set<WiFi> ret = new HashSet<WiFi>();
		for(DataPoint point : data){
		        ValueComparator bvc =  new ValueComparator(point.getWifis());
		        TreeMap<WiFi,Double> sorted_map = new TreeMap<WiFi,Double>(bvc);
		        sorted_map.putAll(point.getWifis());
		        int i = 0;
		        for(WiFi wifi: sorted_map.keySet()){
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
	static public Map<WiFi, Double> getWiFiStrengthFeatures(DataPoint data, Set<WiFi> wifis){
		Map<WiFi, Double> ret = new HashMap<WiFi, Double>();
		for(WiFi wifi: wifis){
			if(data.getWifis().containsKey(wifi)){
				//ret.put(wifi, 1.0);
				ret.put(wifi, data.getWifis().get(wifi) / Math.abs(Constants.NO_SIGNAL_STRENGTH));
			}
			else{
				//ret.put(wifi, 0.0);
				ret.put(wifi, 0.0);
			}
		}
		return ret;
	}
	static public Map<WiFi, Double> getWiFiStrengthDifferenceFeatures(DataPoint data, Set<WiFi> wifis){
		Map<WiFi, Double> ret = new HashMap<WiFi, Double>();
		for(WiFi wifi: wifis){
			double prevStrength = Constants.NO_SIGNAL_STRENGTH;
			double curStrength = Constants.NO_SIGNAL_STRENGTH;
			if(data.getWifis().containsKey(wifi)){
				curStrength = data.getWifis().get(wifi);
			}
			if(data.getPrev().getWifis().containsKey(wifi)){
				prevStrength = data.getPrev().getWifis().get(wifi);
			}
			double change = (curStrength-prevStrength) / Math.abs(prevStrength) ;
			if(Math.abs(change) < 0.1)
				change= 0 ;
			ret.put(wifi, (double) change  );
		}
		return ret;
	}
	static public double getTime(DataPoint data){
		return data.getTime().getMinuteOfDay() / 1440.0 ;
	}
	static public Boolean isWeekday(DataPoint data){
		return data.getTime().dayOfWeek().get() < 6;
	}
	static class ValueComparator implements Comparator<WiFi> {

	    Map<WiFi, Double> base;
	    public ValueComparator(Map<WiFi, Double> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(WiFi a, WiFi b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
}
