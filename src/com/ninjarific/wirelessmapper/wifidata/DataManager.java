package com.ninjarific.wirelessmapper.wifidata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        mMainActivity.registerReceiver(mBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	public void onStop() {
		mMainActivity.unregisterReceiver(mBroadCastReceiver);	
	}

	public void onResume() {
		mMainActivity.registerReceiver(mBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));	
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
		WifiScan scan = checkForScanMerge(scanResults);
		
    	mMainActivity.onScanResult(scan);
	}
	
	private WifiScan checkForScanMerge(List<ScanResult> scanResults) {
		if (DEBUG) Log.i(TAG, "checkForScanMerge()");
		ArrayList<WifiPoint> newPoints = new ArrayList<WifiPoint>();
		Set<WifiPoint> existingPoints = new HashSet<WifiPoint>();
		WifiScan scan = null;
		
		if (DEBUG) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t scan results:");
			for (ScanResult sr : scanResults) {
				sb.append("\n\t\t " + sr.toString());
			}
			Log.v(TAG, sb.toString());
		}

		if (DEBUG) Log.i(TAG, "\t processing scan results");
		for (ScanResult result : scanResults) {
			if (-result.level < Constants.SCAN_CONNECTION_THREASHOLD) {
				if (DEBUG) Log.d(TAG, "\t ignoring scan result " + result.SSID + ": level below threashold (" + -result.level + ")");
				continue;
			}
			if (DEBUG) Log.d(TAG, "\t checking scan result " + result.SSID);
			
			List<WifiPoint> matchingPoints = lookupInstancesOfWifiPoint(result.SSID, result.BSSID);
			WifiPoint point = null;
			if (matchingPoints.size() == 0) {
				if (DEBUG) Log.d(TAG, "\t no matches found, creating new point");
				// create new point to be added to database and connected either with this 
				// scan or a scan that is found to overlap this one
				point = new WifiPoint(result);
				newPoints.add(point);
				
			} else {
				if (DEBUG) Log.d(TAG, "\t match found, using existing point" + matchingPoints.get(0));
				point = matchingPoints.get(0);
				point.setLevel(result.level);
				existingPoints.add(point);
			}
		}

		if (existingPoints.size() == 0) {
			// no existing points found, so this scan is all new and no comparison needs to be run
			if (DEBUG) Log.i(TAG, "\t no existing point matches found; creating new scan");
			scan = new WifiScan();
			addPointsForScan(scan, existingPoints, newPoints, true);
			
		} else {
			if (DEBUG) Log.i(TAG, "\t existing points found, looking for matching scan");
			scan = getWifiScanMatchFromPoints(new ArrayList<WifiPoint>(existingPoints));
			if (scan == null) {
				// no matching scan was found! This means the new scan is 
				// sufficiently different to warrant a new set of connections!
				if (DEBUG) Log.i(TAG, "\t no overlap found; creating new scan for all points");
				scan = new WifiScan();
				addPointsForScan(scan, existingPoints, newPoints, true);
			
			} else {
				// matching scan found!  Existing points' connections are 
				// sufficient, just add the new points to the matching scan
				if (DEBUG) Log.i(TAG, "\t matching scan found; adding new point connections to found scan");
				addPointsForScan(scan, existingPoints, newPoints, false);
			}
		}
		
		return scan;
		
	}
	
	/**
	 * returns the first scan found that matches the points
	 * @param points
	 * @return
	 */
	private WifiScan getWifiScanMatchFromPoints(List<WifiPoint> points) {
		if (DEBUG) Log.i(TAG, "getWifiScanMatchFromPoints()");
		List<List<WifiScanPointData>> matchingData = new ArrayList<List<WifiScanPointData>>();
		Set<WifiScanPointData> foundConnections = new HashSet<WifiScanPointData>();
		// assemble scans for each point that match the signal strength found by this scan's connections
		for (WifiPoint point : points) {
			if (point.getLevel() < Constants.SCAN_CONNECTION_THREASHOLD) {
				Log.e(TAG, "getWifiScanMatchFromPoints() passed point with level beneath threashold at " + point.getLevel());
				continue;
			}
			List<WifiScanPointData> connections = getConnectionsForPoint(point);
			if (DEBUG) Log.d(TAG, "\t found " + connections.size() + " connections for point " + point);
			int min = point.getLevel() - Constants.POINT_LEVEL_SIGNIFICANT_VARIATION;
			int max = point.getLevel() + Constants.POINT_LEVEL_SIGNIFICANT_VARIATION;
			List<WifiScanPointData> matchingConnections = new ArrayList<WifiScanPointData>();
			for (WifiScanPointData connection : connections) {
				if (connection.getLevel() > min && connection.getLevel() < max) {
					if (DEBUG) Log.d(TAG, "\t found matching connection " + connection);
					matchingConnections.add(connection);
					foundConnections.add(connection);
				}
			}
			
			if (matchingConnections.size() > 0) {
				matchingData.add(matchingConnections);
			}
		}
		
		// see if there is a common scan found in all points' connections at a common signal strength
		if (DEBUG) Log.d(TAG, "\t checking for common scan");

		if (DEBUG) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t found connections:");
			for (WifiScanPointData sc : foundConnections) {
				sb.append("\n\t\t " + sc.toString());
			}
			Log.v(TAG, sb.toString());
		}
		
		for (WifiScanPointData connection : foundConnections) {
			boolean match = false;
			if (DEBUG) Log.d(TAG, "\t checking connection " + connection);
			for (List<WifiScanPointData> connectionData : matchingData) {
				
				if (DEBUG) {
					StringBuilder sb = new StringBuilder();
					sb.append("\t checking vs: ");
					for (WifiScanPointData sc : connectionData) {
						sb.append("\n\t\t " + sc.toString());
					}
					Log.v(TAG, sb.toString());
				}

				// check to see if there is a match in the list
				boolean matchInList = false;
				for (WifiScanPointData data : connectionData) {
					if (data.approximateConnectionMatch(connection)) {
						matchInList = true;
						if (DEBUG) Log.w(TAG, "\t " + data + " in list matches " + connection);
						break;
					}
				}
				
				// if there's no match in the list then break
				if (matchInList) {
					match = true;
					break;
				}
			}
			
			if (match == true) {
				if (DEBUG) Log.w(TAG, "\t match found! " + connection);
				return connection.getScan();
			}
		}
		
		return null;
	}

	private void addPointsForScan(WifiScan scan, Set<WifiPoint> existingPoints, List<WifiPoint> newPoints, boolean isNewScan) {
		if (DEBUG) Log.d(TAG, "addPointsForScan(): " + existingPoints.size() + " existing points, " + newPoints.size() + " new points");
		ArrayList<WifiScanPointData> connectionData = new ArrayList<WifiScanPointData>();
		for (WifiPoint point : existingPoints) {
			if (point.getLevel() != 0) {
				if (DEBUG) Log.d(TAG, "\t creating connection for point " + point);
				WifiScanPointData connection = new WifiScanPointData(scan, point, point.getLevel());
				connectionData.add(connection);
				if (DEBUG) Log.d(TAG, "\t connection created: \n\t\t" + connection);
			}
		}
		for (WifiPoint point : newPoints) {
			if (point.getLevel() != 0) {
				if (DEBUG) Log.d(TAG, "\t creating connection for point " + point);
				WifiScanPointData connection = new WifiScanPointData(scan, point, point.getLevel());
				connectionData.add(connection);
				if (DEBUG) Log.d(TAG, "\t connection created: \n\t\t" + connection);
			}
		}
		if (isNewScan) {
			mDatabaseHelper.insert(scan);
		}
		mDatabaseHelper.batchInsert(newPoints);
		mDatabaseHelper.batchInsert(connectionData);
	}

	public List<WifiScan> getAllWifiScanObjects() {
		return getAllScans();
	}
	
	/*
	 * returns all the wifi points picked up by a scan
	 */
	public List<WifiPoint> getPointsForScan(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "getPointsForScan() " + scan);
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
	
	/*
	 * returns all the connections related to a scan
	 */
	public List<WifiScanPointData> getConnectionsForScan(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "getConnectionsForScan()");
		List<WifiScanPointData> data = lookupConnectionsForScan(scan);
		if (DEBUG) if (data != null) {
			Log.i(TAG, "\t data size: " + data.size());
		} else {
			Log.i(TAG, "\t data is null");
		}
		return data;
	}
	
	/*
	 * returns all the connections related to a point
	 */
	public List<WifiScanPointData> getConnectionsForPoint(WifiPoint point) {
		if (DEBUG) Log.i(TAG, "getConnectionsForPoint() " + point);
		List<WifiScanPointData> data = lookupConnectionsForPoint(point);
		if (DEBUG) if (data != null) {
		} else {
			Log.w(TAG, "\t hit error - data is null!");
		}
		return data;
	}
	
	/*
	 * Convenience methods to build and run our prepared queries.
	 */

	private PreparedQuery<WifiScan> getAllScansQuery = null;
	private PreparedQuery<WifiPoint> wifiPointForScanQuery = null;
	private PreparedQuery<WifiScan> wifiScanForPointQuery = null;
	private PreparedQuery<WifiPoint> wifiPointInstancesQuery = null;
	private PreparedQuery<WifiScanPointData> connectionsForScanQuery = null;
	private PreparedQuery<WifiScanPointData> connectionsForPointQuery = null;
	
	private List<WifiScan> getAllScans() {
		if (getAllScansQuery == null) {
			getAllScansQuery = makeGetAllScansQuery();
		}
		try {
			return mDatabaseHelper.getDaoForModelClass(WifiScan.class).query(getAllScansQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
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

	private List<WifiPoint> lookupInstancesOfWifiPoint(String ssid, String bssid) {
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

	private List<WifiScanPointData> lookupConnectionsForScan(WifiScan scan) {
		if (connectionsForScanQuery == null) {
			connectionsForScanQuery = makeConnectionsForScanQuery();
		}
		try {
			connectionsForScanQuery.setArgumentHolderValue(0, scan);
			return mDatabaseHelper.getDaoForModelClass(WifiScanPointData.class).query(connectionsForScanQuery);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private List<WifiScanPointData> lookupConnectionsForPoint(WifiPoint point) {
		if (connectionsForPointQuery == null) {
			connectionsForPointQuery = makeConnectionsForPointQuery();
		}
		try {
			connectionsForPointQuery.setArgumentHolderValue(0, point);
			return mDatabaseHelper
					.getDaoForModelClass(WifiScanPointData.class)
					.query(connectionsForPointQuery);
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
		QueryBuilder<WifiScan, Long> queryBuilder = 
				(QueryBuilder<WifiScan, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiScan.class)
				.queryBuilder();
		try {
			return queryBuilder.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeGetAllScansQuery");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Build our query for all wifi points that were detected by a scan
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiPoint> makeWifiPointForScanQuery() {
		QueryBuilder<WifiScanPointData, Long> queryBuilder = 
				(QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiScanPointData.class)
				.queryBuilder();
		queryBuilder.selectColumns(WifiScanPointData.WIFI_POINT_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiScanPointData.WIFI_SCAN_ID_FIELD_NAME, userSelectArg);
			QueryBuilder<WifiPoint, Long> wifiDataQb = 
					(QueryBuilder<WifiPoint, Long>) mDatabaseHelper
					.getDaoForModelClass(WifiPoint.class)
					.queryBuilder();
			wifiDataQb.where().in(WifiPoint.ID_FIELD_NAME, queryBuilder);
			return wifiDataQb.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeWifiPointForScanQuery");
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * Build our query for all scans detected a point
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiScan> makeWifiScanForPointQuery() {
		QueryBuilder<WifiScanPointData, Long> queryBuilder = 
				(QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiScanPointData.class)
				.queryBuilder();
		queryBuilder.selectColumns(WifiScanPointData.WIFI_SCAN_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiScanPointData.WIFI_POINT_ID_FIELD_NAME, userSelectArg);
			QueryBuilder<WifiScan, Long> wifiDataQb = 
					(QueryBuilder<WifiScan, Long>) mDatabaseHelper
					.getDaoForModelClass(WifiScan.class)
					.queryBuilder();
			wifiDataQb.where().in(WifiScan.ID_FIELD_NAME, queryBuilder);
			return wifiDataQb.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeWifiScanForPointQuery");
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
		QueryBuilder<WifiPoint, Long> wifiDataQb = 
				(QueryBuilder<WifiPoint, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiPoint.class)
				.queryBuilder();
		try {
			wifiDataQb.where().eq(WifiPoint.SSID_FIELD_NAME, ssidSelectArg)
						.and().eq(WifiPoint.BSSID_FIELD_NAME, bssidSelectArg);
			return wifiDataQb.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeWifiPointInstancesQuery");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Build our query for all connections that exist for a scan
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiScanPointData> makeConnectionsForScanQuery() {
		QueryBuilder<WifiScanPointData, Long> queryBuilder = 
				(QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiScanPointData.class)
				.queryBuilder();
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiScanPointData.WIFI_SCAN_ID_FIELD_NAME, userSelectArg);
			return queryBuilder.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeConnectionsForScanQuery");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Build our query for all connections that exist for a point
	 */
	@SuppressWarnings("unchecked")
	private PreparedQuery<WifiScanPointData> makeConnectionsForPointQuery() {
		QueryBuilder<WifiScanPointData, Long> queryBuilder = 
				(QueryBuilder<WifiScanPointData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiScanPointData.class)
				.queryBuilder();

		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiScanPointData.WIFI_POINT_ID_FIELD_NAME, userSelectArg);
			return queryBuilder.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeConnectionsForScanQuery");
			e.printStackTrace();
			return null;
		}
	}
	
}
