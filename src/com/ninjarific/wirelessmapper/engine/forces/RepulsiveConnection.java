package com.ninjarific.wirelessmapper.engine.forces;

import android.graphics.PointF;

import com.ninjarific.wirelessmapper.entities.actors.ForceActor;
import com.ninjarific.wirelessmapper.utilties.MathUtils;

public class RepulsiveConnection extends ForceConnection {
	private static final double cMaxDistanceForForce = 300 * 300;
	private static final double cForceMagnitude = 10;

	public RepulsiveConnection(ForceActor actor1, ForceActor actor2) {
		super(actor1, actor2);
	}

	public PointF calculateForce(PointF outPoint) {
		PointF pos1 = mActor1.getPosition();
		PointF pos2 = mActor2.getPosition();
		double distanceSquared = MathUtils.getSquareDistanceBetweenPoints(pos1, pos2);
		
		if (distanceSquared > cMaxDistanceForForce) {
			// clip force at max distance
			outPoint.set(0,0);
			return outPoint;
		}
		
		double dx = pos1.x - pos2.x;
		double dy = pos1.y - pos2.y;
		
		// prevent inaction in case that actors are in the same spot
		if (dx == 0 && dy == 0) {
			dx = -1 + Math.random() * 2;
			dy = -1 + Math.random() * 2;
		}
		
		// normalise
		double mag = Math.sqrt(dx * dx + dy * dy);
		
		float fx = (float) (dx / mag * cForceMagnitude / distanceSquared);
		float fy = (float) (dy / mag * cForceMagnitude / distanceSquared);
		
		if (fx < cMinimumForceCutoff) fx = 0;
		if (fy < cMinimumForceCutoff) fy = 0;
		
		outPoint.set(fx, fy);
		return outPoint;
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof RepulsiveConnection
				&& (mActor1.equals(((ForceConnection) o).getActor1()) || mActor1.equals(((ForceConnection) o).getActor2()))
				&& (mActor2.equals(((ForceConnection) o).getActor1()) || mActor2.equals(((ForceConnection) o).getActor2())));
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash += this.getClass().hashCode();
		hash += mActor1.hashCode();
		hash += mActor2.hashCode();
		return hash;
	}
}
