package com.ninjarific.wirelessmapper.wifidata;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.database.DatabaseHelper;
import com.ninjarific.wirelessmapper.database.orm.models.WifiData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScanPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScanPointData;

public class DataManager {
	private static final String TAG = "DataManager";
	private static final boolean DEBUG = Constants.DEBUG;
    
	private MainActivity mMainActivity;

	// database
	private DatabaseHelper mDatabaseHelper;
	private BroadcastReceiver mBroadCastReceiver;

	private List<WifiData> mWifiDataList;

	private WifiManager mWifiManager;
	
	public DataManager(MainActivity activity) {
		mMainActivity = activity;
		// TODO: setup database
		
		// Wifi linkup
		mWifiManager = (WifiManager) mMainActivity.getSystemService(Context.WIFI_SERVICE);
		
		// turn on wifi if it is off
		enableWifi();
		
		// register to receive broadcast events from wifiManager system
		
		mBroadCastReceiver = new BroadcastReceiver() {			
        	@Override
            public void onReceive(Context c, Intent intent) {
        		if (DEBUG) Log.i(TAG, "onReceive() broadcast intent");
        		((MainActivity) c).getDataManager().onScanResults(mWifiManager.getScanResults());
            }
        };
	}
	
	private void enableWifi() {
		if (mWifiManager.isWifiEnabled() == false) {
			if (DEBUG) Log.i(TAG, "Enabling WiFi");
			mMainActivity.showToastMessage("Enabling WiFi");
			
		    mWifiManager.setWifiEnabled(true);
		}
	}
	
	public void startScan() {
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
		if (DEBUG) Log.i(TAG, "onScanResults()");
		WifiScanPoint point = new WifiScanPoint();
		mDatabaseHelper.insert(point);
		for (ScanResult result : scanResults) {
			WifiData scan = new WifiData(result);
			mDatabaseHelper.insert(scan);
			WifiScanPointData data = new WifiScanPointData(point, scan);
			mDatabaseHelper.insert(data);
		}
    	mMainActivity.onScanResult(point);
	}
	
	public List<WifiData> getWifiData() {
		return mWifiDataList;
	}

	public void onStop() {
		mMainActivity.unregisterReceiver(mBroadCastReceiver);	
	}
	
	
	// TODO:
	/*
	 * Convenience methods to build and run our prepared queries.
	 */

//	private PreparedQuery<Post> postsForUserQuery = null;
//	private PreparedQuery<User> usersForPostQuery = null;
//
//	private List<Post> lookupPostsForUser(User user) throws SQLException {
//		if (postsForUserQuery == null) {
//			postsForUserQuery = makePostsForUserQuery();
//		}
//		postsForUserQuery.setArgumentHolderValue(0, user);
//		return postDao.query(postsForUserQuery);
//	}
//
//	private List<User> lookupUsersForPost(Post post) throws SQLException {
//		if (usersForPostQuery == null) {
//			usersForPostQuery = makeUsersForPostQuery();
//		}
//		usersForPostQuery.setArgumentHolderValue(0, post);
//		return userDao.query(usersForPostQuery);
//	}
//
//	/**
//	 * Build our query for Post objects that match a User.
//	 */
//	private PreparedQuery<Post> makePostsForUserQuery() throws SQLException {
//		// build our inner query for UserPost objects
//		QueryBuilder<UserPost, Integer> userPostQb = userPostDao.queryBuilder();
//		// just select the post-id field
//		userPostQb.selectColumns(UserPost.POST_ID_FIELD_NAME);
//		SelectArg userSelectArg = new SelectArg();
//		// you could also just pass in user1 here
//		userPostQb.where().eq(UserPost.USER_ID_FIELD_NAME, userSelectArg);
//
//		// build our outer query for Post objects
//		QueryBuilder<Post, Integer> postQb = postDao.queryBuilder();
//		// where the id matches in the post-id from the inner query
//		postQb.where().in(Post.ID_FIELD_NAME, userPostQb);
//		return postQb.prepare();
//	}
//
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
