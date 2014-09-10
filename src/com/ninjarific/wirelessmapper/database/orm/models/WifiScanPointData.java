package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.field.DatabaseField;

public class WifiScanPointData extends BaseModel<Long> {
	public final static String WIFI_SCAN_POINT_ID_FIELD_NAME = "user_id";
	public final static String DATA_SCAN_POINT_ID_FIELD_NAME = "post_id";
	// This is a foreign object which just stores the id from the User object in this table.
	@DatabaseField(foreign = true, columnName = WIFI_SCAN_POINT_ID_FIELD_NAME)
	WifiScanPoint scanPoint;

	// This is a foreign object which just stores the id from the Post object in this table.
	@DatabaseField(foreign = true, columnName = DATA_SCAN_POINT_ID_FIELD_NAME)
	WifiData scanData;
	
	public WifiScanPointData() {/* for ORMLite use */}
	
	public WifiScanPointData(WifiScanPoint point, WifiData data) {
		scanPoint = point;
		scanData = data;
	}
}
