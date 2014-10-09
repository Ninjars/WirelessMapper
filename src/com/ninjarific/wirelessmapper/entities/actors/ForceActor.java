package com.ninjarific.wirelessmapper.entities.actors;

import java.util.ArrayList;

import android.graphics.PointF;

import com.ninjarific.wirelessmapper.engine.forces.ForceConnection;

public class ForceActor extends MobileActor {
	private double mMass;
	private ArrayList<ForceConnection> mForceConnections;
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
	
}
