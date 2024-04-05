import java.awt.*;

public class March implements Drawable {
    public final double distanceToNearest;
    public final DoublePoint center;
    public final Color color;
    public final CollisionObject hitObject;

    public March(Color colorHit, double radius, DoublePoint center, CollisionObject hitObject) {
        color = colorHit;
        this.distanceToNearest = radius;
        this.center = center;
        this.hitObject = hitObject;

    }

    @Override
    public void drawObject(Graphics2D g2) {
        g2.setColor(color);
        g2.drawOval((int) (center.x - distanceToNearest), (int) (center.y - distanceToNearest), (int) (distanceToNearest *2), (int) (distanceToNearest *2));
    }
}
