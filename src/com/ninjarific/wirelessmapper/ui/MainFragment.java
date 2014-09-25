package com.ninjarific.wirelessmapper.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.R;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.listeners.ScanListener;

public class MainFragment extends RootFragment implements OnClickListener, ScanListener {
	private static final String TAG = "MainFragment";
	private static final boolean DEBUG = true && Constants.DEBUG;


	private MainActivity mActivity;
	private View mScanButtonView;
	private View mConnectButtonView;
	private View mDataButtonView;

	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		super.onCreateView(inflater, container, bundle);
	    View view = inflater.inflate(R.layout.fragment_main, container, false);
	    
	    mScanButtonView = view.findViewById(R.id.main_button_scan);
	    mScanButtonView.setOnClickListener(this);
	    
	    mConnectButtonView = view.findViewById(R.id.main_button_connect);
	    mConnectButtonView.setOnClickListener(this);
	    
	    mDataButtonView = view.findViewById(R.id.main_button_data);
	    mDataButtonView.setOnClickListener(this);
		
	    return view;
	}

	@Override
	public void onClick(View view) {
		if (view.equals(mScanButtonView)) {
			if (DEBUG) Log.d(TAG, "onClick() on Scan button");
			mActivity.getDataManager().startScan();
		
		} else if (view.equals(mConnectButtonView)) {
			if (DEBUG) Log.d(TAG, "onClick() on Connect button");
			// TODO: go to mapping view
			mActivity.addScanListener(this);
			mActivity.getDataManager().startScan();
			
		} else if (view.equals(mDataButtonView)) {
			if (DEBUG) Log.d(TAG, "onClick() on Data button");
			// TODO: database browser, for now just load the scan list view
	        ScanListFragment frag = new ScanListFragment();
			mActivity.setContentFragment(frag, true);
			mActivity.addScanListener(frag);
			
		}
		
	}

	@Override
	public void onScanResult(WifiScan scan) {
		mActivity.removeScanListener(this);
		// use returned scan as the connection point to existing scans
		ScanDisplayFragment frag = new ScanDisplayFragment();
		frag.addScans(mActivity.getDataManager().getAllScansConnectedToScan(scan));

		mActivity.setContentFragment(frag, true);
	}
}
