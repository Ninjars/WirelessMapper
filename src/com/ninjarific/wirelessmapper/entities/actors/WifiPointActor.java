package com.ninjarific.wirelessmapper.entities.actors;

import java.util.HashMap;

import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiPointActorDescriptor;


public class WifiPointActor extends MoveableActor {
	
	private HashMap<Long, Integer> mScanConnections;
	private String mName;
	private Long mId;

	public WifiPointActor(WifiPointActorDescriptor desc) {
		super(desc);
		WifiPoint point = desc.getPoint();
		mScanConnections = desc.getScanConnections();
		mName = point.getSsid();
		mId = point.getId();
	}
	
	// TODO: create a bitmap for the actor
	// TODO: add functions to get the bitmap for when the actor is passed to the render node
}
