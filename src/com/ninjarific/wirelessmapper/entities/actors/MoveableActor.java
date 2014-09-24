package com.ninjarific.wirelessmapper.entities.actors;

import java.util.ArrayList;

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
	private static final double cMaxDistanceForForce = 200 * 200;
	private static final double cFrictionConstant = 0.9;
	
	private final double cOrbitalVelocity = 0.3 + (0.3 * Math.random());
	
	private PointF mLastPosition;
	private PointF mVelocity;
	private PointF mAcceleration;
	private boolean mIsActive;
	private long mLastActive;
	protected Mode mMode = Mode.STANDARD;
	protected long mOrbitStartTime;
	private int mOrbitRadius;
	private ArrayList<ForceSource> mForceSources;
	
	enum Mode {
		ORBIT,
		STANDARD,
	}
	
	/*
	 * what is needed:
	 * List of connections broken down to allow quick access to actor's position and desired distance from said position.
	 * The further from the optimum position, the stronger the force that pushes back, 
	 * with a max deviation of maybe double the desired distance.
	 * - comparison of current distance to desired distance
	 * - direction of force for restoration
	 * - sum with all other forces for all connections for point
	 * - apply resulting force as acceleration to current velocity
	 * - apply friction constant to velocity
	 * 
	 * Perhaps, as the connection goes both ways, the forces per connection should be calculated at a higher level
	 * and passed down to the point and scan actors to be resolved by the actors into their individual accelerations
	 */

	public MoveableActor(MovableActorDescriptor desc) {
		mPosition = desc.getPosition();
		mLastPosition = new PointF(mPosition.x, mPosition.y);
		mIsActive = desc.isIsActive();
		mVelocity = new PointF();
		mAcceleration = new PointF();
		mLastActive = SystemClock.elapsedRealtime();
		mForceSources = new ArrayList<ForceSource>();
		if (DEBUG) Log.d(TAG, "isActive on startup " + mIsActive);
	}
	
	private class ForceSource {
		private RootActor mActor;
		private double mTargetDistanceSquared;
		private PointF mCachedAccel;

		public ForceSource(RootActor actor, double targetDistance) {
			mActor = actor;
			mTargetDistanceSquared = 10 * targetDistance * targetDistance;
			mCachedAccel = new PointF();
		}
		
		public PointF getAcceleration(PointF position, double mass) {
			PointF actorPos = mActor.getPosition();
			double distanceSquared = getSquareDistanceBetweenPoints(position, actorPos);
			double deltaDistance = distanceSquared - mTargetDistanceSquared;
			
			// treat distance squared as basically the force we will be applying
			// f.x + f.y = deltaDistance
			// cap at a max value
			deltaDistance = Math.min(deltaDistance, cMaxDistanceForForce);
			double dx = actorPos.x - position.x;
			double dy = actorPos.y - position.y;
			
			if (dx == 0 && dy == 0) {
				dx = -1 + Math.random() * 2;
				dy = -1 + Math.random() * 2;
			}
			
			// normalise
			double mag = Math.sqrt(dx * dx + dy * dy);
			
			
			float fx = (float) (dx / mag * deltaDistance);
			float fy = (float) (dy / mag * deltaDistance);
			
			mCachedAccel.set(fx, fy);
			return mCachedAccel;
		}
		
	}
	
	@Override
	public void setPosition(PointF newPos) {
		mIsActive = true;
		mLastPosition = mPosition;
		mPosition = newPos;
	}

	public PointF getVelocity() {
		return mVelocity;
	}
	
	public void setVelocity(PointF velocity) {
		mIsActive = true;
		mVelocity = velocity;
	}
	
	public void addForceSource(RootActor actor, double targetDistance) {
		mForceSources.add(new ForceSource(actor, targetDistance));
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
			double deltaSeconds = deltaT / 1000.0;
			calculateCurrentAcceleration();
			updateVelocity(deltaSeconds);
			updatePosition(deltaSeconds);
		}
	}
	
	private void calculateCurrentAcceleration() {
		mAcceleration.set(0,0);
		for (ForceSource source : mForceSources) {
			PointF sourceAccel = source.getAcceleration(mPosition, 1f); // TODO: actual mass value
			mAcceleration.x += sourceAccel.x;
			mAcceleration.y += sourceAccel.y;
		}
	}

	private void updateVelocity(double deltaSeconds) {
		switch (mMode) {
			case STANDARD: {
				mVelocity.x = (float) ((mVelocity.x + mAcceleration.x * deltaSeconds) * cFrictionConstant);
				mVelocity.y = (float) ((mVelocity.y + mAcceleration.y * deltaSeconds) * cFrictionConstant);
				break;
			}
			case ORBIT: {
				break;
			}
		}
	}

	private void updatePosition(double deltaSeconds) {
		mLastPosition.set(mPosition);

		switch (mMode) {
			case STANDARD: {
				mPosition.x += mVelocity.x * deltaSeconds;
				mPosition.y += mVelocity.y * deltaSeconds;
				break;
			}
			case ORBIT: {
				double angle = ((SystemClock.elapsedRealtime() - mOrbitStartTime) / 1000f * cOrbitalVelocity) % (2f * Math.PI);
				setPosition(new PointF((float) (mOrbitRadius * Math.sin(angle)), (float) (mOrbitRadius * Math.cos(angle))));
				break;
			}
		}
	}
	
	private double getSpeedSquared() {
		return (mVelocity.x * mVelocity.x) + (mVelocity.y * mVelocity.y);
	}
	
	private static double getSquareDistanceBetweenPoints(PointF a, PointF b) {
		return (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y);
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
				mVelocity.set(0,0);
				return false;
			}
		}
		return true;
	}

}
