package com.ninjarific.wirelessmapper.engine.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.data.PointDataObject;
import com.ninjarific.wirelessmapper.engine.data.ScanDataObject;
import com.ninjarific.wirelessmapper.engine.forces.AttractToDistanceConnection;
import com.ninjarific.wirelessmapper.engine.interfaces.ScanDataTaskInterface;
import com.ninjarific.wirelessmapper.entities.actors.ForceActor;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class AddScansTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "AddScansTask";
	private static final boolean DEBUG = true;

	private ScanDataTaskInterface mDataInterface;
	private DataManager mDataManager;

	private ArrayList<WifiScan> mStartScans;

	private boolean mCancelled = false;
	
	public AddScansTask(ScanDataTaskInterface dataInterface, ArrayList<WifiScan> startScans, DataManager dataManager) {
		if (DEBUG) Log.d(TAG, "task created for " + startScans.size() + " scans");
		mDataInterface = dataInterface;
		mDataManager = dataManager;
		mStartScans = startScans;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if (DEBUG) Log.d(TAG, "doInBackground()");
		ArrayList<WifiScan> currentScans = new ArrayList<WifiScan>();
		ArrayList<WifiScan> nextScans = new ArrayList<WifiScan>();
		Set<WifiScan> processedScans = new HashSet<WifiScan>();
		currentScans.addAll(mStartScans);

		while (!currentScans.isEmpty() && !mCancelled) {
			if (DEBUG) Log.d(TAG, "\t while loop iteration, " + currentScans.size() + " scans to process");
			for (WifiScan scan : currentScans) {
				// get or create data object for scan
				ScanDataObject scanData = mDataInterface.getScanDataObject(scan);
				if (scanData == null) {
					scanData = new ScanDataObject(scan, mDataManager);
					mDataInterface.addScanDataObject(scanData);
				}

				if (!scanData.allPointsProcessed()) {
					for (WifiConnectionData connection : scanData.getConnections()) {
						PointDataObject pointData = mDataInterface.getPointDataObject(connection.getPoint());
						if (pointData == null) {
							pointData = new PointDataObject(connection.getPoint(), mDataManager);
							mDataInterface.addPointDataObject(pointData);
						}
					}
				}
				
				scanData.finishedProcessingPoints();
				nextScans.addAll(scanData.getConnectedScans());
			}
			// remove already handled scans from prospective scans
			nextScans.removeAll(processedScans);
			// update set of processed scans
			processedScans.addAll(nextScans);
			// configure for next loop
			currentScans = nextScans;
			nextScans = new ArrayList<WifiScan>();
			
		}
		if (DEBUG) Log.d(TAG, "\t exited loop");
		
//		try {
//			Thread.sleep(1500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		return null;
	}

	@Override
	protected void onCancelled() {
		if (DEBUG) Log.d(TAG, "onCancelled()");
		mCancelled = true;
	}

	@Override
	protected void onPostExecute(Void result) {
		mDataInterface.onAddScansTaskComplete(this);
	}
	
	

}
