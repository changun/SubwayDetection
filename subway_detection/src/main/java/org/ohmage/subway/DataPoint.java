package org.ohmage.subway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.ohmage.models.OhmageUser;

public class DataPoint {

	Map<WiFi, Double> wifis = new HashMap<WiFi, Double>();
	
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
	public Map<WiFi, Double> getWifis() {
		return wifis;
	}
	public void setWifis(Map<WiFi, Double> wifis) {
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
			double avgAvailalbeWifi = this.getMeanWifiAPNumberInNextNSeconds(Constants.LOOKAHEAD_TIME);
			isSubwayEvent= ( avgAvailalbeWifi < Constants.MINIMUN_AVAILABLE_WIFI_FOR_NON_SUBWAY);
		}
		return isSubwayEvent;
	}
	// get the points within the next N seconds (include itself)
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
	// return the first subway even in the next N seconds
	public DataPoint getSubwayEventInNextNSeconds(int n){
		for(DataPoint dp: getPointsInNextNSeconds(n)){
			if(dp.isSubwayEvent())
				return dp;
		}
		return null;
	}
	// get the points within the previous  N seconds (not include itself)
	public List<DataPoint> getPointsInPrevNSeconds(int n){
		// use stack to store points to output the points in a reversed order
		Stack<DataPoint> stack = new Stack<DataPoint>();
		DataPoint pointer = this;
		while(pointer.prev != null){
			pointer = pointer.prev;
			if(pointer.time.plusSeconds(n).isAfter(this.getTime())){
				stack.push(pointer);
			}
			else{
				break;
			}
			
		}
		return (new ArrayList<DataPoint>(stack));
	}
	// get the average of the availalbe WiFi APs in the next N seconds
	public double getMeanWifiAPNumberInNextNSeconds(int n){
		List<DataPoint> points = this.getPointsInNextNSeconds(n);
		double totalApNumber = 0;
		for(DataPoint point: points){
			totalApNumber += point.getWifis().size();
		}
		return  totalApNumber / points.size();
	}
	
}
