package com.ninjarific.wirelessmapper.engine;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.actors.WifiPointActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiPointActorDescriptor;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiScanActorDescriptor;
import com.ninjarific.wirelessmapper.ui.views.GraphicsView;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class MainEngineThread extends Thread {
	private static final String TAG = "MainEngineThread";
	private static final boolean DEBUG = true;
	private static final boolean DEBUG_FPS = false;
	
	static final long FPS = 30;
	private GraphicsView mGraphicsView;
	private boolean mIsRunning = false;
	private long mLastStartTime;
	
	private DataManager mDataManager;
	private LongSparseArray<WifiScanActor> mScanActors;
	private LongSparseArray<WifiPointActor> mPointActors;
	
	public MainEngineThread(GraphicsView view, DataManager datamanager) {
		mGraphicsView = view;
		mDataManager = datamanager;
		mScanActors = new LongSparseArray<WifiScanActor>();
		mPointActors = new LongSparseArray<WifiPointActor>();
	}
	
	public void setRunning(boolean running) {
		mIsRunning = running;
	}
	
	private long getSystemTimestamp() {
		return SystemClock.elapsedRealtime();
	}
	
	@Override
	public void run() {
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime;
        mLastStartTime = getSystemTimestamp();
		while (mIsRunning) {
			startTime = getSystemTimestamp();
			if (DEBUG_FPS) Log.v(TAG, "updateTime: " + (startTime - mLastStartTime));
			update(startTime - mLastStartTime);
			mLastStartTime = startTime;
			Canvas c = null;
			try {
				c = mGraphicsView.getHolder().lockCanvas();
				synchronized (mGraphicsView.getHolder()) {
					mGraphicsView.draw(c);
				}
			} finally {
				if (c != null) {
					mGraphicsView.getHolder().unlockCanvasAndPost(c);
				}
			}
			
			// sleep to maintain fps
			sleepTime = ticksPS - (getSystemTimestamp() - startTime);
			try {
				if (sleepTime > 0) {
					sleep(sleepTime);
				} else {
					// sleep a few ms anyway, to reduce cpu load when the system is struggling
					sleep(10);
				}
			} catch (Exception e) {}
		}
		if (DEBUG) Log.i(TAG, "exited engine thread loop");
	}

	/**
	 * Process the next game logic cycle.
	 * @param timeDelta - the elapsed ms since the previous update
	 * 
	 */
	private void update(long timeDelta) {
		for (int i = 0, size = mScanActors.size(); i < size; i++) {
			mScanActors.valueAt(i).update(timeDelta);
		}
		for (int i = 0, size = mPointActors.size(); i < size; i++) {
			mPointActors.valueAt(i).update(timeDelta);
		}
		
		mGraphicsView.onEngineTick(timeDelta);
		
	}
	
	public void addWifiScans(ArrayList<WifiScan> mScans) {
		if (DEBUG) Log.d(TAG, "addWifiScan() " + mScans);
		for (WifiScan scan : mScans) {
			if (mScanActors.get(scan.getId()) == null) {
				List<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
				WifiScanActorDescriptor desc = new WifiScanActorDescriptor(scan, connections);
				WifiScanActor actor = new WifiScanActor(desc);
				mScanActors.put(scan.getId(), actor);
				mGraphicsView.createRendererForActor(actor);
			}
		}

		for (WifiScan scan : mScans) {
			List<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
			addWifiPointsFromConnections(connections);
			mScanActors.get(scan.getId()).loadPointConnections(this);
		}
	}

	private void addWifiPointsFromConnections(List<WifiConnectionData> connections) {
		if (DEBUG) Log.d(TAG, "addWifiPointsFromConnections() count " + connections.size());
		for (WifiConnectionData connection : connections) {
			WifiPoint point = connection.getPoint();
			if (mPointActors.get(point.getId()) == null) {
				List<WifiConnectionData> scanConnections = mDataManager.getConnectionsForPoint(point);
				WifiPointActorDescriptor desc = new WifiPointActorDescriptor(point, scanConnections);
				WifiPointActor actor = new WifiPointActor(desc, this);
				mPointActors.put(point.getId(), actor);
				mGraphicsView.createRendererForActor(actor);
			}
		}
	}

	public WifiScanActor getScanActorById(long id) {
		if (DEBUG) Log.d(TAG, "getScanActorById() " + id);
		if (DEBUG) Log.d(TAG, "\t scanActors: " + mScanActors.toString());
		return mScanActors.get(id);
	}

	public WifiPointActor getPointActorById(long id) {
		if (DEBUG) Log.d(TAG, "getPointActorById() " + id);
		return mPointActors.get(id);
	}

}
