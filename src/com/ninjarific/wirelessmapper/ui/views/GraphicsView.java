package com.ninjarific.wirelessmapper.ui.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;

import com.ninjarific.wirelessmapper.MainActivity;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;
import com.ninjarific.wirelessmapper.entities.actors.RootActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiPointActor;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.graphics.renderers.GroupNode;
import com.ninjarific.wirelessmapper.graphics.renderers.WifiPointGroupNode;
import com.ninjarific.wirelessmapper.graphics.renderers.WifiScanGroupNode;
import com.ninjarific.wirelessmapper.utilties.MathUtils;

public class GraphicsView extends SurfaceView implements OnTouchListener {
	private static final String TAG = "GraphicsView";
	private static final boolean DEBUG = true;
	private static final float cViewFlingFrictionFactor = 4f;
	private static final float cTapDetectionRadiusCutoff = 5*5;
	private static final float cFlingVelocityCutoff = 100;
	
    private SurfaceHolder mSurfaceHolder;
    private MainEngineThread mEngine;
	private boolean mSurfaceReady;
	private boolean mEngineStartCalled;
	private MainActivity mMainActivity;

	private GroupNode mRenderTree;
	private ArrayList<WifiScan> mWifiScanToAdd;
	private PointF mCenterTranslation;
	private PointF mViewVelocity;
	private VelocityTracker mVelocityTracker = null;
	private Float mLastTouchX;
	private Float mLastTouchY;
	private Float mDownTouchX;
	private Float mDownTouchY;

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
        	mEngine.addWifiScans(mWifiScanToAdd);
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
		if (actor == null) {
			return;
		}
		if (DEBUG) Log.d(TAG, "createRendererForActor() " + actor.getClass().getSimpleName());
		
		if (actor instanceof WifiScanActor) {
			GroupNode actorNode = new WifiScanGroupNode((WifiScanActor) actor);
			mRenderTree.addChild(actorNode);
		}
		
		if (actor instanceof WifiPointActor) {
			GroupNode actorNode = new WifiPointGroupNode((WifiPointActor) actor);
			mRenderTree.addChild(actorNode);
		}

	}
	
	public void addWifiScans(ArrayList<WifiScan> mScans) {
		if (mEngine != null) {
			mEngine.addWifiScans(mScans);
		} else {
			mWifiScanToAdd = mScans;
		}
	}

	public void setVelocity(PointF velocity) {
		mViewVelocity = velocity;
		
	}

	private void translateView(float x, float y) {
		PointF pos = mRenderTree.getTranslation();
		pos.x += x;
		pos.y += y;
		mRenderTree.setTranslation(pos);
		
	}

	public void onEngineTick(long timeDelta) {
		if (mViewVelocity != null) {
			updateViewTranslation(timeDelta);
		}
		
	}

	private void updateViewTranslation(long timeDelta) {
		PointF pos = mRenderTree.getTranslation();
		float seconds = (timeDelta / 1000f);
		mViewVelocity.x -= mViewVelocity.x * cViewFlingFrictionFactor * seconds;
		mViewVelocity.y -= mViewVelocity.y * cViewFlingFrictionFactor * seconds;
		pos.x += mViewVelocity.x * seconds;
		pos.y += mViewVelocity.y * seconds;
		mRenderTree.setTranslation(pos);
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: 
				mLastTouchX = event.getX();
				mLastTouchY = event.getY();
				mDownTouchX = event.getX();
				mDownTouchY = event.getY();
				mViewVelocity = null;
				
				if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                
				break;
			
			case MotionEvent.ACTION_MOVE: 
				mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call 
                // computeCurrentVelocity(). Then call getXVelocity() 
                // and getYVelocity() to retrieve the velocity for each pointer ID. 
                mVelocityTracker.computeCurrentVelocity(500);
                Log.d("", "X velocity: " + 
                        VelocityTrackerCompat.getXVelocity(mVelocityTracker, 
                        pointerId));
                Log.d("", "Y velocity: " + 
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                        pointerId));

                translateView(event.getX() - mLastTouchX, event.getY() - mLastTouchY);
				mLastTouchX = event.getX();
				mLastTouchY = event.getY();
				
                break;
			
			case MotionEvent.ACTION_UP: 
                float dx = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
                float dy = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
                
				if (MathUtils.getSquareDistanceBetweenPoints(mDownTouchX, mDownTouchY, event.getX(), event.getY()) < cTapDetectionRadiusCutoff) {
					if (DEBUG) Log.i(TAG, "tap detected");
				} else if (Math.abs(dx) > cFlingVelocityCutoff || Math.abs(dy) > cFlingVelocityCutoff) {
					if (DEBUG) Log.i(TAG, "fling detected");
                	setVelocity(new PointF(dx, dy));
                }
				
			case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
				try {
					mVelocityTracker.recycle();
				} catch (IllegalStateException e) {
					// TODO: work out why this is triggered quite so often!
					Log.e(TAG, e.toString());
				}
                break;
		}
		return true;
		
	}

}
