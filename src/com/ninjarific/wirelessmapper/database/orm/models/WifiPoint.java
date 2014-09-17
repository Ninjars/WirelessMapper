package com.ninjarific.wirelessmapper.database.orm.models;

import android.net.wifi.ScanResult;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WifiData")
public class WifiPoint extends BaseModel<Long> {
	public final static String SSID_FIELD_NAME = "ssid";
	public final static String BSSID_FIELD_NAME = "bssid";
	
	@DatabaseField(canBeNull = false)
	private String ssid;
	@DatabaseField(canBeNull = false)
	private String bssid;
	
	protected WifiPoint() {/* for ORMLite use */}
	
	public WifiPoint(ScanResult result) {
		bssid = result.BSSID;
		ssid = result.SSID;
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

	@Override
	public boolean equals(Object o) {
		return (o instanceof WifiPoint 
				&& this.getSsid().equals(((WifiPoint) o).getSsid())
				&& this.getBssid().equals(((WifiPoint) o).getBssid()));
	}
	
	
}
