package com.ninjarific.wirelessmapper.engine;

import java.util.ArrayList;
import java.util.Set;

import android.util.Log;
import android.util.LongSparseArray;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.actors.WifiPointActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.listeners.GraphicsViewListener;
import com.ninjarific.wirelessmapper.listeners.MainLoopUpdateListener;
import com.ninjarific.wirelessmapper.ui.views.GraphicsView;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class GameController implements GraphicsViewListener, MainLoopUpdateListener {
	private static final String TAG = "GameController";
	private static final boolean DEBUG = true;
	private DataManager mDataManager;
	private GraphicsView mGraphicsView;
	private MainEngineThread mEngine;
	private LongSparseArray<WifiPointActor> mPointActors;
	private LongSparseArray<WifiScanActor> mScanActors;

	public GameController(GraphicsView graphicsView, DataManager dataManager) {
		mGraphicsView = graphicsView;
		mDataManager = dataManager;
		mEngine = new MainEngineThread(graphicsView);
		mEngine.addUpdateLisener(mGraphicsView);
		mEngine.addUpdateLisener(this);
		mScanActors = new LongSparseArray<WifiScanActor>();
		mPointActors = new LongSparseArray<WifiPointActor>();
	}
    
    public void start() {
		if (DEBUG) Log.d(TAG, "start()");
		mEngine.setRunning(true);
		mEngine.start();
    }

	public void stop() {
		if (DEBUG) Log.d(TAG, "stop()");
    	mEngine.setRunning(false);
	}

	@Override
	public void onSurfaceCreated() {
		if (DEBUG) Log.d(TAG, "onSurfaceCreated()");
		mEngine.setSurfaceState(true);
	}

	@Override
	public void onSurfaceDestroyed() {
		if (DEBUG) Log.d(TAG, "onSurfaceDestroyed()");
		mEngine.setSurfaceState(false);
		mEngine.setRunning(false);
	}

	@Override
	public void attemptThreadReconnect() throws InterruptedException {
		mEngine.join();
	}
	
	/**
	 * Process the next game logic cycle.
	 * @param timeDelta - the elapsed ms since the previous update
	 * 
	 */
	@Override
	public void onUpdate(long timeDelta) {
		for (int i = 0, size = mScanActors.size(); i < size; i++) {
			mScanActors.valueAt(i).update(timeDelta);
		}
		for (int i = 0, size = mPointActors.size(); i < size; i++) {
			mPointActors.valueAt(i).update(timeDelta);
		}
	}
	
//	public void addWifiScans(ArrayList<WifiScan> mScans) {
//		if (mEngine != null) {
//			AddScansTask task = new AddScansTask(mEngine, mScans);
//			task.execute();
//		}
//	}
	
	// TODO: revise this to follow the pattern for add single scan
	// this function will make more sense when points retain their last known position
	// and it won't be horrible to add all.
	public void addWifiScans(ArrayList<WifiScan> scans) {
		if (DEBUG) Log.d(TAG, "addWifiScan() " + scans);
		for (WifiScan scan : scans) {
			if (mScanActors.get(scan.getId()) == null) {
//					Set<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
				WifiScanActor actor = new WifiScanActor(scan);
				mScanActors.put(scan.getId(), actor);
				mGraphicsView.createRendererForActor(actor);
			}
		}

		for (WifiScan scan : scans) {
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
				WifiPointActor actor = new WifiPointActor(point);
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
