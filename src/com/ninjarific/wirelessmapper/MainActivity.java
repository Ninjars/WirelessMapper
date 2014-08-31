package com.ninjarific.wirelessmapper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.listeners.ScanListener;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class MainActivity extends Activity {
	private static final String TAG = "WifiTester";
	private static final boolean DEBUG = true && Constants.DEBUG;
	
	private Toast mToast;
	private DataManager mDataManager;
	private ScanListener mScanListener;
	private Intent mScanIntent;
	
	
	/* Called when the activity is first created. */
	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		mDataManager = new DataManager(this);

		// toast for notes - we make a dummy one that we will reuse later, avoiding overlapping toasts
        mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        mToast.cancel();

//		setContentFragment(null); // create main fragment and assign here
        
        // WIP start on a background scanning service
//        //create ScanService
//        mScanIntent = new Intent(this, ScanService.class);
//        //configure service
//        mScanIntent.setData(Uri.parse(dataUrl));
//        //start service - launches onHandleIntent()
//        this.startService(mScanIntent);
    }
	
	public DataManager getDataManager() {
		return mDataManager;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		mDataManager.onStop();
		super.onStop();
	}	
    
    public void showToastMessage(String message) {
		mToast.setText(message);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.show();
    }
	
	private void setContentFragment(Fragment fragment) {
		FragmentTransaction ft =  getFragmentManager().beginTransaction();
		ft.replace(R.id.content, fragment);
		ft.commit();
	}

	public void onScanResult() {
		if (DEBUG) Log.i(TAG, "onScanResults()");
		if (mScanListener != null) {
			if (DEBUG) Log.i(TAG, "onScanResults() --> ScanListener");
			mScanListener.onScanResult();
		}
    	mToast.cancel();		
	}

	public void onDataSetChanged() {
		if (mScanListener != null) {
			if (DEBUG) Log.i(TAG, "onScanResults() --> ScanListener");
			mScanListener.onDataChanged();
		}
		
	}    
    
}