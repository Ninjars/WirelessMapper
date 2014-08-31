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
//    	mWifiDataList.addAll(mWifiDataSource.processScanResults(scanResults)); // TODO: implement adding data to database
    	mMainActivity.onScanResult();
	}
	
	public List<WifiData> getWifiData() {
		return mWifiDataList;
	}

	public void onStop() {
		mMainActivity.unregisterReceiver(mBroadCastReceiver);	
	}
	
}
