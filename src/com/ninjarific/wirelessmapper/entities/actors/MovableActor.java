package com.ninjarific.wirelessmapper.entities.actors;

import java.util.HashSet;
import java.util.Set;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;

import com.ninjarific.wirelessmapper.utilties.MathUtils;

public class MovableActor extends RootActor {
	private static final String TAG = "MoveableActor";
	private static final boolean DEBUG = true;
	
	private static final double cInactiveVelocityCutoff = 0.01;
	private static final long cInactiveMsCutoff = 1500; // time before an almost stationary object stops itself
	private static final double cInactiveDistanceCutoff = 0.001;
	private static final double cMaxDistanceForForce = 150 * 150;
	private static final double cBaseFrictionConstant = 0.2; // 1 is no friction, 0 means no movement possible
	private static final double cVariableFrictionConstant = 0.5; // max change in friction as distance changes
	private static final double cMaxDistanceForFrictionVariance = 100*100; // distance at which ceases to change
	private static final double cForceActivateThreashold = 200*200;
	
	private final double cOrbitalVelocity = 0.3 + (0.3 * Math.random());

	private PointF mPosition;
	private PointF mLastPosition;
	private PointF mVelocity;
	private PointF mAcceleration;
	private boolean mIsActive;
	private long mLastActive;
	protected Mode mMode = Mode.STANDARD;
	protected long mOrbitStartTime;
	private int mOrbitRadius;
	private Set<ForceSource> mForceSources;
	private double mMass = 1;
	private boolean mForceSourcesLocked;
	private Set<ForceSource> mForceSourcesToAdd;
	private double mBestDistance;
	
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

	public MovableActor(PointF position, double mass, boolean isActive) {
		mPosition = position;
		mLastPosition = new PointF(mPosition.x, mPosition.y);
		mIsActive = isActive;
		mMass = mass;
		mVelocity = getRandomVelocity();
		mAcceleration = new PointF();
		mLastActive = SystemClock.elapsedRealtime();
		mForceSources = new HashSet<ForceSource>();
		mForceSourcesToAdd = new HashSet<ForceSource>();
		if (DEBUG) Log.d(TAG, "isActive on startup " + mIsActive);
	}
	
	private PointF getRandomVelocity() {
		float dx = (float) (-10 + Math.random() * 20);
		float dy = (float) (-10 + Math.random() * 20);
		
		// normalise
		double mag = Math.sqrt(dx * dx + dy * dy);
		
		
		float fx = (float) ((dx / mag * 1000) / mMass);
		float fy = (float) ((dy / mag * 1000) / mMass);
		
		return new PointF(fx, fy);
	}
	
	private class ForceSource {
		private MovableActor mActor;
		private double mTargetDistanceSquared;
		private PointF mCachedAccel;
		private double mDeltaDistance;

		public ForceSource(MovableActor actor, double targetDistance) {
			mActor = actor;
			mTargetDistanceSquared = 10 * targetDistance * targetDistance;
			mCachedAccel = new PointF();
			mDeltaDistance = cMaxDistanceForFrictionVariance;
		}
		
		public PointF getAcceleration(PointF position) {
			PointF actorPos = mActor.getPosition();
			double distanceSquared = MathUtils.getSquareDistanceBetweenPoints(position, actorPos);
			mDeltaDistance = distanceSquared - mTargetDistanceSquared;
			
			// treat distance squared as basically the force we will be applying
			// f.x + f.y = deltaDistance
			// cap at a max value
			mDeltaDistance = Math.min(Math.max(mDeltaDistance, -cMaxDistanceForForce), cMaxDistanceForForce);
			double dx = actorPos.x - position.x;
			double dy = actorPos.y - position.y;
			
			if (dx == 0 && dy == 0) {
				dx = -1 + Math.random() * 2;
				dy = -1 + Math.random() * 2;
			}
			
			// normalise
			double mag = Math.sqrt(dx * dx + dy * dy);
			
			
			float fx = (float) ((dx / mag * mDeltaDistance) / mMass);
			float fy = (float) ((dy / mag * mDeltaDistance) / mMass);
			
			mCachedAccel.set(fx, fy);
			return mCachedAccel;
		}

		public void activate() {
			mActor.activate();
		}
		
