package com.ninjarific.wirelessmapper.wifidata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WifiData")
public class WifiData {
	@SuppressWarnings("unused")
	private static final String TAG = "WifiData";
	
	@DatabaseField(generatedId=true, canBeNull = false)
	private long id;
	@DatabaseField(canBeNull = false)
	private String ssid;
	@DatabaseField(canBeNull = false)
	private String bssid;
	@DatabaseField(canBeNull = false)
	private int level;
	@DatabaseField(canBeNull = false)
	private int freq;
	@DatabaseField
	private float accuracy;

	@Override
	public String toString() {
		return getSsid() + " " + getBssid();
	}
	
	public long getId() {
		return id;
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getBssid() {
		return bssid;
	}
	public void setBssid(String bssid) {
		this.bssid = bssid;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public float getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
}
