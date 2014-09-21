package com.ninjarific.wirelessmapper.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;
import com.ninjarific.wirelessmapper.entities.actors.RootActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.graphics.renderers.GroupNode;
import com.ninjarific.wirelessmapper.graphics.renderers.WifiScanGroupNode;

public class GraphicsView extends SurfaceView {
	private static final String TAG = "GraphicsView";
	private static final boolean DEBUG = true;
	
    private SurfaceHolder mSurfaceHolder;
    private MainEngineThread mEngine;
	private boolean mSurfaceReady;
	private boolean mEngineStartCalled;
	private MainActivity mMainActivity;

	private GroupNode mRenderTree;
	private WifiScan mWifiScanToAdd;
	private PointF mCenterTranslation;

	public GraphicsView(Context context) {
        super(context);
        initialisation();
    }
	
    public GraphicsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        initialisation();
	}

	public GraphicsView(Context context, AttributeSet attrs) {
		super(context, attrs);
        initialisation();
	}
	
	private void initialisation() {
		if (DEBUG) Log.d(TAG, "initialisation()");
		mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (DEBUG) Log.d(TAG, "surfaceDestroyed()");
				boolean retry = true;
				if (mEngine != null) {
					mEngine.setRunning(false);
				}
				while (retry) {
					try {
						mEngine.join();
						retry = false;
					} catch (InterruptedException e) {
					}
				}
			}
		
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				onSurfaceCreated();
			}
		
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				onSurfaceChanged(width, height);
			}
			
		});
	}
    
	
	
    protected void onSurfaceCreated() {
		if (DEBUG) Log.d(TAG, "onSurfaceCreated()");
	 	// this is when view dimensions can be called on and things can get rolling
    	mSurfaceReady = true;
    	if (mEngineStartCalled) {
 			mEngine.setRunning(true);
    		mEngine.start();
    	}
    }
    
    protected void onSurfaceChanged(int width, int height) {
		if (DEBUG) Log.d(TAG, "onSurfaceChanged()");
		if (mRenderTree != null) {
			if (DEBUG) Log.d(TAG, "\t updating render tree base translation");
	        PointF currentTranslation = mRenderTree.getTranslation();
	        // remove previous center offset
	        if (mCenterTranslation != null) {
		        currentTranslation.x -= mCenterTranslation.x;
		        currentTranslation.y -= mCenterTranslation.y;
	        }

			if (DEBUG) Log.d(TAG, "\t base translation: " + currentTranslation);
	        // apply new center offset
	        mCenterTranslation = new PointF(width / 2f, height / 2f);
	        currentTranslation.x += mCenterTranslation.x;
	        currentTranslation.y += mCenterTranslation.y;
	        mRenderTree.setTranslation(currentTranslation);
			
		} else {
			if (DEBUG) Log.d(TAG, "\t no render tree; just storing the translation");
			mCenterTranslation = new PointF(width / 2f, height / 2f);
		}
    }
    
    public void startEngine(MainActivity activity) {
		if (DEBUG) Log.d(TAG, "startEngine()");
    	mMainActivity = activity;
        mEngine = new MainEngineThread(this, activity.getDataManager());
        mRenderTree = new GroupNode();
        if (mCenterTranslation != null) {
        	mRenderTree.setTranslation(mCenterTranslation);
        }
        
        if (mWifiScanToAdd != null) {
        	mEngine.addWifiScan(mWifiScanToAdd);
        }

	 	// we have a pointer to the activity for call backs and managers,
        // and the engine is ready to run as soon as the surface is created.
    	mEngineStartCalled = true;
    	if (mSurfaceReady) {
 			mEngine.setRunning(true);
    		mEngine.start();
    	}
    }
    
    public void onDestroy() {
    	mEngine.setRunning(false);
    }
    
    public void draw(Canvas canvas) {
    	if (canvas == null) {
    		return;
    	}
    	canvas.drawColor(Color.BLACK);
    	mRenderTree.draw(canvas);
    }

	public void createRendererForActor(RootActor actor) {
		if (DEBUG) Log.d(TAG, "createRendererForActor() " + actor);
		if (actor == null) {
			return;
		}
		
		if (actor instanceof WifiScanActor) {
			GroupNode actorNode = new WifiScanGroupNode((WifiScanActor) actor);
			mRenderTree.addChild(actorNode);
		}

	}
	
	public void addWifiScan(WifiScan scan) {
		if (mEngine != null) {
			mEngine.addWifiScan(scan);
		} else {
			mWifiScanToAdd = scan;
		}
	}
}
