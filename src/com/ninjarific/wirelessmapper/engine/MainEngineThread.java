package com.ninjarific.wirelessmapper.engine;

import java.util.HashMap;
import java.util.List;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiScanActorDescriptor;
import com.ninjarific.wirelessmapper.ui.views.GraphicsView;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class MainEngineThread extends Thread {
	private static final String TAG = "MainEngineThread";
	private static final boolean DEBUG = true;
	
	static final long FPS = 30;
	private GraphicsView mGraphicsView;
	private boolean mIsRunning = false;
	private long mTick;
	
	private DataManager mDataManager;
	private HashMap<WifiScan, WifiScanActor> mScanActors;
	
	public MainEngineThread(GraphicsView view, DataManager datamanager) {
		mGraphicsView = view;
		mDataManager = datamanager;
		mScanActors = new HashMap<WifiScan, WifiScanActor>();
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
		while (mIsRunning) {
			startTime = getSystemTimestamp();
			update(startTime - mTick);
			mTick = startTime;
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
		// TODO Auto-generated method stub
		
	}
	
	public void addWifiScan(WifiScan scan) {
		if (!mScanActors.containsKey(scan)) {
			List<WifiConnectionData> connections = mDataManager.getConnectionsForScan(scan);
			LongSparseArray<Integer> processedConnections = DataManager.getProcessedConnectionsForConnectionData(connections);
			WifiScanActorDescriptor desc = new WifiScanActorDescriptor(scan, processedConnections);
			WifiScanActor actor = new WifiScanActor(desc);
			mScanActors.put(scan, actor);
			mGraphicsView.createRendererForActor(actor);
		}
		
	}

}
