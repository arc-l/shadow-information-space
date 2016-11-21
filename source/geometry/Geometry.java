package geometry;

import geometry.drawable.DrawingContext;

import java.util.Properties;


public class Geometry {
	public static float SCALING_FACTOR;

	public static final int CANVAS_WIDTH;

	public static final int CANVAS_HEIGHT;

	public static final int CANVAS_MARGIN;

	public static final boolean AUTO_RESIZE;

	public static final int MIN_WIDTH;

	public static final int MIN_HEIGHT;
	
	public static final DrawingContext DEFAULT_DC;

	static {
		Properties geomProp = FileHelper.loadUrlProfile(
				FileHelper.getBaseUrl(), "config/geometry.properties");
		SCALING_FACTOR = Float.parseFloat(geomProp.getProperty("scaling-factor")
				.trim());
		CANVAS_WIDTH = Integer.parseInt(geomProp.getProperty("canvas-width")
				.trim());
		CANVAS_HEIGHT = Integer.parseInt(geomProp.getProperty("canvas-height")
				.trim());
		CANVAS_MARGIN = Integer.parseInt(geomProp.getProperty("canvas-margin")
				.trim());
		AUTO_RESIZE = (Integer.parseInt(geomProp.getProperty("auto-resize")
				.trim()) == 1);
		MIN_WIDTH = Integer.parseInt(geomProp.getProperty("minimum-width")
				.trim());
		MIN_HEIGHT = Integer.parseInt(geomProp.getProperty("minimum-height")
				.trim());
		DEFAULT_DC = new DrawingContext((int)SCALING_FACTOR, (int)SCALING_FACTOR, 0 , 0);
	}

}
