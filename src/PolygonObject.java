import java.awt.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Random;

public class PolygonObject extends CollisionObject{
    private static final Random r = new Random();
    ArrayList<Line2D> edges;

    public PolygonObject() {
        super(new DoublePoint());//position doesnt matter for this class
        edges = new ArrayList<>();
    }

    public static PolygonObject generateSquare(Dimension positionBounds, int widthBound){
        PolygonObject square = new PolygonObject();
        int width = r.nextInt(widthBound);
        int x1 = r.nextInt(positionBounds.width-width);
        int y1 = r.nextInt(positionBounds.height-width);
        int x2 = x1 + width;
        int y2 = y1 + width;

        square.edges.add(new Line2D.Double(x1,y1,x1,y2));
        square.edges.add(new Line2D.Double(x1,y2,x2,y2));
        square.edges.add(new Line2D.Double(x2,y2,x2,y1));
        square.edges.add(new Line2D.Double(x2,y1,x1,y1));
        square.color = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
        return square;

    }

    public static PolygonObject generateBlob(Dimension positionBounds, int widthBound,int points){
        PolygonObject polygon = new PolygonObject();
        int xPos = r.nextInt(positionBounds.width-widthBound);
        int yPos = r.nextInt(positionBounds.height-widthBound);

        double lastX = xPos + widthBound;
        double lastY = yPos;

        for (int i = 0; i < points; i++) {
            double angle = ((double)i/points)*6; // /6 for angle in radians

            int width = r.nextInt(widthBound);
            double newX = xPos + (width) * Math.cos(angle);
            double newY = yPos + (width) * Math.sin(angle);

            polygon.edges.add(new Line2D.Double(lastX,lastY,newX,newY));

            lastX = newX;
            lastY = newY;
        }

        polygon.edges.add(new Line2D.Double(lastX,lastY,xPos + widthBound,yPos));

        polygon.color = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
        return polygon;

    }

    public static PolygonObject generateSmoothBlob(Dimension positionBounds, int widthBound,int points){
        PolygonObject polygon = new PolygonObject();
        int xPos = r.nextInt(positionBounds.width-widthBound);
        int yPos = r.nextInt(positionBounds.height-widthBound);

        double lastX = xPos + widthBound;
        double lastY = yPos;

        for (int i = 0; i < points; i++) {
            double angle = ((double)i/points)*6; // /6 for angle in radians

            int width = ((widthBound/2) + r.nextInt(widthBound/2));
            double newX = xPos + (width) * Math.cos(angle);
            double newY = yPos + (width) * Math.sin(angle);

            polygon.edges.add(new Line2D.Double(lastX,lastY,newX,newY));

            lastX = newX;
            lastY = newY;
        }

        polygon.edges.add(new Line2D.Double(lastX,lastY,xPos + widthBound,yPos));

        polygon.color = new Color(r.nextInt(255),r.nextInt(255),r.nextInt(255));
        return polygon;

    }

    @Override
    public double getSignedDistance(DoublePoint point) {// lazy implementation NEED TO FIX
        double smallestDistance = Integer.MAX_VALUE;
        for (Line2D line:edges) {
            smallestDistance = Math.min(smallestDistance,line.ptSegDist(point.x,point.y));
        }
        return smallestDistance * (contains(point) ? -1 : 1);
    }

    @Override
    public boolean contains(DoublePoint point) {// lazy implementation NEED TO FIX
        Polygon polygon = new Polygon();
        for (Line2D edge:edges) {
            polygon.addPoint((int) edge.getX1(), (int) edge.getY1());
        }
        return polygon.contains(point.x,point.y);
    }

    @Override
    public void drawObject(Graphics2D g2) {// lazy implementation NEED TO FIX
        Polygon convert = new Polygon();
        g2.setColor(color);
        for (Line2D line:edges) {
            convert.addPoint((int) line.getX1(), (int) line.getY1());
        }
        g2.fillPolygon(convert);
    }
}
