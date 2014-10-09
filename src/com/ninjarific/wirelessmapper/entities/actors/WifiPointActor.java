package com.ninjarific.wirelessmapper.entities.actors;

import android.graphics.PointF;

import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;


public class WifiPointActor extends ForceActor {
	private static final String TAG = "WifiPointActor";
	private static final boolean DEBUG = true;
	
	private String mName;
	private Long mId;
	
	public WifiPointActor(WifiPoint point) {
		super(new PointF(0,0), 5);
		mName = point.getSsid();
		mId = point.getId();
			
	}

	public String getActorLabel() {
		return mId.toString();
	}
	
}
