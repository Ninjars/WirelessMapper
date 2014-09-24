package com.ninjarific.wirelessmapper.entities.actors;

import android.graphics.PointF;

public abstract class RootActor {
	protected PointF mPosition;

	public abstract void update(long timeDeltaMs);


	public PointF getPosition() {
		return mPosition;
	}

	public void setPosition(PointF position) {
		mPosition = position;
	}
}
