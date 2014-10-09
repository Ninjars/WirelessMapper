package com.ninjarific.wirelessmapper.engine.forces;

import com.ninjarific.wirelessmapper.entities.actors.ForceActor;
import com.ninjarific.wirelessmapper.utilties.MathUtils;

import android.graphics.PointF;

public class AttractToDistanceConnection extends ForceConnection {
	private static final double cMaxDistanceForForce = 150 * 150;
	private static final double cExternalForceMagnitude = 1;
	private static final double cInternalForceMagnitude = 0.5;
	private double mOptimumDistanceSquared;

	public AttractToDistanceConnection(ForceActor actor1, ForceActor actor2, double targetDistance) {
		super(actor1, actor2);
		mOptimumDistanceSquared = targetDistance * targetDistance;
	}
	
	public double getOptimumDistanceSquared() {
		return mOptimumDistanceSquared;
	}
	
	@Override
	public PointF calculateForce(PointF outPoint) {
		PointF pos1 = mActor1.getPosition();
		PointF pos2 = mActor2.getPosition();
		double distanceSquared = MathUtils.getSquareDistanceBetweenPoints(pos1, pos2);
		double deltaDistanceSquared = distanceSquared - mOptimumDistanceSquared;
		
		if (deltaDistanceSquared == 0) {
			// yay, we're in the right spot!
			outPoint.set(0,0);
			return outPoint;
		}
		
		// get normalised vector
		double dx = pos1.x - pos2.x;
		double dy = pos1.y - pos2.y;
		double mag = Math.sqrt(dx * dx + dy * dy);
		float fx = 0;
		float fy = 0;
		
		if (deltaDistanceSquared < 0) {
			// actors are closer than desired
			// using a different force magnitude to allow movement when within range
			fx = (float) (dx / mag * deltaDistanceSquared * cInternalForceMagnitude);
			fy = (float) (dy / mag * deltaDistanceSquared * cInternalForceMagnitude);
			
		} else {
			// actors are further than desired; cap distance for force calculation
			// to avoid things getting out of hand
			deltaDistanceSquared = Math.min(cMaxDistanceForForce, deltaDistanceSquared);
			fx = (float) (dx / mag * deltaDistanceSquared * cExternalForceMagnitude);
			fy = (float) (dy / mag * deltaDistanceSquared * cExternalForceMagnitude);
		}
		outPoint.set(fx, fy);
		return outPoint;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof AttractToDistanceConnection
				&& mOptimumDistanceSquared == ((AttractToDistanceConnection) o).getOptimumDistanceSquared()
				&& (mActor1.equals(((ForceConnection) o).getActor1()) || mActor1.equals(((ForceConnection) o).getActor2()))
				&& (mActor2.equals(((ForceConnection) o).getActor1()) || mActor2.equals(((ForceConnection) o).getActor2())));
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash += this.getClass().hashCode();
		hash += mActor1.hashCode();
		hash += mActor2.hashCode();
		hash += mOptimumDistanceSquared;
		return hash;
	}
}
