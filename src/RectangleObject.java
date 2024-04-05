import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Random;

public class RectangleObject extends CollisionObject{
    double width,height;
    Random r = new Random();

    public RectangleObject(DoublePoint p, double width) {
        super(p);
        this.width = width;
        this.height = height;
        this.color = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
    }

    @Override
    public double getSignedDistance(DoublePoint point) {
        double botLeftBotRight = new Line2D.Double(position.x,position.y,position.x + width, position.y).ptSegDist(point.x,point.y);
        double botLeftTopLeft = new Line2D.Double(position.x,position.y,position.x , position.y+ height).ptSegDist(point.x,point.y);
        double topRightBotRight = new Line2D.Double(position.x+ width,position.y + height,position.x + width, position.y).ptSegDist(point.x,point.y);
        double topRightTopLeft = new Line2D.Double(position.x,position.y+height,position.x + width, position.y + height).ptSegDist(point.x,point.y);
        return Math.min(botLeftBotRight,Math.min(botLeftTopLeft,Math.min(topRightBotRight,topRightTopLeft)))  * (contains(point) ? -1 : 1);
    }

    @Override
    public boolean contains(DoublePoint point) {
        return (position.x < point.x && position.x + width > point.x && position.y < point.y && position.y + height > point.y);
    }

    @Override
    public void drawObject(Graphics2D g2) {
        g2.setColor(color);
        g2.fillRect((int)position.x ,(int)position.y ,(int)width,(int)height);
    }
}

