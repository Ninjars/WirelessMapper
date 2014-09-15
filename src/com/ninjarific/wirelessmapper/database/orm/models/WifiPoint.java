package com.ninjarific.wirelessmapper.database.orm.models;

import android.net.wifi.ScanResult;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WifiData")
public class WifiPoint extends BaseModel<Long> {
	@SuppressWarnings("unused")
	private static final String TAG = "WifiData";
	public final static String SSID_FIELD_NAME = "ssid";
	public final static String BSSID_FIELD_NAME = "bssid";
	
	@DatabaseField(canBeNull = false)
	private String ssid;
	@DatabaseField(canBeNull = false)
	private String bssid;
	@DatabaseField(canBeNull = false)
	private int level;
	@DatabaseField(canBeNull = false)
	private int freq;
	
	protected WifiPoint() {/* for ORMLite use */}
	
	public WifiPoint(ScanResult result) {
		bssid = result.BSSID;
		ssid = result.SSID;
		level = result.level;
		freq = result.frequency;
	}

	@Override
	public String toString() {
		return getSsid() + " " + getBssid();
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
}
