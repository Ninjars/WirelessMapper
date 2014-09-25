package com.ninjarific.wirelessmapper;

import android.graphics.Color;

public class Constants {
	public static final boolean DEBUG = true;
	public static final int SCAN_CONNECTION_THREASHOLD = 20;
	public static final int POINT_LEVEL_SIGNIFICANT_VARIATION = 20;

	/*
	 * Color ints
	 */
	public static final int POINT_RENDERER_COLOR_MED = Color.argb(255, 59, 106, 205);
	public static final int POINT_RENDERER_COLOR_LIGHT = Color.argb(255, 132, 163, 229);
	public static final int POINT_RENDERER_COLOR_DARK = Color.argb(255, 12, 55, 148);
	
	public static final int SCAN_RENDERER_COLOR_MED = Color.argb(255, 255, 141, 53);
	public static final int SCAN_RENDERER_COLOR_LIGHT = Color.argb(255, 255, 161, 89);
	public static final int SCAN_RENDERER_COLOR_DARK = Color.argb(255, 221, 96, 0);
}
