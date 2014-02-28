package org.ohmage.subway_detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.ohmage.models.OhmageUser;

public class DataPoint {
	Map<String, Double> wifis = new HashMap<String, Double>();
	MobilityState mode;
	DateTime time;
	OhmageUser user;
	DataPoint next;
	DataPoint prev;
	Double lat;
	Double lng;
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLng() {
		return lng;
	}
	public void setLng(Double lng) {
		this.lng = lng;
	}
	public Map<String, Double> getWifis() {
		return wifis;
	}
	public void setWifis(Map<String, Double> wifis) {
		this.wifis = wifis;
	}
	public MobilityState getMode() {
		return mode;
	}
	public void setMode(MobilityState mode) {
		this.mode = mode;
	}
	public DateTime getTime() {
		return time;
	}
	public void setTime(DateTime time) {
		this.time = time;
	}
	public OhmageUser getUser() {
		return user;
	}
	public void setUser(OhmageUser user) {
		this.user = user;
	}
	public DataPoint getNext() {
		return next;
	}
	public void setNext(DataPoint next) {
		this.next = next;
	}
	public DataPoint getPrev() {
		return prev;
	}
	public void setPrev(DataPoint prev) {
		this.prev = prev;
	}
	private Boolean isSubwayEvent = null;
	public boolean isSubwayEvent(){
		if(isSubwayEvent == null){
			isSubwayEvent= (this.getMeanWifiAPNumberInNextNSeconds(Constants.LOOKAHEAD_TIME) < 1.0);
		}
		return isSubwayEvent;
	}
	
	// get the points within the next N seconds (include itsself)
	public List<DataPoint> getPointsInNextNSeconds(int n){
		List<DataPoint> points = new ArrayList<DataPoint>();
		DataPoint pointer = this;
		points.add(pointer);
		while(pointer.next != null){
			pointer = pointer.next;
			if(pointer.time.minusSeconds(n).isBefore(this.getTime())){
				points.add(pointer);
			}
			else{
				break;
			}
			
		}
		return points;
	}
	// get the points within the next N seconds (include itsself)
	public List<DataPoint> getPointsInPrevNSeconds(int n){
		List<DataPoint> points = new ArrayList<DataPoint>();
		DataPoint pointer = this;
		points.add(pointer);
		while(pointer.prev != null){
			pointer = pointer.prev;
			if(pointer.time.plusSeconds(n).isAfter(this.getTime())){
				points.add(pointer);
			}
			else{
				break;
			}
			
		}
		List<DataPoint> reverse = new ArrayList<DataPoint>();
		for(DataPoint point: points){
			reverse.add(null);
		}
		int i = points.size();
		for(DataPoint point: points){
			reverse.set(--i, point);
		}
		return (reverse);
	}
	public double getMeanWifiAPNumberInNextNSeconds(int n){
		List<DataPoint> points = this.getPointsInNextNSeconds(n);
		double totalApNumber = 0;
		for(DataPoint point: points){
			totalApNumber += point.getWifis().size();
		}
		return  totalApNumber / points.size();
	}
	
}
