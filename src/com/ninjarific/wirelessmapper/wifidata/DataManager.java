package com.ninjarific.wirelessmapper.wifidata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.util.LongSparseArray;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.database.DatabaseHelper;
import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;

public class DataManager {
	private static final String TAG = "DataManager";
	private static final boolean DEBUG = Constants.DEBUG;
    
	private MainActivity mMainActivity;

	// database
	private DatabaseHelper mDatabaseHelper;
	private BroadcastReceiver mBroadCastReceiver;

	private WifiManager mWifiManager;
	
	private ScanState mScanState = ScanState.IDLE;
	
	private enum ScanState {
		IDLE,
		PENDING,
		SCANNING
	}
	
	public DataManager(MainActivity activity) {
		mMainActivity = activity;

		mDatabaseHelper = new DatabaseHelper(activity);
		
		// Wifi linkup
		mWifiManager = (WifiManager) mMainActivity.getSystemService(Context.WIFI_SERVICE);
		
		// register to receive broadcast events from wifiManager system
		
		mBroadCastReceiver = new BroadcastReceiver() {			
        	@Override
            public void onReceive(Context c, Intent intent) {
        		if (DEBUG) Log.i(TAG, "onReceive() broadcast intent " + intent.getAction());
        		final String action = intent.getAction();
        		if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
        			DataManager.this.onScanResults(mWifiManager.getScanResults());
        		}
        		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
        			DataManager.this.onReceiveStateChange(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
        		}
            }
        };
        mMainActivity.registerReceiver(mBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mMainActivity.registerReceiver(mBroadCastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
	}

	protected void onReceiveStateChange(int intExtra) {
		if (DEBUG) Log.i(TAG, "onReceiveStateChange() " + intExtra);
		switch (intExtra) {
		case WifiManager.WIFI_STATE_ENABLED:
			if (DEBUG) Log.d(TAG, "\t WIFI_STATE_ENABLED");
			onWifiReadyForScan();
			break;
		}
		
	}

	public void onStop() {
		mMainActivity.unregisterReceiver(mBroadCastReceiver);	
	}

	public void onResume() {
		mMainActivity.registerReceiver(mBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));	
	}
	
	private void toggleWifi() {
		if (mWifiManager.isWifiEnabled() == false) {
			if (DEBUG) Log.i(TAG, "Enabling WiFi");
		    mWifiManager.setWifiEnabled(true);
			mMainActivity.registerReceiver(mBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		} else {
			if (DEBUG) Log.d(TAG, "Toggling wifi");
		    mWifiManager.setWifiEnabled(false);
		    mWifiManager.setWifiEnabled(true);
		}
	}
	
	/*
	 * call to prepare wifi adapter for scan
	 */
	public void initiateScan() {
		if (DEBUG) Log.i(TAG, "startScan()");
		switch (mScanState) {
			case SCANNING: 
				mMainActivity.showToastMessage("Scan already running");
				break;
				
			case PENDING:
				mMainActivity.showToastMessage("Waiting for wifi adapter");
				break;
				
			case IDLE:
				toggleWifi();
				mScanState = ScanState.PENDING;
				break;
		}
	}
	
	private void onWifiReadyForScan() {
		if (mScanState == ScanState.PENDING) {
			if (DEBUG) Log.d(TAG, "\t start scan");
			mMainActivity.showToastMessage("Scanning");
			mWifiManager.startScan();
			mScanState = ScanState.SCANNING;
		}
	}
	
//	public void clearDatabase() {		
//		mMainActivity.showToastMessage("Clearing Database");
//		mDatabaseHelper.;
//		mWifiDataList.clear();
//		mMainActivity.onDataSetChanged();
//	}

	private void onScanResults(List<ScanResult> scanResults) {
		if (mScanState == ScanState.SCANNING) {
			if (DEBUG) Log.i(TAG, "onScanResults() count " + scanResults.size());
			WifiScan scan = checkForScanMerge(scanResults);
			
	    	mMainActivity.onScanResult(scan);
	    	mScanState = ScanState.IDLE;
    	
		} else {
			Log.i(TAG, "ignoring system scan");
		}
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
			if (-result.level < Constants.SCAN_CONNECTION_THRESHOLD) {
				if (DEBUG) Log.d(TAG, "\t ignoring scan result " + result.SSID + ": level below threashold (" + -result.level + ")");
				continue;
			}
			
			if (result.SSID == "" || result.BSSID == "") {
				if (DEBUG) Log.d(TAG, "\t ignoring scan result with empty SSID or BSSID");
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
				point.setLevel(100 + result.level); // results are -100 for max signal, 0 for min signal
				existingPoints.add(point);
			}
		}

		if (existingPoints.size() == 0) {
			// no existing points found, so this scan is all new and no comparison needs to be run
			if (DEBUG) Log.i(TAG, "\t no existing point matches found; creating new scan");
			mMainActivity.showToastMessage("complete new scan");
			scan = new WifiScan();
			addPointsForScan(scan, existingPoints, newPoints, true);
			
		} else {
			if (DEBUG) Log.i(TAG, "\t existing points found, looking for matching scan");
			scan = getWifiScanMatchFromPoints(new ArrayList<WifiPoint>(existingPoints));
			if (scan == null) {
				// no matching scan was found! This means the new scan is 
				// sufficiently different to warrant a new set of connections!
				if (DEBUG) Log.i(TAG, "\t no overlap found; creating new scan for all points");
				mMainActivity.showToastMessage("no overlap; new scan");
				scan = new WifiScan();
				addPointsForScan(scan, existingPoints, newPoints, true);
			
			} else {
				// matching scan found!  Existing points' connections are 
				// sufficient, just add the new points to the matching scan
				if (!newPoints.isEmpty()) {
					if (DEBUG) Log.i(TAG, "\t matching scan found; adding new point connections to found scan");
					mMainActivity.showToastMessage("appending " + newPoints.size() + " to old scan");
					addPointsForScan(scan, existingPoints, newPoints, false);
				} else {
					if (DEBUG) Log.i(TAG, "\t matching scan found; no new points to add");
					mMainActivity.showToastMessage("no new results");
				}
			}
		}
		
		return scan;
		
	}
	
