import java.awt.*;
import java.util.Random;

public class CircleObject extends CollisionObject{
    double radius;
    Random r = new Random();

    public CircleObject(DoublePoint p, double radius) {
        super(p);
        this.color = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
        this.radius = radius;
    }

    @Override
    public double getSignedDistance(DoublePoint point) {
        return Math.sqrt(((point.x - (position.x)) * (point.x - (position.x ))) + ((point.y - (position.y )) * (point.y - (position.y )))) - radius ;
    }

    @Override
    public boolean contains(DoublePoint point) {
        return Math.sqrt((position.x - point.x) * (position.x - point.x) + (position.y - point.y) * (position.y - point.y)) < radius;
    }

    @Override
    public void drawObject(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval((int)position.x - (int)radius,(int)position.y - (int)radius,(int)radius*2,(int)radius*2);
    }

}
