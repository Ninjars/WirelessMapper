package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WifiScanResult")
public class WifiScanResult extends BaseModel<Long> {
	// http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html#Foreign-Collection
	@ForeignCollectionField(eager = true)
	ForeignCollection<WifiData> wifiData;
	
	protected WifiScanResult() {/* for ORMLite use */};
	
	public WifiScanResult(ForeignCollection<WifiData> data) {
		wifiData = data;
	}
	
	public ForeignCollection<WifiData> getWifiData() {
		return wifiData;
	}
}
