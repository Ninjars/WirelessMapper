package com.ninjarific.wirelessmapper.engine;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;

import com.ninjarific.wirelessmapper.listeners.MainLoopUpdateListener;
import com.ninjarific.wirelessmapper.ui.views.GraphicsView;

public class MainEngineThread extends Thread {
	private static final String TAG = "MainEngineThread";
	private static final boolean DEBUG = true;
	private static final boolean DEBUG_FPS = false;
	
	static final long FPS = 30;
	
	private GraphicsView mGraphicsView;
	
	private boolean mIsRunning = false;
	private long mLastStartTime;
	
	private boolean mSurfaceIsReady = false;
	
	private ArrayList<MainLoopUpdateListener> mUpdateListeners;
	
	public MainEngineThread(GraphicsView view) {
		mGraphicsView = view;
		mUpdateListeners = new ArrayList<MainLoopUpdateListener>();
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
			if (mSurfaceIsReady ) {
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
		for (MainLoopUpdateListener l : mUpdateListeners) {
			l.onUpdate(timeDelta);
		}
	}
	
	public void addUpdateLisener(MainLoopUpdateListener l) {
		mUpdateListeners.add(l);
	}

	public void removeUpdateLisener(MainLoopUpdateListener l) {
		mUpdateListeners.remove(l);
	}

	public boolean isRunning() {
		return mIsRunning;
	}

	public void setSurfaceState(boolean ready) {
		mSurfaceIsReady = ready;
	}

}
