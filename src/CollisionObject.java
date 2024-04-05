import java.awt.*;

public abstract class CollisionObject implements Drawable  {
    DoublePoint position;
    public Color color;
    public CollisionObject(DoublePoint p) {
        this.position = p;
    }
    public abstract double getSignedDistance(DoublePoint p);
    public abstract boolean contains(DoublePoint p);
    @Override
    public abstract void drawObject(Graphics2D g2);
    public Double calculateNormalAt(DoublePoint p){ // returns an estimate of the normal at the given position in radians
        double epsilon = 0.01; // arbitrary — should be smaller than any surface detail in the distance function, but not so small as to get lost in float precision
        double centerDistance = getSignedDistance(p);
        double xDistance = getSignedDistance(new DoublePoint(p.x + epsilon,p.y))-centerDistance;
        double yDistance = getSignedDistance(new DoublePoint(p.x,p.y + epsilon))-centerDistance;

        double normal = Math.atan2(yDistance,xDistance);
        // Ensure angle is between 0 and 2π
        return (normal > 0 ? normal : (2*Math.PI + normal));
    }
}