	/**
	 * returns the first scan found that matches the points
	 * - all passed points must closely match with the connections to the same
	 * scan otherwise will return null.
	 * @param points
	 * @return
	 */
	private WifiScan getWifiScanMatchFromPoints(List<WifiPoint> points) {
		if (DEBUG) Log.i(TAG, "getWifiScanMatchFromPoints()");
		List<List<WifiConnectionData>> dataForEachPoint = new ArrayList<List<WifiConnectionData>>();
		Set<WifiConnectionData> foundConnections = new HashSet<WifiConnectionData>();
		
		// assemble scans for each point that match the signal strength found by this scan's connections
		for (WifiPoint point : points) {
			Set<WifiConnectionData> allConnectionsForPoint = getConnectionsForPoint(point);
			if (DEBUG) Log.d(TAG, "\t found " + allConnectionsForPoint.size() + " connections for point " + point);
			int min = point.getLevel() - Constants.POINT_LEVEL_SIGNIFICANT_VARIATION;
			int max = point.getLevel() + Constants.POINT_LEVEL_SIGNIFICANT_VARIATION;
			List<WifiConnectionData> matchingConnections = new ArrayList<WifiConnectionData>();
			for (WifiConnectionData connection : allConnectionsForPoint) {
				if (connection.getLevel() > min && connection.getLevel() < max) {
					if (DEBUG) Log.d(TAG, "\t found connection with similar level" + connection);
					matchingConnections.add(connection);
					foundConnections.add(connection);
				}
			}
			
			if (matchingConnections.size() > 0) {
				dataForEachPoint.add(matchingConnections);
			} else {
				if (DEBUG) Log.d(TAG, "\t found no connections with similar level");
				return null;
			}
		}
		
		// see if there is a common scan found in all points' connections at a common signal strength
		if (DEBUG) Log.d(TAG, "\t checking for common scan");

		if (DEBUG) {
			StringBuilder sb = new StringBuilder();
			sb.append("\t found connections:");
			for (WifiConnectionData sc : foundConnections) {
				sb.append("\n\t\t " + sc.toString());
			}
			Log.v(TAG, sb.toString());
		}
		
		Set<WifiScan> matchingScans = new HashSet<WifiScan>();
		for (WifiConnectionData connection : foundConnections) {
			WifiScan testingScan = connection.getScan();
			boolean scanIsValidForAllPoints = true;
			if (DEBUG) Log.d(TAG, "\t checking scan " + testingScan);
			for (List<WifiConnectionData> pointConnectionData : dataForEachPoint) {
				//see if each list has a connection that contains the scan
				boolean match = false;
				for (WifiConnectionData data : pointConnectionData) {
					if (data.getScan().equals(testingScan)) {
						if (DEBUG) Log.d(TAG, "\t found matching scan " + data.getScan());
						// this point has a connection to the scan being tested for
						match = true;
					}
				}
				
				// if a point doesn't have the scan in its connections, then move on to the next scan
				if (!match) {
					if (DEBUG) Log.d(TAG, "\t no matching scan found for " + testingScan);
					scanIsValidForAllPoints = false;
					break;
				}
			}
			
			if (scanIsValidForAllPoints) {
				if (DEBUG) Log.d(TAG, "\t scan matched for all point data");
				matchingScans.add(testingScan);
			}
		}
		
		// we now have a list of scans that could match.
		// check how much the scans overlap by; if we see too few of the matching scans' 
		// points, then make a new scan regardless
		if (DEBUG) Log.d(TAG, "\t found " + matchingScans.size() + " matching scans");
		for (WifiScan scan : matchingScans) {
			double match = getFractionOverlap(points, lookupWifiPointForScan(scan));
			if (DEBUG) Log.d(TAG, "\t match " + match + " for scan " + scan);
			if (match > Constants.SCAN_MATCH_THRESHOLD) {
				return scan;
			}
		}
		
		return null;
	}

