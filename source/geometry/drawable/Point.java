package geometry.drawable;


import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class Point extends AbstractDrawable {

	private Point2D point = null;

	private int radius = 3;

	public Point(Point2D point, DrawingContext dc) {
		super(dc);
		this.point = point;
	}

	public Point(Point2D point, int radius, DrawingContext dc) {
		super(dc);
		this.point = point;
		this.radius = radius;
	}

	public void draw(Graphics2D g) {
		g.setColor(getFillColor());
		g.fillOval((int) point.getX() - radius + dc.X_ORIGION, 
				(int) (dc.Y_MAX - point.getY() - dc.Y_ORIGION) - radius, 
				radius * 2, 
				radius * 2
				);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

}
