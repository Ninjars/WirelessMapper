package com.ninjarific.wirelessmapper.entities.descriptors;

import android.graphics.PointF;

public class MovableActorDescriptor extends RootActorDescriptor {

	private PointF mPosition;
	private PointF mVelocity;
	private boolean mIsActive; // used to deactivate non-moving actors until they are acted upon

	public MovableActorDescriptor() {
		super();
		mPosition = new PointF(0,0);
		mVelocity = new PointF(0,0);
		mIsActive = true;
	}
	
	public MovableActorDescriptor(PointF position, PointF velocity, boolean isActive) {
		super();
		mPosition = position;
		mVelocity = velocity;
		mIsActive = isActive;
	}

	public PointF getPosition() {
		return mPosition;
	}

	public void setPosition(PointF position) {
		mPosition = position;
	}

	public PointF getVelocity() {
		return mVelocity;
	}

	public void setVelocity(PointF velocity) {
		mVelocity = velocity;
	}

	public boolean isIsActive() {
		return mIsActive;
	}

	public void setIsActive(boolean isActive) {
		mIsActive = isActive;
	}
}
