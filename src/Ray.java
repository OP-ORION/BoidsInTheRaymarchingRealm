import java.awt.*;
import java.util.ArrayList;

public class Ray implements Drawable{

    public Color color;
    public final double distance;
    public final Double normalAtHit;
    public final ArrayList<March> marches;
    public final CollisionObject hitObject;

    public Ray(ArrayList<March> marches,double angle) {
        this.marches = marches;
        color = marches.get(marches.size()-1).color;// make color the color of the hit object
        March first = marches.get(0);
        March last = marches.get(marches.size()-1);
        distance = Math.sqrt((first.center.x - last.center.x) * (first.center.x - last.center.x) + (first.center.y - last.center.y) * (first.center.y - last.center.y));
        normalAtHit = last.hitObject.calculateNormalAt(last.center);
        hitObject = last.hitObject;
    }

    @Override
    public void drawObject(Graphics2D g2) {
        for (March m:marches) {
            m.drawObject(g2);
        }
        g2.setColor(color);
        March first = marches.get(0);
        March last = marches.get(marches.size()-1);
        g2.drawLine((int) first.center.x, (int) first.center.y, (int) last.center.x, (int) last.center.y);
    }
}
