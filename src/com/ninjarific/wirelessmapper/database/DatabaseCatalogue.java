package com.ninjarific.wirelessmapper.database;

import com.ninjarific.wirelessmapper.database.orm.models.WifiData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScanPoint;

public class DatabaseCatalogue {
	public static final Class<?>[] modelClasses = new Class[] {
		WifiData.class,
		WifiScanPoint.class,
	};
}
