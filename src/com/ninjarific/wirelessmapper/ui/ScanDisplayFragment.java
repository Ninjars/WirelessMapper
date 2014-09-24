package com.ninjarific.wirelessmapper.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.R;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.ui.views.GraphicsView;

public class ScanDisplayFragment extends RootFragment {
	private static final String TAG = "ScanDisplayFragment";
	private static final boolean DEBUG = Constants.DEBUG;

	private MainActivity mActivity;
	private GraphicsView mGraphicsView;
	private WifiScan mScan;
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		super.onCreateView(inflater, container, bundle);
		if (DEBUG) Log.i(TAG, "onCreateView()");
		
	    View view = inflater.inflate(R.layout.fragment_scan_display, container, false);
		mGraphicsView = (GraphicsView) view.findViewById(R.id.scan_display_graphics_view);
		mGraphicsView.setOnTouchListener(mGraphicsView);
		
	    return view;
	}
	
	public void setScanForDisplay(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "setScanForDisplay() " + scan);
		mScan = scan;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.i(TAG, "onResume()");
		if (mScan != null) {
			mGraphicsView.addWifiScan(mScan);
			mScan = null;
		}
		mGraphicsView.startEngine(mActivity);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mGraphicsView.onDestroy();
	}
	
}
