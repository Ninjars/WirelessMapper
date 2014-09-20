package com.ninjarific.wirelessmapper.database;

import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;

public class DatabaseCatalogue {
	public static final Class<?>[] modelClasses = new Class[] {
		WifiPoint.class,
		WifiScan.class,
		WifiConnectionData.class
	};
}
