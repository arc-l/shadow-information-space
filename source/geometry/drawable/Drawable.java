package geometry.drawable;

import java.awt.Graphics2D;

public interface Drawable {

	public void preDraw(Graphics2D g);
	public void draw(Graphics2D g);
	public void drawText(Graphics2D g);
	public void drawDebugText(Graphics2D g);
	public void postDraw(Graphics2D g);
	public void paint(Graphics2D g);

	public void setDrawText(boolean drawText);
	public boolean isDrawText();

	public void setDrawDebugText(boolean drawText);
	public boolean isDrawDebugText();
}