	private double getFractionOverlap(List<WifiPoint> points, List<WifiPoint> matchingPoints) {
		int count = 0;
		for (WifiPoint point : matchingPoints) {
			if (points.contains(point)) {
				count ++;
			}
		}
		return matchingPoints.size() / (double) count;
	}

	private void addPointsForScan(WifiScan scan, Set<WifiPoint> existingPoints, List<WifiPoint> newPoints, boolean isNewScan) {
		if (DEBUG) Log.d(TAG, "addPointsForScan(): " + existingPoints.size() + " existing points, " + newPoints.size() + " new points");
		ArrayList<WifiConnectionData> connectionData = new ArrayList<WifiConnectionData>();
		for (WifiPoint point : newPoints) {
			if (point.getLevel() != 0) {
				if (DEBUG) Log.d(TAG, "\t creating connection for point " + point);
				WifiConnectionData connection = new WifiConnectionData(scan, point, point.getLevel());
				connectionData.add(connection);
				if (DEBUG) Log.d(TAG, "\t connection created: \n\t\t" + connection);
			}
		}
		
		if (isNewScan) {
			for (WifiPoint point : existingPoints) {
				if (point.getLevel() != 0) {
					if (DEBUG) Log.d(TAG, "\t creating connection for point " + point);
					WifiConnectionData connection = new WifiConnectionData(scan, point, point.getLevel());
					connectionData.add(connection);
					if (DEBUG) Log.d(TAG, "\t connection created: \n\t\t" + connection);
				}
			}
			
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
	public Set<WifiConnectionData> getConnectionsForScan(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "getConnectionsForScan()");
		Set<WifiConnectionData> data = lookupConnectionsForScan(scan);
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
	public Set<WifiConnectionData> getConnectionsForPoint(WifiPoint point) {
		if (DEBUG) Log.i(TAG, "getConnectionsForPoint() " + point);
		Set<WifiConnectionData> data = lookupConnectionsForPoint(point);
		if (DEBUG) {
			if (data != null) {
				Log.i(TAG, "\t data: " + data.toString());
				
			} else {
				Log.w(TAG, "\t hit error - data is null!");
			}
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
	private PreparedQuery<WifiConnectionData> connectionsForScanQuery = null;
	private PreparedQuery<WifiConnectionData> connectionsForPointQuery = null;
	
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

	private Set<WifiConnectionData> lookupConnectionsForScan(WifiScan scan) {
		if (connectionsForScanQuery == null) {
			connectionsForScanQuery = makeConnectionsForScanQuery();
		}
		try {
			connectionsForScanQuery.setArgumentHolderValue(0, scan);
			return new HashSet<WifiConnectionData>(mDatabaseHelper.getDaoForModelClass(WifiConnectionData.class).query(connectionsForScanQuery));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Set<WifiConnectionData> lookupConnectionsForPoint(WifiPoint point) {
		if (connectionsForPointQuery == null) {
			connectionsForPointQuery = makeConnectionsForPointQuery();
		}
		try {
			connectionsForPointQuery.setArgumentHolderValue(0, point);
			return new HashSet<WifiConnectionData>(mDatabaseHelper
					.getDaoForModelClass(WifiConnectionData.class)
					.query(connectionsForPointQuery));
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
		QueryBuilder<WifiConnectionData, Long> queryBuilder = 
				(QueryBuilder<WifiConnectionData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiConnectionData.class)
				.queryBuilder();
		queryBuilder.selectColumns(WifiConnectionData.WIFI_POINT_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiConnectionData.WIFI_SCAN_ID_FIELD_NAME, userSelectArg);
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
		QueryBuilder<WifiConnectionData, Long> queryBuilder = 
				(QueryBuilder<WifiConnectionData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiConnectionData.class)
				.queryBuilder();
		queryBuilder.selectColumns(WifiConnectionData.WIFI_SCAN_ID_FIELD_NAME);
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiConnectionData.WIFI_POINT_ID_FIELD_NAME, userSelectArg);
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
	private PreparedQuery<WifiConnectionData> makeConnectionsForScanQuery() {
		QueryBuilder<WifiConnectionData, Long> queryBuilder = 
				(QueryBuilder<WifiConnectionData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiConnectionData.class)
				.queryBuilder();
		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiConnectionData.WIFI_SCAN_ID_FIELD_NAME, userSelectArg);
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
	private PreparedQuery<WifiConnectionData> makeConnectionsForPointQuery() {
		QueryBuilder<WifiConnectionData, Long> queryBuilder = 
				(QueryBuilder<WifiConnectionData, Long>) mDatabaseHelper
				.getDaoForModelClass(WifiConnectionData.class)
				.queryBuilder();

		SelectArg userSelectArg = new SelectArg();
		try {
			queryBuilder.where().eq(WifiConnectionData.WIFI_POINT_ID_FIELD_NAME, userSelectArg);
			return queryBuilder.prepare();
		} catch (SQLException e) {
			Log.e(TAG, "failed to build makeConnectionsForScanQuery");
			e.printStackTrace();
			return null;
		}
	}

	public static LongSparseArray<Integer> getProcessedConnectionsForConnectionData(List<WifiConnectionData> connections) {
		if (DEBUG) Log.d(TAG, "getProcessedConnectionsForConnectionData()");
		LongSparseArray<Integer> data = new LongSparseArray<Integer>();
		for (WifiConnectionData connection : connections) {
			data.put(connection.getId(), connection.getLevel());
		}
		if (DEBUG) Log.d(TAG, "\t return data: " + data);
		return data;
	}

	public ArrayList<WifiScan> getAllScansConnectedToScan(WifiScan scan) {
		LinkedHashSet<WifiScan> connectedScans = new LinkedHashSet<WifiScan>();
		connectedScans.add(scan);
		
		int lastSize = 0;
		while (connectedScans.size() > lastSize) {
			lastSize = connectedScans.size();
			HashSet<WifiScan> newScans = new HashSet<WifiScan>();
			for (WifiScan s : connectedScans) {
				newScans.addAll(getScansConnectedToScan(s));
			}
			connectedScans.addAll(newScans);
		}
		
		// just to return the original scan as the first item in the list
		connectedScans.remove(scan);
		ArrayList<WifiScan> scanList = new ArrayList<WifiScan>();
		scanList.add(scan);
		scanList.addAll(connectedScans);
		return scanList;
	}
	
	private HashSet<WifiScan> getScansConnectedToScan(WifiScan scan) {
		HashSet<WifiScan> connectedScans = new HashSet<WifiScan>();
		connectedScans.add(scan);
		
		List<WifiPoint> points = getPointsForScan(scan);
		
		if (points != null) {
			for (WifiPoint point : points) {
				List<WifiScan> scans = getScansForPoint(point);
				if (scans != null) {
					connectedScans.addAll(scans);
				}
			}
		}
		
		return connectedScans;
	}
	
}
