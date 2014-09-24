package com.ninjarific.wirelessmapper.utilties;

import android.graphics.PointF;

public class MathUtils {

	public static float getSquareDistanceBetweenPoints(PointF a, PointF b) {
		return (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y);
	}
	
	public static float getSquareDistanceBetweenPoints(float ax, float ay, float bx, float by) {
		return (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
	}
}
