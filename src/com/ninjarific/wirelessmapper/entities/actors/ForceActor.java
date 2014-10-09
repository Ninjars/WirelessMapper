package com.ninjarific.wirelessmapper.entities.actors;

import java.util.HashSet;
import java.util.Set;

import android.graphics.PointF;
import android.util.Log;

import com.ninjarific.wirelessmapper.engine.forces.ForceConnection;

public class ForceActor extends MobileActor {
	private static final String TAG = "ForceActor";
	private static final boolean DEBUG = true;
	private double mMass;
	private Set<ForceConnection> mForceConnections;
	private PointF mActingForce;

	public ForceActor(PointF position, double mass) {
		super(position);
		mMass = mass;
		mForceConnections = new HashSet<ForceConnection>();
		mActingForce = new PointF(0,0);
	}
	
	public PointF getPosition() {
		return super.getPosition();
	}
	
	@Override
	protected PointF getAcceleration() {
		mActingForce.set(0,0);
		for (ForceConnection connection : mForceConnections) {
			PointF force = connection.getForce();
			mActingForce.x += force.x / mMass;
			mActingForce.y += force.y / mMass;
		}
		return mActingForce;
	}
	
	public void addForceConnection(ForceConnection connection) {
		if (DEBUG) Log.i(TAG, "addForceConnection() " + connection.getClass().getSimpleName());
		mForceConnections.add(connection);
	}
}
