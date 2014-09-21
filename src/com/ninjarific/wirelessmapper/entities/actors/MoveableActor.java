package com.ninjarific.wirelessmapper.entities.actors;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;

import com.ninjarific.wirelessmapper.entities.descriptors.MovableActorDescriptor;

public class MoveableActor extends RootActor {
	private static final String TAG = "MoveableActor";
	private static final boolean DEBUG = true;
	
	private static final double cInactiveVelocityCutoff = 0.01;
	private static final long cInactiveMsCutoff = 1500; // time before an almost stationary object stops itself
	private static final double cInactiveDistanceCutoff = 0.001;
	
	private final double cOrbitalVelocity = 0.3 + (0.3 * Math.random());

	private static final float cMaxSpeedSquared = 10 * 10; // max velocity per second is constant, but square roots are expensive
	
	private float mRateOfAcceleration = 5; // units per second
	private PointF mPosition;
	private PointF mTargetPosition;
	private PointF mLastPosition;
	private PointF mVelocity;
	private boolean mIsActive;
	private long mLastActive;
	protected Mode mMode = Mode.STANDARD;
	protected long mOrbitStartTime;
	private int mOrbitRadius;
	
	enum Mode {
		ORBIT,
		STANDARD,
	}

	public MoveableActor(MovableActorDescriptor desc) {
		mPosition = desc.getPosition();
		mLastPosition = new PointF(mPosition.x, mPosition.y);
		mIsActive = desc.isIsActive();
		mVelocity = new PointF();
		mLastActive = SystemClock.elapsedRealtime();
		if (DEBUG) Log.d(TAG, "isActive on startup " + mIsActive);
	}

	public PointF getPosition() {
		return mPosition;
	}

	public void setPosition(PointF position) {
		mIsActive = true;
		updatePosition(position);
	}
	
	private void updatePosition(PointF newPos) {
		mLastPosition.x = mPosition.x;
		mLastPosition.y = mPosition.y;
		mPosition = newPos;
	}

	public PointF getTargetPosition() {
		return mTargetPosition;
	}
	
	public void setTargetPosition(PointF position) {
		mIsActive = true;
		mTargetPosition = position;
		// TODO: to keep the acceleration problem simple, reset velocity on change of target.
		mVelocity = new PointF();
	}

	public PointF getVelocity() {
		return mVelocity;
	}
	
	public void setVelocity(PointF velocity) {
		mIsActive = true;
		mVelocity = velocity;
	}
	
	protected void setMode(Mode mode) {
		mMode = mode;
		if (mode == Mode.ORBIT) {
			mOrbitStartTime = (long) (SystemClock.elapsedRealtime() - (Math.random() * 20000));
		}
	}
	
	protected void setOrbitRadius(int radius) {
		mOrbitRadius = (radius - 25) * 6;
	}
	
	@Override
	public void update(long deltaT) {
		if (mIsActive) {
			// check to see if actor is still active
			// if we are inactive, only something acting on the
			// actor should awaken it, eg by setting its position or velocity
			mIsActive = checkIfActive();
		}
		
		if (mIsActive) {
			updateVelocity(deltaT);
			updatePosition(deltaT);
		}
	}
	
	private void updateVelocity(long deltaT) {
		switch (mMode) {
			case STANDARD: {
				if (mTargetPosition != null) {
					// TODO: currently this algo doesn't take into account the direction of current 
					// acceleration when calculating the point to accelerate towards
					double remainingDistance = getSquareDistanceBetweenPoints(mTargetPosition, mPosition);
					double decelerationDistance = getCurrentDecelerationDistanceSquared();
					
					if (remainingDistance > decelerationDistance) {
						accelerateToPoint(deltaT, mRateOfAcceleration, mTargetPosition);
					} else {
						accelerateToPoint(deltaT, -mRateOfAcceleration, mTargetPosition);
					}
				}
				break;
			}
			case ORBIT: {
				break;
			}
		}
	}
	
	private void accelerateToPoint(long deltaT, double acceleration, PointF point) {
		PointF targetVector = new PointF(point.x - mPosition.x, point.y - mPosition.y);
		PointF currentVector = mVelocity;
		float dVx = targetVector.x - currentVector.x;
		float dVy = targetVector.y - currentVector.y;
	}
	
	private double getCurrentDecelerationDistanceSquared() {
		return getSpeedSquared() / ((2*mRateOfAcceleration) * (2*mRateOfAcceleration));
	}

	private void updatePosition(long deltaT) {
		mLastPosition.x = mPosition.x;
		mLastPosition.y = mPosition.y;

		switch (mMode) {
			case STANDARD: {
				mPosition.x += mVelocity.x * deltaT / 1000;
				mPosition.y += mVelocity.y * deltaT / 1000;
				break;
			}
			case ORBIT: {
				double angle = ((SystemClock.elapsedRealtime() - mOrbitStartTime) / 1000f * cOrbitalVelocity) % (2f * Math.PI);
				setPosition(new PointF((float) (mOrbitRadius * Math.sin(angle)), (float) (mOrbitRadius * Math.cos(angle))));
				break;
			}
		}
	}
	
	private static double getSquareDistanceBetweenPoints(PointF a, PointF b) {
		return (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y);
	}
	
	private double getSpeedSquared() {
		return (mVelocity.x * mVelocity.x) + (mVelocity.y * mVelocity.y);
	}
	
	/**
	 * Checks to see if the actor is moving significantly.
	 * @return
	 */
	private boolean checkIfActive() {
		if (Math.abs(mVelocity.x) > cInactiveVelocityCutoff 
				|| Math.abs(mVelocity.y) > cInactiveVelocityCutoff 
				|| getSquareDistanceBetweenPoints(mLastPosition, mPosition) > cInactiveDistanceCutoff) {
			mLastActive = SystemClock.elapsedRealtime();
			return true;
		} else {
			if (SystemClock.elapsedRealtime() - mLastActive > cInactiveMsCutoff) {
				if (DEBUG) Log.d(TAG, "going inactive");
				mVelocity.x = 0;
				mVelocity.y = 0;
				return false;
			}
		}
		return true;
	}

}
