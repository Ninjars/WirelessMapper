package com.ninjarific.wirelessmapper.entities.actors;

import android.graphics.PointF;

public class MobileActor extends RootActor {
//	private static final String TAG = "MobileActor";
//	private static final boolean DEBUG = true;
	
//	private static final double cInactiveVelocityCutoff = 0.01;
//	private static final long cInactiveMsCutoff = 1500; // time before an almost stationary object stops itself
//	private static final double cInactiveDistanceCutoff = 0.001;
	private static final double cFrictionConstant = 0.5;
	private static final double cRandomVelMax = 100;
	
	private PointF mPosition;
	private PointF mLastPosition;
	private PointF mVelocity;
	private PointF mAcceleration;

//	private boolean mIsActive;
//	private long mLastActive;
	
	public MobileActor(PointF position) {
		mPosition = position;
		mLastPosition = new PointF(mPosition.x, mPosition.y);
		mVelocity = getRandomVelocity();
		mAcceleration = getAcceleration();
//		mIsActive = true;
//		mLastActive = SystemClock.elapsedRealtime();
	}
	
	@Override
	public void update(long timeDeltaMs) {
		double deltaSeconds = timeDeltaMs / 1000.0;
		mAcceleration = getAcceleration();
		updateVelocity(deltaSeconds);
		updatePosition(deltaSeconds);
	}
	
	protected PointF getAcceleration() {
		if (mAcceleration == null) {
			mAcceleration = new PointF();
		}
		return mAcceleration;
	}
	
	private PointF getRandomVelocity() {
		float dx = (float) (-1 + Math.random() * 2);
		float dy = (float) (-1 + Math.random() * 2);
		
		// normalise
		double mag = Math.sqrt(dx * dx + dy * dy);
		
		
		float fx = (float) (dx / mag * cRandomVelMax);
		float fy = (float) (dy / mag * cRandomVelMax);
		
		return new PointF(fx, fy);
	}

	private void updateVelocity(double deltaSeconds) {
		mVelocity.x = (float) ((mVelocity.x + mAcceleration.x * deltaSeconds) * cFrictionConstant);
		mVelocity.y = (float) ((mVelocity.y + mAcceleration.y * deltaSeconds) * cFrictionConstant);
	}

	private void updatePosition(double deltaSeconds) {
		mLastPosition.set(mPosition);
		mPosition.x += mVelocity.x * deltaSeconds;
		mPosition.y += mVelocity.y * deltaSeconds;
	}
	
//	/**
//	 * Checks to see if the actor is moving significantly.
//	 * @return
//	 */
	// TODO: implement this and system to reactivate actors when appropriate
//	private boolean checkIfActive() {
//		if (Math.abs(mVelocity.x) > cInactiveVelocityCutoff 
//				|| Math.abs(mVelocity.y) > cInactiveVelocityCutoff 
//				|| MathUtils.getSquareDistanceBetweenPoints(mLastPosition, mPosition) > cInactiveDistanceCutoff) {
//			mLastActive = SystemClock.elapsedRealtime();
//			return true;
//		} else {
//			if (SystemClock.elapsedRealtime() - mLastActive > cInactiveMsCutoff) {
//				if (DEBUG) Log.d(TAG, "going inactive");
//				mVelocity.set(0,0);
//				return false;
//			}
//		}
//		return true;
//	}

}
