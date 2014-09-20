package com.ninjarific.wirelessmapper.engine.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.Log;

import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;

public class WifiScanRenderNode extends RenderNode {
	private static final String TAG = "WifiScanRenderNode";
	private static final boolean DEBUG = true;
	private WifiScanActor mActor;

	public WifiScanRenderNode(WifiScanActor actor) {
		if (DEBUG) Log.d(TAG, "created");
		mActor = actor;
		createPaint();
	}
	
	private void createPaint() {
		mPaint = new Paint();
		mPaint.setARGB(255, 255, 0, 125);
		mPaint.setStyle(Style.FILL);
	}

	@Override
	public void draw(Canvas c) {
		PointF p = mActor.getPosition();
		c.drawCircle(p.x, p.y, 100, mPaint);
		
	}

}
