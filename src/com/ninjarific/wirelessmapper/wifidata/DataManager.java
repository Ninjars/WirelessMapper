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
		// TODO: setup database
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
	 * returns all the hotspots picked up by a scan
	 */
	public List<WifiPoint> getPointsForScan(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "getPointsForScan()");
		try {
			List<WifiPoint> data = lookupWifiDataForScan(scan);
			if (DEBUG) Log.i(TAG, "\t data size: " + data.size());
			return data;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void onStop() {
		mMainActivity.unregisterReceiver(mBroadCastReceiver);	
	}
	
	
	// TODO:
	/*
	 * Convenience methods to build and run our prepared queries.
	 */

	private PreparedQuery<WifiScan> getAllScansQuery = null;
	private PreparedQuery<WifiPoint> wifiDataForScanQuery = null;
//	private PreparedQuery<User> usersForPostQuery = null;
	
	private List<WifiScan> getAllScans() throws SQLException {
		if (getAllScansQuery == null) {
			getAllScansQuery = makeGetAllScansQuery();
		}
		return mDatabaseHelper.getDaoForModelClass(WifiScan.class).query(getAllScansQuery);
	}
	
	private List<WifiPoint> lookupWifiDataForScan(WifiScan scan) throws SQLException {
		if (wifiDataForScanQuery == null) {
			wifiDataForScanQuery = makeWifiDataForScanQuery();
		}
		wifiDataForScanQuery.setArgumentHolderValue(0, scan);
		return mDatabaseHelper.getDaoForModelClass(WifiPoint.class).query(wifiDataForScanQuery);
	}

//	private List<User> lookupUsersForPost(Post post) throws SQLException {
//		if (usersForPostQuery == null) {
//			usersForPostQuery = makeUsersForPostQuery();
//		}
//		usersForPostQuery.setArgumentHolderValue(0, post);
//		return userDao.query(usersForPostQuery);
//	}

	/**
	 * Build our query for all wifi scan objects
	 */
	private PreparedQuery<WifiScan> makeGetAllScansQuery() throws SQLException {
		// build our inner query for UserPost objects
		QueryBuilder<WifiScan, Long> queryBuilder = (QueryBuilder<WifiScan, Long>) mDatabaseHelper.getDaoForModelClass(WifiScan.class).queryBuilder();
		return queryBuilder.prepare();
	}
	
	/**
	 * Build our query for all wifi points that were detected by a scan
	 */
	private PreparedQuery<WifiPoint> makeWifiDataForScanQuery() throws SQLException {
		// build our inner query for UserPost objects
		QueryBuilder<WifiScanPointData, Long> queryBuilder = (QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper.getDaoForModelClass(WifiScanPointData.class).queryBuilder();
		queryBuilder.selectColumns(WifiScanPointData.DATA_SCAN_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		queryBuilder.where().eq(WifiScanPointData.WIFI_SCAN_POINT_ID_FIELD_NAME, userSelectArg);

		QueryBuilder<WifiPoint, Long> wifiDataQb = (QueryBuilder<WifiPoint, Long>) mDatabaseHelper.getDaoForModelClass(WifiPoint.class).queryBuilder();
		// where the id matches in the post-id from the inner query
		wifiDataQb.where().in(WifiPoint.ID_FIELD_NAME, queryBuilder);
		return wifiDataQb.prepare();
	}

//	/**
//	 * Build our query for User objects that match a Post
//	 */
//	private PreparedQuery<User> makeUsersForPostQuery() throws SQLException {
//		QueryBuilder<UserPost, Integer> userPostQb = userPostDao.queryBuilder();
//		// this time selecting for the user-id field
//		userPostQb.selectColumns(UserPost.USER_ID_FIELD_NAME);
//		SelectArg postSelectArg = new SelectArg();
//		userPostQb.where().eq(UserPost.POST_ID_FIELD_NAME, postSelectArg);
//
//		// build our outer query
//		QueryBuilder<User, Integer> userQb = userDao.queryBuilder();
//		// where the user-id matches the inner query's user-id field
//		userQb.where().in(Post.ID_FIELD_NAME, userPostQb);
//		return userQb.prepare();
//	}
	
}
