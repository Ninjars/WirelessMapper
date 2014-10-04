package com.ninjarific.wirelessmapper.ui;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.R;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.listeners.ScanListener;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class ScanListFragment extends RootFragment implements OnClickListener, ScanListener, OnItemClickListener {
	private static final String TAG = "ScanListFragment";
	private static final boolean DEBUG = Constants.DEBUG;

	private MainActivity mActivity;
	private ArrayAdapter<WifiScan> mListAdapter;
	private ListView mListView;
	
	private DataManager mDataManager;
	private List<WifiScan> mWifiScans;	

	private Button mScanButton;
	private Button mClearButton;
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		super.onCreateView(inflater, container, bundle);
	    View view = inflater.inflate(R.layout.fragment_scan_list, container, false);
	    mListView = (ListView) view.findViewById(R.id.list);
	    mDataManager = mActivity.getDataManager();
		mWifiScans = mDataManager.getAllWifiScanObjects();
	    
		mScanButton = (Button) view.findViewById(R.id.buttonScan);
		mScanButton.setOnClickListener(this);
//		mClearButton = (Button) view.findViewById(R.id.buttonClear);
//		mClearButton.setOnClickListener(this);
		
		mListAdapter = new ArrayAdapter<WifiScan>(mActivity, R.layout.row, mWifiScans);
		
		mListView.setAdapter(this.mListAdapter);
		mListView.setOnItemClickListener(this);
		
	    return view;
	}
	
	 public void onClick(View view) {
	    	if (DEBUG) Log.i(TAG, "onClick()");
	    	
	    	if (view.getId() == R.id.buttonScan) {
	    		if (DEBUG) Log.i(TAG, "Scan clicked");
				mActivity.addScanListener(this);
	    		mDataManager.initiateScan();
				return;
	    	}
//	    	if (view.getId() == R.id.buttonClear) {
//	    		if (DEBUG) Log.i(TAG, "Clear clicked");
////	    		mDataManager.clearDatabase();
//				return;
//	    	}
    }

	@Override
	public void onScanResult(WifiScan scan) {
		if (DEBUG) Log.i(TAG, "onScanResult()");
		mWifiScans = mDataManager.getAllWifiScanObjects();
		Log.i(TAG, "datalist size: " + mWifiScans.size());
		mListAdapter.clear();
		mListAdapter.addAll(mWifiScans);
		mListAdapter.notifyDataSetChanged();
		
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
		WifiScan item = mListAdapter.getItem(position);
		if (DEBUG) Log.i(TAG, "onItemClick() " + item.toString());
		
//		Set<WifiConnectionData> points = mDataManager.getConnectionsForScan(item);
//		PointsListFragment frag = new PointsListFragment();
//		frag.setInfo(points);
		
		ScanDisplayFragment frag = new ScanDisplayFragment();
		frag.addScans(mDataManager.getAllScansConnectedToScan(item));

		mActivity.setContentFragment(frag, true);
	}

}
