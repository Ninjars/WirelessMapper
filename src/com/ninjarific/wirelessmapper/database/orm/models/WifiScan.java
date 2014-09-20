package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WifiScanPoint")
public class WifiScan extends BaseModel<Long> {
	
	public WifiScan() {/* for ORMLite use */};

	@Override
	public String toString() {
		return "<WifiScan " + this.getId() + ">";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof WifiScan 
				&& this.getId().equals(((WifiScan) o).getId()));
	}

	@Override
	public int hashCode() {
        int hash = 5;
        hash += this.getClass().hashCode();
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
	}
	
}
