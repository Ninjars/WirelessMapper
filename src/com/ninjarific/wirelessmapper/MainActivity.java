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
import com.ninjarific.wirelessmapper.ui.MainFragment;
import com.ninjarific.wirelessmapper.ui.RootFragment;
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

        RootFragment frag = new MainFragment();
		setContentFragment(frag, false);
        
    }
	
	public DataManager getDataManager() {
		return mDataManager;
	}

	@Override
	protected void onStart() {
		if (DEBUG) Log.i(TAG, "onStart()");
		super.onStart();
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
    
	public void addScanListener(ScanListener l) {
		mScanListener.add(l);
	}	
    
	public void removeScanListener(ScanListener l) {
		mScanListener.remove(l);
	}
	
    public void showToastMessage(String message) {
		if (DEBUG) Log.i(TAG, "showToastMessage() " + message);
		if (mToast != null) {
			mToast.setText(message);
			mToast.setDuration(Toast.LENGTH_SHORT);
			mToast.show();
		} else {
			if (DEBUG) Log.w(TAG, "\t aborted - toast was null");
			
		}
    }
	
	public void setContentFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction ft =  getFragmentManager().beginTransaction();

		if (addToBackStack) {
			ft.replace(R.id.content, fragment);
			ft.addToBackStack(null);
		} else {
			ft.replace(R.id.content, fragment);
		}
		ft.commit();
	}

	public void onScanResult(WifiScan point) {
		if (DEBUG) Log.i(TAG, "onScanResult()");
		ArrayList<ScanListener> listenersToRemove = null;
		for (ScanListener l : mScanListener) {
			if (DEBUG) Log.i(TAG, "onScanResult() --> ScanListener");
			if (l == null) {
				if (listenersToRemove == null) {
					listenersToRemove = new ArrayList<ScanListener>();
				}
				listenersToRemove.add(l);
			} else {
				l.onScanResult(point);
			}
		}
		
		if (listenersToRemove != null) {
			for (ScanListener l : listenersToRemove) {
				mScanListener.remove(l);
			}
		}
	}  
    
}