		public MovableActor getActor() {
			return mActor;
		}
		
		public double getTargetDistance() {
			return mTargetDistanceSquared;
		}
		
		public double getDeltaDistance() {
			return mDeltaDistance;
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof ForceSource
					&& mActor.equals(((ForceSource) o).getActor())
					&& mTargetDistanceSquared == ((ForceSource) o).getTargetDistance());
		}

		@Override
		public int hashCode() {
			int hash = 13;
			hash += this.getClass().hashCode();
			hash += mActor.hashCode();
			hash += mTargetDistanceSquared;
			return hash;
		}
		
		
	}
	
	public void setPosition(PointF newPos) {
		mIsActive = true;
		mLastPosition = mPosition;
		mPosition = newPos;
	}

	public PointF getPosition() {
		return mPosition;
	}

	public PointF getVelocity() {
		return mVelocity;
	}
	
	public void setVelocity(PointF velocity) {
		mIsActive = true;
		mVelocity = velocity;
	}
	
	public void addForceSource(MovableActor actor, double targetDistance, boolean addMass) {
		if (actor != null) {
			ForceSource fs = new ForceSource(actor, targetDistance);
			if (addMass && !mForceSources.contains(fs)) {
				addMass(actor.getMass());
			}
			if (mForceSourcesLocked) {
				mForceSourcesToAdd.add(fs);
			} else {
				mForceSources.add(fs);
			}
		} else {
			Log.w(TAG, "addForceSource() passed null actor");
		}
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
	
	protected void setMass(double mass) {
		mMass = mass;
	}
	
	private void addMass(double mass) {
		mMass += mass;
	}
	
	public double getMass() {
		return mMass;
	}
	
	public void activate() {
		mIsActive = true;
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
		mForceSourcesLocked = true;
		mBestDistance = cMaxDistanceForFrictionVariance;
		for (ForceSource source : mForceSources) {
			PointF sourceAccel = source.getAcceleration(mPosition);
			mAcceleration.x += sourceAccel.x;
			mAcceleration.y += sourceAccel.y;
			
			// TODO: don't do this, especially not here
			if (Math.abs(source.getDeltaDistance()) < mBestDistance) {
				mBestDistance = Math.abs(source.getDeltaDistance());
			}
		}
		// TODO: work out a better way of reawakening force sources
		if (mAcceleration.x*mAcceleration.x > cForceActivateThreashold ||mAcceleration.y*mAcceleration.y > cForceActivateThreashold){
			awakenConnectedForceSources();
		}
		mForceSourcesLocked = false;
		onForceSourcesUnlocked();
	}

	private void awakenConnectedForceSources() {
		for (ForceSource fs : mForceSources) {
			fs.activate();
		}
	}
	
	private void onForceSourcesUnlocked() {
		for (ForceSource s : mForceSourcesToAdd) {
			mForceSources.add(s);
		}
		mForceSourcesToAdd.clear();
	}

	private void updateVelocity(double deltaSeconds) {
		switch (mMode) {
			case STANDARD: {
				double friction = calculateFriction();
				mVelocity.x = (float) ((mVelocity.x + mAcceleration.x * deltaSeconds) * friction);
				mVelocity.y = (float) ((mVelocity.y + mAcceleration.y * deltaSeconds) * friction);
				break;
			}
			case ORBIT: {
				break;
			}
		}
	}
	
	private double calculateFriction() {
		// TODO: 
		// have a count of connections within a 'close enough' tolerance
		// to desired position
		// compare this count to total count of connections
		// apply this fraction as a factor to the friction calculation:
		// the closer we get to all nodes being in desired location,
		// the closer friction tends to base friction constant
		double friction = cBaseFrictionConstant + ((mBestDistance / cMaxDistanceForFrictionVariance) * cVariableFrictionConstant);
		
		return friction;
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
	
	/**
	 * Checks to see if the actor is moving significantly.
	 * @return
	 */
	private boolean checkIfActive() {
		if (Math.abs(mVelocity.x) > cInactiveVelocityCutoff 
				|| Math.abs(mVelocity.y) > cInactiveVelocityCutoff 
				|| MathUtils.getSquareDistanceBetweenPoints(mLastPosition, mPosition) > cInactiveDistanceCutoff) {
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
