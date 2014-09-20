package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.field.DatabaseField;
import com.ninjarific.wirelessmapper.Constants;

public class WifiConnectionData extends BaseModel<Long> {
	public final static String WIFI_POINT_ID_FIELD_NAME = "point_id";
	public final static String WIFI_SCAN_ID_FIELD_NAME = "scan_id";
	public final static String LEVEL_ID_FIELD_NAME = "level_id";
	// This is a foreign object which just stores the id from the User object in this table.
	@DatabaseField(foreign = true, foreignAutoRefresh=true, columnName = WIFI_SCAN_ID_FIELD_NAME, canBeNull = false)
	WifiScan mScan;

	// This is a foreign object which just stores the id from the Post object in this table.
	@DatabaseField(foreign = true, foreignAutoRefresh=true, columnName = WIFI_POINT_ID_FIELD_NAME, canBeNull = false)
	WifiPoint mPoint;
	
	@DatabaseField(columnName = LEVEL_ID_FIELD_NAME, canBeNull = false)
	private int mLevel;
	
	public WifiConnectionData() {/* for ORMLite use */}
	
	public WifiConnectionData(WifiScan scan, WifiPoint point, int signalLevel) {
		mScan = scan;
		mPoint = point;
		mLevel = signalLevel;
	}
	
	public int getLevel() {
		return mLevel;
	}
	
	public WifiScan getScan() {
		return mScan;
	}
	
	public WifiPoint getPoint() {
		return mPoint;
	}
	
	@Override
	public String toString() {
		return "<Connection " + getId() + ": scan " + mScan.getId() + " to point " + mPoint.getId() + " " + mPoint.getSsid() + " @ " + mLevel + ">";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof WifiConnectionData 
				&& this.getScan().equals(((WifiConnectionData) o).getScan())
				&& this.getPoint().equals(((WifiConnectionData) o).getPoint())
				&& this.getLevel() == (((WifiConnectionData) o).getLevel()));
	}

	@Override
	public int hashCode() {
        int hash = 17;
        hash += this.getClass().hashCode();
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        hash += (this.getScan() != null ? this.getScan().hashCode() : 0);
        hash += (this.getPoint() != null ? this.getPoint().hashCode() : 0);
        hash += this.getLevel();
        return hash;
	}
	
	/*
	 * fuzzy equality to merge similar level connections to be treated as the same
	 */
	public boolean approximateConnectionMatch(WifiConnectionData data) {
		return (this.getScan().equals(data.getScan())
				&& this.getPoint().equals(data.getPoint())
				&& this.getLevel() - Constants.POINT_LEVEL_SIGNIFICANT_VARIATION < data.getLevel()
				&& this.getLevel() + Constants.POINT_LEVEL_SIGNIFICANT_VARIATION > data.getLevel()
				);
	}
	
}
