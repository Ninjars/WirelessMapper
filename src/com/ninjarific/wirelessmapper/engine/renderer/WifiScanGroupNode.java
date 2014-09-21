package com.ninjarific.wirelessmapper.engine.renderer;

import com.ninjarific.wirelessmapper.Constants;
import com.ninjarific.wirelessmapper.engine.renderer.BasicTextRenderNode.VerticalAlign;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;

public class WifiScanGroupNode extends GroupNode {

	private WifiScanActor mActor;

	public WifiScanGroupNode(WifiScanActor actor) {
		mActor = actor;
		CircleRenderNode rend = new CircleRenderNode();
		rend.setColor(Constants.SCAN_RENDERER_COLOR_MED);
		this.addChild(rend);
		
		BasicTextRenderNode label = new BasicTextRenderNode(actor.getActorLabel());
		label.setColor(Constants.SCAN_RENDERER_COLOR_LIGHT);
		label.setVerticalAlign(VerticalAlign.MIDDLE);
		this.addChild(label);
	}
	
	@Override
	protected void update() {
		mTranslation = mActor.getPosition();
	}
}
