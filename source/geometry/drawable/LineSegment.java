package geometry.drawable;


import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class LineSegment extends AbstractDrawable {

	private Line2D seg;

	// private boolean direction = false;

	public LineSegment(Line2D seg, DrawingContext dc) {
		super(dc);
		this.seg = seg;
	}

	public void draw(Graphics2D g) {
		g.setColor(getLineColor());
		g.drawLine((int) seg.getX1() - dc.X_ORIGION, (int)(dc.Y_MAX - seg.getY1() + dc.Y_ORIGION), (int) seg.getX2() - dc.X_ORIGION,
				(int) (dc.Y_MAX - seg.getY2() + dc.Y_ORIGION));
	}

}
