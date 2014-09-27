package com.ninjarific.wirelessmapper.engine;

import java.util.ArrayList;
import java.util.Set;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.actors.WifiPointActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
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
	
	// TODO: revise this to follow the pattern for add single scan
	// this function will make more sense when points retain their last known position
	// and it won't be horrible to add all.
	public void addWifiScans(ArrayList<WifiScan> mScans) {
		if (DEBUG) Log.d(TAG, "addWifiScan() " + mScans);
		for (WifiScan scan : mScans) {
			if (mScanActors.get(scan.getId()) == null) {
//				Set<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
				WifiScanActor actor = new WifiScanActor(scan);
				mScanActors.put(scan.getId(), actor);
				mGraphicsView.createRendererForActor(actor);
			}
		}

		for (WifiScan scan : mScans) {
			Set<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
			createWifiPointActorsFromConnections(connections);
			mScanActors.get(scan.getId()).createForceConnections(this);
		}
	}

	private void createWifiPointActorsFromConnections(Set<WifiConnectionData> connections) {
		if (DEBUG) Log.d(TAG, "addWifiPointsFromConnections() count " + connections.size());
		for (WifiConnectionData connection : connections) {
			WifiPoint point = connection.getPoint();

			// create actors for points, if they don't already exist
			if (mPointActors.get(point.getId()) == null) {
				WifiPointActor actor = new WifiPointActor(point, this);
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

	public void addSingleScan(WifiScan scan) {
		if (mScanActors.get(scan.getId()) == null) {
			// create actor for scan if scan doesn't already exist
			WifiScanActor actor = new WifiScanActor(scan);
			mScanActors.put(scan.getId(), actor);
			mGraphicsView.createRendererForActor(actor);

			Set<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
			
			// create points
			createWifiPointActorsFromConnections(connections);
			
			// look to establish all connections from scan to points
			for (WifiConnectionData connection : connections) {
				WifiPointActor pointActor = mPointActors.get(connection.getPoint().getId());
				if (pointActor != null) {
					actor.addForceSource(pointActor, connection.getLevel(), true);
					pointActor.addForceSource(actor, connection.getLevel(), false);
				}
			}
		}
	}

}
