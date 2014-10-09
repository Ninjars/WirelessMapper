package com.ninjarific.wirelessmapper.engine.forces;

import android.graphics.PointF;

import com.ninjarific.wirelessmapper.entities.actors.ForceActor;

public abstract class ForceConnection {
	protected static final double cMinimumForceCutoff = 0.001;
	protected ForceActor mActor1;
	protected ForceActor mActor2;
	private PointF mForce;
	private PointF mInverseForce;
	private boolean mUpdateAlternator = true;

	public ForceConnection(ForceActor actor1, ForceActor actor2) {
		mActor1 = actor1;
		mActor2 = actor2;
		mForce = new PointF();
		mInverseForce = new PointF();
	}
	
	public PointF getForce(ForceActor actor) {
		updateForce();
		if (actor.equals(mActor2)) {
			return mForce;
		} else {
			mInverseForce.set(-mForce.x, -mForce.y);
			return mInverseForce;
		}
	}
	
	public ForceActor getActor1() {
		return mActor1;
	}
	
	public ForceActor getActor2() {
		return mActor2;
	}
	
	/*
	 * force connections are between two actors, both of whom are going
	 * to call for the force in an update loop.  To half the calcs needed,
	 * only recalculate forces every other call.
	 */
	private void updateForce() {
		if (mUpdateAlternator) {
			mForce = calculateForce(mForce);
		}
		mUpdateAlternator = !mUpdateAlternator;
	}
	
	public abstract PointF calculateForce(PointF outPoint);
	
}
