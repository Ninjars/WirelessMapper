package com.ninjarific.wirelessmapper.engine.tasks;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;

public class AddScansTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "AddScansTask";

	private ArrayList<WifiScan> mScans;
	private MainEngineThread mEngine;
	public AddScansTask(MainEngineThread engine, ArrayList<WifiScan> scans) {
		mEngine = engine;
		mScans = scans;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		for (WifiScan scan : mScans) {
			Log.i(TAG, "adding scan");
			if (mEngine == null) {
				return null;
			}
			
			mEngine.addSingleScan(scan);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
