package org.ohmage.subway;

public class WiFi{
	final private String bssid;
	public WiFi(String bssid){
		this.bssid = bssid.toUpperCase();
	}
	public boolean equals(Object arg0) {
		return bssid.equals(arg0.toString());
	}
	public int hashCode() {
		return bssid.hashCode();
	}
	public String toString(){
		return bssid.toString();
	}
}