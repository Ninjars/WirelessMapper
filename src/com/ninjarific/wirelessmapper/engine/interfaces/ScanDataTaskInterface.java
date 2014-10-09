package com.ninjarific.wirelessmapper.engine.interfaces;

import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.data.PointDataObject;
import com.ninjarific.wirelessmapper.engine.data.ScanDataObject;
import com.ninjarific.wirelessmapper.engine.tasks.AddScansTask;

public interface ScanDataTaskInterface {

	PointDataObject getPointDataObject(WifiPoint point);
	ScanDataObject getScanDataObject(WifiScan scan);

	void addPointDataObject(PointDataObject pointData);
	void addScanDataObject(ScanDataObject scanData);

	void onAddScansTaskComplete(AddScansTask addScansTask);

}
