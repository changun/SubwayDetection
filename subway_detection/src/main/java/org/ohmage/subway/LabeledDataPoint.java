package org.ohmage.subway;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.ohmage.models.OhmageUser;

public class LabeledDataPoint extends DataPoint{
	DataPoint dp;
	public Double getLat() {
		return dp.getLat();
	}
	public void setLat(Double lat) {
		dp.setLat(lat);
	}
	public Double getLng() {
		return dp.getLng();
	}
	public void setLng(Double lng) {
		dp.setLng(lng);
	}
	public Map<WiFi, Double> getWifis() {
		return dp.getWifis();
	}
	public void setWifis(Map<WiFi, Double> wifis) {
		dp.setWifis(wifis);
	}
	public MobilityState getMode() {
		return dp.getMode();
	}
	public void setMode(MobilityState mode) {
		dp.setMode(mode);
	}
	public DateTime getTime() {
		return dp.getTime();
	}
	public void setTime(DateTime time) {
		dp.setTime(time);
	}
	public OhmageUser getUser() {
		return dp.getUser();
	}
	public void setUser(OhmageUser user) {
		dp.setUser(user);
	}
	public DataPoint getNext() {
		return dp.getNext();
	}
	public void setNext(DataPoint next) {
		dp.setNext(next);
	}
	public DataPoint getPrev() {
		return dp.getPrev();
	}
	public void setPrev(DataPoint prev) {
		dp.setPrev(prev);
	}
	public boolean isSubwayEvent() {
		return dp.isSubwayEvent();
	}
	public List<DataPoint> getPointsInNextNSeconds(int n) {
		return dp.getPointsInNextNSeconds(n);
	}
	public DataPoint getSubwayEventInNextNSeconds(int n) {
		return dp.getSubwayEventInNextNSeconds(n);
	}
	public List<DataPoint> getPointsInPrevNSeconds(int n) {
		return dp.getPointsInPrevNSeconds(n);
	}
	public double getMeanWifiAPNumberInNextNSeconds(int n) {
		return dp.getMeanWifiAPNumberInNextNSeconds(n);
	}
	public boolean isBeforeSubway(){
		return this.beforeSubway;
	}
	final private boolean beforeSubway;
	LabeledDataPoint(DataPoint dp, boolean beforeSubway){
		this.dp = dp;
		this.beforeSubway = beforeSubway;
		if(dp.isSubwayEvent() && beforeSubway)
			throw new RuntimeException("a data point can't be a subway event and a before-subway at the same time!");
	}
}
