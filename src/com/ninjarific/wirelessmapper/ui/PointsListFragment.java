package com.ninjarific.wirelessmapper.ui;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.R;
import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;

public class PointsListFragment extends RootFragment {
	private static final String TAG = "PointsListFragment";
	private static final boolean DEBUG = Constants.DEBUG;
	
	private MainActivity mActivity;
	private ArrayAdapter<WifiConnectionData> mListAdapter;
	private ListView mListView;
	
	private ArrayList<WifiConnectionData> mWifiConnections;	
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		super.onCreateView(inflater, container, bundle);
	    View view = inflater.inflate(R.layout.fragment_points_list, container, false);
	    mListView = (ListView) view.findViewById(R.id.list);
		
	    if (mWifiConnections != null) {
	    	mListAdapter = new ArrayAdapter<WifiConnectionData>(mActivity, R.layout.row, mWifiConnections);
	    } else {
	    	Log.e(TAG, "Points not set!");
	    }
		
		mListView.setAdapter(this.mListAdapter);
		
	    return view;
	}

	public void setInfo(Set<WifiConnectionData> points) {
		mWifiConnections = new ArrayList<WifiConnectionData>(points);
		
	}
}
