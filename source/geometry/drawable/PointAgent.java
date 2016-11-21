package geometry.drawable;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class PointAgent extends AbstractDrawable {

	public Point2D location = null;

	public int radius = 10;

	public final int id;

	public PointAgent(Point2D point, DrawingContext dc, int id) {
		super(dc);
		this.id = id;
		this.location = point;
	}

	public PointAgent(Point2D point, int radius, DrawingContext dc, int id) {
		super(dc);
		this.location = point;
		this.id = id;
		this.radius = radius;
	}

	public void draw(Graphics2D g) {

		BasicStroke bs = (BasicStroke) g.getStroke();
		g.setStroke(new BasicStroke(dc.X_MAX / 500));
		int dRadius = radius * dc.X_MAX / 800;
		g.setColor(getFillColor());
		g.fillOval((int) location.getX() - dc.X_ORIGION - dRadius, (int) (dc.Y_MAX
				- location.getY() + dc.Y_ORIGION) - dRadius, dRadius * 2, dRadius * 2);
		g.setColor(getTextColor());
		g.drawOval((int) location.getX() - dc.X_ORIGION - dRadius, (int) (dc.Y_MAX
				- location.getY() + dc.Y_ORIGION) - dRadius, dRadius * 2, dRadius * 2);

		// Agent ID text
		g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN,
				16 * dc.X_MAX / 1000));
		g.setColor(getTextColor());
		g.drawString("" + id, (int) (location.getX() + dc.X_MAX / 200)
				- dc.X_ORIGION + (id < 10 ? 8 * dc.X_MAX / 1000 : 0), (int) (dc.Y_MAX + dc.Y_ORIGION
				- location.getY() + dc.Y_MAX / 200 + 16 * dc.X_MAX / 1000));
		g.setStroke(bs);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

}
