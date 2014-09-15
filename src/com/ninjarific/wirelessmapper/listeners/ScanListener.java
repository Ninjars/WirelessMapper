package com.ninjarific.wirelessmapper.listeners;

import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;

public interface ScanListener {
	public void onScanResult(WifiScan scan);
	public void onDataChanged();
}