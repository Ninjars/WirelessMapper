package com.ninjarific.wirelessmapper.wifidata;

import java.sql.SQLException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.database.DatabaseHelper;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScanPointData;

public class DataManager {
	private static final String TAG = "DataManager";
	private static final boolean DEBUG = Constants.DEBUG;
    
	private MainActivity mMainActivity;

	// database
	private DatabaseHelper mDatabaseHelper;
	private BroadcastReceiver mBroadCastReceiver;

	private List<WifiPoint> mWifiDataList;

	private WifiManager mWifiManager;
	
	public DataManager(MainActivity activity) {
		mMainActivity = activity;

		mDatabaseHelper = new DatabaseHelper(activity);
		
		// Wifi linkup
		mWifiManager = (WifiManager) mMainActivity.getSystemService(Context.WIFI_SERVICE);
		
		// turn on wifi if it is off
		enableWifi();
		
		// register to receive broadcast events from wifiManager system
		
		mBroadCastReceiver = new BroadcastReceiver() {			
        	@Override
            public void onReceive(Context c, Intent intent) {
        		if (DEBUG) Log.i(TAG, "onReceive() broadcast intent");
        		DataManager.this.onScanResults(mWifiManager.getScanResults());
            }
        };
        mMainActivity.registerReceiver(mBroadCastReceiver,  new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	private void enableWifi() {
		if (mWifiManager.isWifiEnabled() == false) {
			if (DEBUG) Log.i(TAG, "Enabling WiFi");
			mMainActivity.showToastMessage("Enabling WiFi");
			
		    mWifiManager.setWifiEnabled(true);
		} else {
			if (DEBUG) Log.d(TAG, "Wifi already enabled");
		}
	}
	
	public void startScan() {
		if (DEBUG) Log.i(TAG, "startScan()");
		enableWifi();
		mMainActivity.showToastMessage("Scanning");
		mWifiManager.startScan();
	}
	
//	public void clearDatabase() {		
//		mMainActivity.showToastMessage("Clearing Database");
//		mDatabaseHelper.;
//		mWifiDataList.clear();
//		mMainActivity.onDataSetChanged();
//	}

	private void onScanResults(List<ScanResult> scanResults) {
		if (DEBUG) Log.i(TAG, "onScanResults() count " + scanResults.size());
		WifiScan scan = new WifiScan();
		mDatabaseHelper.insert(scan);
		for (ScanResult result : scanResults) {
			WifiPoint point = new WifiPoint(result);
			mDatabaseHelper.insert(point);
			WifiScanPointData data = new WifiScanPointData(scan, point);
			mDatabaseHelper.insert(data);
		}
		
    	mMainActivity.onScanResult(scan);
	}
	
	public List<WifiScan> getAllWifiScanObjects() {
		try {
			return getAllScans();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * returns all the wifi points picked up by a scan
	 */
	public List<WifiPoint> getPointsForScan(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "getPointsForScan()");
		List<WifiPoint> data = lookupWifiPointForScan(scan);
		if (DEBUG) if (data != null) {
			Log.i(TAG, "\t data size: " + data.size());
		} else {
			Log.i(TAG, "\t data is null");
		}
		return data;
	}
	
	/*
	 * returns all the scans that detected a point
	 */
	public List<WifiScan> getScansForPoint(WifiPoint point) {
		if (DEBUG) Log.i(TAG, "getScansForPoint()");
		List<WifiScan> data = lookupWifiScanForPoint(point);
		if (DEBUG) if (data != null) {
			Log.i(TAG, "\t data size: " + data.size());
		} else {
			Log.i(TAG, "\t data is null");
		}
		return data;
	}

	public void onStop() {
		mMainActivity.unregisterReceiver(mBroadCastReceiver);	
	}
	
	/*
	 * Convenience methods to build and run our prepared queries.
	 */

	private PreparedQuery<WifiScan> getAllScansQuery = null;
	private PreparedQuery<WifiPoint> wifiPointForScanQuery = null;
	private PreparedQuery<WifiScan> wifiScanForPointQuery = null;
	private PreparedQuery<WifiPoint> wifiPointInstancesQuery = null;
	
	private List<WifiScan> getAllScans() throws SQLException {
		if (getAllScansQuery == null) {
			getAllScansQuery = makeGetAllScansQuery();
		}
		return mDatabaseHelper.getDaoForModelClass(WifiScan.class).query(getAllScansQuery);
	}
	
	private List<WifiPoint> lookupWifiPointForScan(WifiScan scan) {
		if (wifiPointForScanQuery == null) {
			wifiPointForScanQuery = makeWifiPointForScanQuery();
		}
		try {
			wifiPointForScanQuery.setArgumentHolderValue(0, scan);
			return mDatabaseHelper.getDaoForModelClass(WifiPoint.class).query(wifiPointForScanQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<WifiScan> lookupWifiScanForPoint(WifiPoint point) {
		if (wifiScanForPointQuery == null) {
			wifiScanForPointQuery = makeWifiScanForPointQuery();
		}
		try {
			wifiScanForPointQuery.setArgumentHolderValue(0, point);
			return mDatabaseHelper.getDaoForModelClass(WifiScan.class).query(wifiScanForPointQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<WifiPoint> lookupInstancesOfWifiPointWithSSID(String ssid, String bssid) {
		if (wifiPointInstancesQuery == null) {
			wifiPointInstancesQuery = makeWifiPointInstancesQuery();
		}
		try {
			wifiPointInstancesQuery.setArgumentHolderValue(0, ssid);
			wifiPointInstancesQuery.setArgumentHolderValue(1, bssid);
			return mDatabaseHelper.getDaoForModelClass(WifiPoint.class).query(wifiPointInstancesQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Build our query for all wifi scan objects
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiScan> makeGetAllScansQuery() {
		QueryBuilder<WifiScan, Long> queryBuilder = (QueryBuilder<WifiScan, Long>) mDatabaseHelper.getDaoForModelClass(WifiScan.class).queryBuilder();
		try {
			return queryBuilder.prepare();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Build our query for all wifi points that were detected by a scan
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiPoint> makeWifiPointForScanQuery() {
		QueryBuilder<WifiScanPointData, Long> queryBuilder = (QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper.getDaoForModelClass(WifiScanPointData.class).queryBuilder();
		queryBuilder.selectColumns(WifiScanPointData.DATA_SCAN_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiScanPointData.WIFI_SCAN_POINT_ID_FIELD_NAME, userSelectArg);
			QueryBuilder<WifiPoint, Long> wifiDataQb = (QueryBuilder<WifiPoint, Long>) mDatabaseHelper.getDaoForModelClass(WifiPoint.class).queryBuilder();
			wifiDataQb.where().in(WifiPoint.ID_FIELD_NAME, queryBuilder);
			return wifiDataQb.prepare();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * Build our query for all scans detected a point
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiScan> makeWifiScanForPointQuery() {
		QueryBuilder<WifiScanPointData, Long> queryBuilder = (QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper.getDaoForModelClass(WifiScanPointData.class).queryBuilder();
		queryBuilder.selectColumns(WifiScanPointData.WIFI_SCAN_POINT_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiScanPointData.DATA_SCAN_ID_FIELD_NAME, userSelectArg);
			QueryBuilder<WifiScan, Long> wifiDataQb = (QueryBuilder<WifiScan, Long>) mDatabaseHelper.getDaoForModelClass(WifiScan.class).queryBuilder();
			wifiDataQb.where().in(WifiPoint.ID_FIELD_NAME, queryBuilder);
			return wifiDataQb.prepare();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * Build our query for all points that match
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiPoint> makeWifiPointInstancesQuery() {
		SelectArg ssidSelectArg = new SelectArg();
		SelectArg bssidSelectArg = new SelectArg();
		QueryBuilder<WifiPoint, Long> wifiDataQb = (QueryBuilder<WifiPoint, Long>) mDatabaseHelper.getDaoForModelClass(WifiPoint.class).queryBuilder();
		// where the id matches in the post-id from the inner query
		try {
			wifiDataQb.where().eq(WifiPoint.SSID_FIELD_NAME, ssidSelectArg)
						.and().eq(WifiPoint.BSSID_FIELD_NAME, bssidSelectArg);
			return wifiDataQb.prepare();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
