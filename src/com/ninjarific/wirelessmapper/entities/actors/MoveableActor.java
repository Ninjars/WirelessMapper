package com.ninjarific.wirelessmapper.entities.actors;

import android.graphics.PointF;
import android.os.SystemClock;

import com.ninjarific.wirelessmapper.entities.descriptors.MovableActorDescriptor;

public class MoveableActor extends RootActor {
	private static final double cInactiveVelocityCutoff = 0.001;
	private static final long cInactiveMsCutoff = 1500; // time before an almost stationary object stops itself

	private PointF mPosition;
	private PointF mVelocity;
	private boolean mIsActive;
	private long mLastActive;
	
	public MoveableActor(MovableActorDescriptor desc) {
		mPosition = desc.getPosition();
		mVelocity = desc.getVelocity();
		mIsActive = desc.isIsActive();
	}

	public PointF getPosition() {
		return mPosition;
	}

	public void setPosition(PointF position) {
		mIsActive = true;
		mPosition = position;
	}

	public PointF getVelocity() {
		return mVelocity;
	}
	
	public void setVelocity(PointF velocity) {
		mIsActive = true;
		mVelocity = velocity;
	}
	
	public void update(long deltaT) {
		if (mIsActive) {
			// check to see if actor is still active
			// if we are inactive, only something acting on the
			// actor should awaken it, eg by setting its position or velocity
			mIsActive = checkIfActive();
		}
		
		if (mIsActive) {
			mPosition.x += mVelocity.x * deltaT / 1000;
			mPosition.y += mVelocity.x * deltaT / 1000;
		}
	}
	
	/**
	 * Checks to see if the actor is moving significantly.
	 * @return
	 */
	private boolean checkIfActive() {
		if (Math.abs(mVelocity.x) > cInactiveVelocityCutoff || Math.abs(mVelocity.y) > cInactiveVelocityCutoff) {
			mLastActive = SystemClock.elapsedRealtime();
			return true;
		} else {
			if (SystemClock.elapsedRealtime() - mLastActive > cInactiveMsCutoff) {
				mVelocity.x = 0;
				mVelocity.y = 0;
				return false;
			}
		}
		return true;
	}

}
