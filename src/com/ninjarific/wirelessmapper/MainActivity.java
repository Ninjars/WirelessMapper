package com.ninjarific.wirelessmapper;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.ninjarific.wirelessmapper.database.DatabaseHelper;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.listeners.ScanListener;
import com.ninjarific.wirelessmapper.ui.DebugDataFragment;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private static final boolean DEBUG = true && Constants.DEBUG;
	
	private Toast mToast;
	private DataManager mDataManager;
	private ArrayList<ScanListener> mScanListener;
	private DatabaseHelper mDatabaseHelper;
	
	
	/* Called when the activity is first created. */
	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mScanListener = new ArrayList<ScanListener>();
		
		mDataManager = new DataManager(this);

		mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		mDatabaseHelper.getWritableDatabase();

		// toast for notes - we make a dummy one that we will reuse later, avoiding overlapping toasts
        mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        mToast.cancel();

        DebugDataFragment frag = new DebugDataFragment();
		setContentFragment(frag, false); // create main fragment and assign here
		mScanListener.add(frag);
        
    }
	
	public DataManager getDataManager() {
		return mDataManager;
	}

	@Override
	protected void onStart() {
		if (DEBUG) Log.i(TAG, "onStart()");
		super.onStart();
		mDataManager.startScan();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		mDataManager.onResume();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		mDataManager.onStop();
		super.onStop();
	}	
    
    public void showToastMessage(String message) {
		if (DEBUG) Log.i(TAG, "showToastMessage()");
		mToast.setText(message);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.show();
    }
	
	public void setContentFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction ft =  getFragmentManager().beginTransaction();

		if (addToBackStack) {
			ft.add(fragment, null);
			ft.addToBackStack(null);
		} else {
			ft.replace(R.id.content, fragment);
		}
		ft.commit();
	}

	public void onScanResult(WifiScan point) {
		if (DEBUG) Log.i(TAG, "onScanResult()");
		for (ScanListener l : mScanListener) {
			if (DEBUG) Log.i(TAG, "onScanResult() --> ScanListener");
			l.onScanResult(point);
		}
    	mToast.cancel();		
	}

	public void onDataSetChanged() {
		for (ScanListener l : mScanListener) {
			if (DEBUG) Log.i(TAG, "onDataSetChanged() --> ScanListener");
			l.onDataChanged();
		}
	}    
    
}