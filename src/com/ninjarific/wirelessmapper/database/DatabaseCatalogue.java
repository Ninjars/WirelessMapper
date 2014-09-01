package com.ninjarific.wirelessmapper.database;

import com.ninjarific.wirelessmapper.wifidata.WifiData;
import com.ninjarific.wirelessmapper.wifidata.WifiScanResult;

public class DatabaseCatalogue {
	public static final Class<?>[] modelClasses = new Class[] {
		WifiData.class,
		WifiScanResult.class,
	};
}
