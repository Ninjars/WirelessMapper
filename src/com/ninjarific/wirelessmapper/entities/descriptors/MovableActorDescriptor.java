package com.ninjarific.wirelessmapper.entities.descriptors;

import android.graphics.PointF;

public class MovableActorDescriptor extends RootActorDescriptor {

	private PointF mPosition;
	private boolean mIsActive; // used to deactivate non-moving actors until they are acted upon

	public MovableActorDescriptor() {
		super();
		mPosition = new PointF(0,0);
		mIsActive = true;
	}
	
	public MovableActorDescriptor(PointF position, boolean isActive) {
		super();
		mPosition = position;
		mIsActive = isActive;
	}

	public PointF getPosition() {
		return mPosition;
	}

	public void setPosition(PointF position) {
		mPosition = position;
	}

	public boolean isIsActive() {
		return mIsActive;
	}

	public void setIsActive(boolean isActive) {
		mIsActive = isActive;
	}
}
