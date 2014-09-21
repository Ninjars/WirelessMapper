package com.ninjarific.wirelessmapper.engine.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

public class BasicTextRenderNode extends RenderNode {
	private Align mDefaultAlign = Paint.Align.CENTER;
	private int mDefaultColor = Color.WHITE;
	private float mTextSize = 35f;
	private VerticalAlign mVertAlign = VerticalAlign.MIDDLE;
	private String mText;
	
	private Rect mTextBounds = new Rect();
	
	public enum VerticalAlign {
		TOP,
		MIDDLE,
		BOTTOM
	}
	
	public BasicTextRenderNode(String text) {
		mText = text;
		createPaint();
	}
	
	public void setHorizontalAlign(Align align) {
		mPaint.setTextAlign(align);
	}

	public void setVerticalAlign(VerticalAlign align) {
		mVertAlign = align;
	}
	
	public void setTextSize(float size) {
		mTextSize = size;
		mPaint.setTextSize(size);
		mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
	}
	
	public void setText(String text) {
		mText = text;
		mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
	}
	
	public void setColor(int color) {
		mPaint.setColor(color);
	}
	
	private void createPaint() {
		mPaint = new TextPaint();
		mPaint.setColor(mDefaultColor);
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(mDefaultAlign);
		mPaint.setTypeface(Typeface.MONOSPACE);
		mPaint.setTextSize(mTextSize);
		mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
	}

	@Override
	public void draw(Canvas c) {
		switch (mVertAlign) {
			case BOTTOM: {
				c.drawText(mText, 0, 0, mPaint);
				break;
			}
			case TOP: {
				c.drawText(mText, 0, mTextBounds.height(), mPaint);
				break;
			}
			case MIDDLE: {
				c.drawText(mText, 0, mTextBounds.height() / 2f, mPaint);
				break;
			}
		}

	}

}
