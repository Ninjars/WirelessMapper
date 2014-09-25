package com.ninjarific.wirelessmapper.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ninjarific.wirelessmapper.R;

public class BackgroundGradient extends View {
	private static final String TAG = "BackgroundGradient";
	
	private Paint mPaint;
	
	public BackgroundGradient(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public BackgroundGradient(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BackgroundGradient(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	} 
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.w(TAG, "onDraw()");
		if(mPaint == null) {
			defineGradient();
		}
		
	    canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
	}

	public void defineGradient() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    RadialGradient gradient = new RadialGradient(
	            getWidth()/2f,
	            getHeight()/1.54f,
	            getWidth()/2.5f, 
	            new int[] {getResources().getColor(R.color.background_gradient_light), getResources().getColor(R.color.background_gradient_dark)},
	            new float[] {0, 1}, 
	            android.graphics.Shader.TileMode.CLAMP);

	    mPaint.setDither(true);
	    mPaint.setShader(gradient);
	}

}
