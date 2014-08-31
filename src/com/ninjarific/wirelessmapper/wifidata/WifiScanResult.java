package com.ninjarific.wirelessmapper.wifidata;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WifiScanResult")
public class WifiScanResult {
	@DatabaseField(generatedId=true, canBeNull = false)
	private long id;
	
	// http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html#Foreign-Collection
	@ForeignCollectionField(eager = true)
	ForeignCollection<WifiData> wifiData;
	
	public long getId() {
		return id;
	}
	
	public ForeignCollection<WifiData> getWifiData() {
		return wifiData;
	}
}
