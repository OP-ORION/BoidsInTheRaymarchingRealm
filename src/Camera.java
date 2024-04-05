import java.awt.*;

public class Camera implements Drawable{
    public Color color = Color.BLACK;
    double angle; // angle in radians, position in pixels
    DoublePoint position;
    public Camera(DoublePoint position, double angle) {
        this.position = position;
        this.angle = angle;
    }

    @Override
    public void drawObject(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval((int) (position.x-5), (int) (position.y-5),10,10);
    }
}
