package com.ninjarific.wirelessmapper.graphics.renderers;

import android.util.Log;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.entities.actors.WifiPointActor;
import com.ninjarific.wirelessmapper.graphics.renderers.BasicTextRenderNode.VerticalAlign;

public class WifiPointGroupNode extends GroupNode {
	private static final String TAG = "WifiPointGroupNode";
	private static final boolean DEBUG = true;

	private WifiPointActor mActor;

	public WifiPointGroupNode(WifiPointActor actor) {
		if (DEBUG) Log.d(TAG, "created graphics node");
		mActor = actor;
		CircleRenderNode rend = new CircleRenderNode();
		rend.setColor(Constants.POINT_RENDERER_COLOR_MED);
		rend.setRadius(25);
		this.addChild(rend);

		if (DEBUG) Log.d(TAG, "starting position = " + actor.getPosition());
		if (DEBUG) Log.d(TAG, "starting velocity = " + actor.getVelocity());
		
		BasicTextRenderNode label = new BasicTextRenderNode(actor.getActorLabel());
		label.setColor(Constants.POINT_RENDERER_COLOR_LIGHT);
		label.setVerticalAlign(VerticalAlign.MIDDLE);
		this.addChild(label);
	}
	
	@Override
	protected void update() {
		mTranslation = mActor.getPosition();
	}
}
