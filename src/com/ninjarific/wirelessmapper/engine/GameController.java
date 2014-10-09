package com.ninjarific.wirelessmapper.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.data.DataObject;
import com.ninjarific.wirelessmapper.engine.data.PointDataObject;
import com.ninjarific.wirelessmapper.engine.data.ScanDataObject;
import com.ninjarific.wirelessmapper.engine.forces.RepulsiveConnection;
import com.ninjarific.wirelessmapper.engine.interfaces.ScanDataTaskInterface;
import com.ninjarific.wirelessmapper.engine.tasks.AddScansTask;
import com.ninjarific.wirelessmapper.entities.actors.RootActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.listeners.GraphicsViewListener;
import com.ninjarific.wirelessmapper.listeners.MainLoopUpdateListener;
import com.ninjarific.wirelessmapper.ui.views.GraphicsView;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class GameController implements GraphicsViewListener, MainLoopUpdateListener, ScanDataTaskInterface {
	private static final String TAG = "GameController";
	private static final boolean DEBUG = true;
	private static final boolean DEBUG_OBJECTS = false;
	private DataManager mDataManager;
	private GraphicsView mGraphicsView;
	private MainEngineThread mEngine;
	private List<PointDataObject> mPointData;
	private List<ScanDataObject> mScanData;
	private ArrayList<AddScansTask> mAddScanTasks;

	public GameController(GraphicsView graphicsView, DataManager dataManager) {
		mGraphicsView = graphicsView;
		mDataManager = dataManager;
		mEngine = new MainEngineThread(graphicsView);
		mEngine.addUpdateLisener(mGraphicsView);
		mEngine.addUpdateLisener(this);
		mScanData = Collections.synchronizedList(new ArrayList<ScanDataObject>());
		mPointData =  Collections.synchronizedList(new ArrayList<PointDataObject>());
		mAddScanTasks = new ArrayList<AddScansTask>();
	}
    
    public void start() {
		if (DEBUG) Log.d(TAG, "start()");
		mEngine.setRunning(true);
		try {
			mEngine.start();
		} catch (IllegalThreadStateException e) {
			if (DEBUG) Log.d(TAG, "\t engine already running; restart loop");
			mEngine.run();
		}
    }

	public void stop() {
		if (DEBUG) Log.d(TAG, "stop()");
    	mEngine.setRunning(false);
    	for (AddScansTask task : mAddScanTasks) {
    		task.cancel(true);
    	}
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
	
	/**
	 * Process the next game logic cycle.
	 * @param timeDelta - the elapsed ms since the previous update
	 * 
	 */
	@Override
	public void onUpdate(long timeDelta) {
		synchronized (mScanData) {
			for (DataObject data : mScanData) {
				data.getActor().update(timeDelta);
			}
		}
		synchronized (mPointData) {
			for (DataObject data : mPointData) {
				data.getActor().update(timeDelta);
			}
		}
	}
	
	public void addConnectedScansRecursively(ArrayList<WifiScan> startScans) {
		AddScansTask task = new AddScansTask(this, startScans, mDataManager);
		task.execute();
		mAddScanTasks.add(task);
	}
	
	/*******
	 * 
	 *  ScanDataTaskInterface methods
	 *  
	 ******/
	
	@Override
	public void onAddScansTaskComplete(AddScansTask task) {
		if (DEBUG) Log.d(TAG, "onAddScansTaskComplete()");
		mAddScanTasks.remove(task);
	}

	@Override
	public PointDataObject getPointDataObject(WifiPoint point) {
		synchronized (mPointData) {
			for (PointDataObject data : mPointData) {
				if (data.getPoint().equals(point)) {
					return data;
				}
			}
		}
		return null;
	}

	@Override
	public ScanDataObject getScanDataObject(WifiScan scan) {
		synchronized (mScanData) {
			for (ScanDataObject data : mScanData) {
				if (data.getScan().equals(scan)) {
					return data;
				}
			}
		}
		return null;
	}

	@Override
	public void addPointDataObject(PointDataObject pointData) {
		if (DEBUG_OBJECTS) Log.d(TAG, "addPointDataObject()");
		if (!mPointData.contains(pointData)) {
			if (DEBUG_OBJECTS) Log.d(TAG, "\t adding new point");
			mPointData.add(pointData);
			createRendererForActor(pointData.getActor());
		}
	}

	@Override
	public void addScanDataObject(ScanDataObject scanData) {
		if (DEBUG_OBJECTS) Log.d(TAG, "addScanDataObject()");
		if (!mScanData.contains(scanData)) {
			ArrayList<ScanDataObject> nonAdjacentData = new ArrayList<ScanDataObject>(mScanData);
			mScanData.add(scanData);
			if (DEBUG_OBJECTS) Log.d(TAG, "\t adding new scan");
			// check for unconnected scans and add forces between
			for (WifiScan scan : scanData.getConnectedScans()) {
				for (ScanDataObject data : mScanData) {
					if (scan.equals(data.getScan())) {
						nonAdjacentData.remove(data);
					}
				}
			}
			
			for (ScanDataObject obj : nonAdjacentData) {
				WifiScanActor actor1 = scanData.getActor();
				WifiScanActor actor2 = obj.getActor();
				RepulsiveConnection connection = new RepulsiveConnection(actor1, actor2);
				actor1.addForceConnection(connection);
				actor2.addForceConnection(connection);
			}

			createRendererForActor(scanData.getActor());
		}
	}
	
	private void createRendererForActor(RootActor actor) {
		mGraphicsView.createRendererForActor(actor);
	}
	
}
