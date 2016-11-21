package geometry.drawable;

public class DrawingContext {

	public int X_MAX;
	public int Y_MAX;
	
	public int X_ORIGION = 0, Y_ORIGION = 0;
	
	public DrawingContext(int x_max, int y_max, int xorg, int yorg) {
		super();
		X_MAX = x_max;
		Y_MAX = y_max;
		X_ORIGION = xorg;
		Y_ORIGION = yorg;
	}
	
	public DrawingContext(DrawingContext dc) {
		X_MAX = dc.X_MAX;
		Y_MAX = dc.Y_MAX;
	}
	
	public int getX_MAX() {
		return X_MAX;
	}
	
	public void setX_MAX(int x_max) {
		X_MAX = x_max;
	}
	
	public int getY_MAX() {
		return Y_MAX;
	}
	
	public void setY_MAX(int y_max) {
		Y_MAX = y_max;
	}

	public int getX_ORIGION() {
		return X_ORIGION;
	}

	public void setX_ORIGION(int x_origion) {
		X_ORIGION = x_origion;
	}

	public int getY_ORIGION() {
		return Y_ORIGION;
	}

	public void setY_ORIGION(int y_origion) {
		Y_ORIGION = y_origion;
	}
	
}
