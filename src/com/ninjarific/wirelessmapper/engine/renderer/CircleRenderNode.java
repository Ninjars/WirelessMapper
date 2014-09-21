package com.ninjarific.wirelessmapper.engine.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.Log;

public class CircleRenderNode extends RenderNode {
	private static final String TAG = "WifiScanRenderNode";
	private static final boolean DEBUG = true;
	
	private int mColor = Color.WHITE;
	private float mRadius = 30;
	private PointF mOffset = new PointF(0,0);

	public CircleRenderNode() {
		if (DEBUG) Log.d(TAG, "created");
		createPaint();
	}
	
	public void setColor(int color) {
		mPaint.setColor(color);
	}
	
	public void setRadius(float radius) {
		mRadius = radius;
	}
	
	public void setOffset(PointF offset) {
		mOffset = offset;
	}
	
	private void createPaint() {
		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(5);
		mPaint.setAntiAlias(true);
	}

	@Override
	public void draw(Canvas c) {
		c.drawCircle(mOffset.x, mOffset.y, mRadius, mPaint);
		
	}

}
