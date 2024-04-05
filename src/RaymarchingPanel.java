import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class RaymarchingPanel extends JPanel {
    ArrayList<CollisionObject> objects;
    public ArrayList<Boid> boids = new ArrayList<Boid>();
    public Camera camera;
    Grid grid;
    BoidSettings settings;
    Random r = new Random();
    NegativeRectangleObject screenBorder = new NegativeRectangleObject(new DoublePoint(),getWidth(),getHeight());
    private static final Color SettingsBackground = new Color(106, 108, 143);
    public long lastDelay = 0;
    private final int delay = 15;
    private final int shapeSizeLimit = 50;
    private final int weirdPolygonSideLimit = 10;
    private final Color screenEdgeColour = new Color(58, 60, 82);
    private final double minStepDistance = 0.1;
    private final double mouseWheelSensetivity = 100;
    private final int objectsCount = 25;
    private final int boidCount = 25;
    double velocityRange = 20;
    final static float[] dash1 = {10.0f};
    final static BasicStroke dashed =
            new BasicStroke(2.5f,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);

    public RaymarchingPanel(int w, int h){
        // deco/ looks
        setPreferredSize(new Dimension(w,h));
        setSize(new Dimension(w,h));
        setVisible(true);
        setBackground(SettingsBackground);
        innitComponents();
    }
    private void innitComponents(){
        int w = getWidth();
        int h = getHeight();

        screenBorder.color = screenEdgeColour;

        settings = new BoidSettings();
        grid = new Grid(w, h, BoidSettings.VIEW_RANGE);
        objects = new ArrayList<CollisionObject>();

        for (int count = 0; count < boidCount; count++){
            Boid addBoid = new Boid(new DoublePoint( (Math.random()*w ),(Math.random()*h)),new DoublePoint((Math.random()*velocityRange-(velocityRange/2)), (Math.random()*velocityRange-(velocityRange/2))),Color.red,settings);
            boids.add(addBoid);
            objects.add(addBoid);
        }

        // add random objects
        camera = new Camera(new DoublePoint(),0);
        for (int i = 0; i < objectsCount; i++) {
            switch (r.nextInt(5)) {
                case 0 -> objects.add(new RectangleObject(new DoublePoint(r.nextDouble(getWidth()-shapeSizeLimit), r.nextDouble(getHeight()-shapeSizeLimit)), r.nextDouble(shapeSizeLimit)));
                case 1 -> objects.add(PolygonObject.generateSquare(new Dimension(getWidth()-shapeSizeLimit, getHeight()-shapeSizeLimit), shapeSizeLimit));
                case 2 -> objects.add(PolygonObject.generateBlob(new Dimension(getWidth()-shapeSizeLimit, getHeight()-shapeSizeLimit), shapeSizeLimit, r.nextInt(weirdPolygonSideLimit)));
                case 3 -> objects.add(new CircleObject(new DoublePoint(r.nextDouble(getWidth()-shapeSizeLimit), r.nextDouble(getHeight()-shapeSizeLimit)), r.nextDouble(shapeSizeLimit)));
                case 4 -> objects.add(PolygonObject.generateSmoothBlob(new Dimension(getWidth()-shapeSizeLimit, getHeight()-shapeSizeLimit), shapeSizeLimit,  r.nextInt(weirdPolygonSideLimit)));
            }

        }
        objects.add(screenBorder);

        //update panel
        new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                repaint();
            }
        }).start();
        //update boids
        new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (RaymarchingFrame.boidsEnabled){
                    long startTime = System.currentTimeMillis();
                    grid.changeDimension(getWidth(),getHeight());// update grid size to current panel size (this also clears the boids)
                    for (Boid boid : boids) {
                        grid.insertBoid(boid); // update the grid
                    }

                    java.util.List<Boid> toRemove = new LinkedList<>();
                    for (Boid boid : boids) {
                        if (boid.markedForRemoval) {
                            toRemove.add(boid);
                        }
                    }
                    boids.removeAll(toRemove);

                    for(Boid boid : boids) {
                        boid.update(grid, RaymarchingPanel.this);
                    }
                    lastDelay = System.currentTimeMillis() - startTime;
                }
            }
        }).start();

        // move camera
        addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (!RaymarchingFrame.ultraSuperDeathMode){
                    camera.position.x = e.getX();
                    camera.position.y = e.getY();
                }
            }
        });

        // rotate camera
        addMouseWheelListener(new MouseAdapter(){
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                if (!RaymarchingFrame.ultraSuperDeathMode){
                    camera.angle += (e.getScrollAmount()*e.getWheelRotation())/mouseWheelSensetivity;
                }
            }
        });

        // update boids bounds when panel resizes
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                BoidSettings.boundWidth = getWidth();// update boids bounds when panel resizes
                BoidSettings.boundHeight = getHeight();// update boids bounds when panel resizes
            }
        });


    }
    public Ray raymarch(DoublePoint position, double angle){
        // update screen size before marching
        screenBorder.width = getWidth();
        screenBorder.height = getHeight();

        ArrayList<March> marches = new ArrayList<>();
        double lastDistance = minStepDistance + 1;
        DoublePoint lastPoint = new DoublePoint(position.x,position.y);
        while (lastDistance > minStepDistance){

            double smallestFoundDistance = Integer.MAX_VALUE;
            CollisionObject closestObject = null;
            Color hitColor = Color.magenta;// debug color;

            for (CollisionObject object:objects) {
                double thisDistance = object.getSignedDistance(lastPoint);
                if (thisDistance < smallestFoundDistance){

                    smallestFoundDistance = thisDistance;
                    closestObject = object;
                    hitColor = object.color;
                }
            }


            marches.add(new March(hitColor, smallestFoundDistance, lastPoint,closestObject));
            lastPoint = new DoublePoint(
                    lastPoint.x + (smallestFoundDistance) * Math.cos(angle),
                    lastPoint.y + (smallestFoundDistance) * Math.sin(angle)
            );

            lastDistance = smallestFoundDistance;
        }
        return new Ray(marches,Math.atan2(position.y - lastPoint.y,position.x - lastPoint.x));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(dashed);

        for (CollisionObject object:objects) {
            g2.setColor(object.color);
            object.drawObject(g2);
        }

        if (RaymarchingFrame.boidsEnabled){
            for (Boid boid : boids) {
                boid.drawObject(g2);
            }
        }
        if (RaymarchingFrame.defaultEnabled){
            Ray cameraMarch = raymarch(camera.position,camera.angle); // basic behavior
            cameraMarch.drawObject(g2);
        }

        if (RaymarchingFrame.visualizerOn){
            g2.setColor(Color.magenta);
            Ray leftSide = raymarch(camera.position,camera.angle - (( - ((double) RaymarchingFrame.visualizerResolution /2))/RaymarchingFrame.visualizerResolution)*RaymarchingFrame.visualizerFOV);
            March leftFirst = leftSide.marches.get(0);
            March leftLast = leftSide.marches.get(leftSide.marches.size()-1);
            g2.drawLine((int) leftFirst.center.x, (int) leftFirst.center.y, (int) leftLast.center.x, (int) leftLast.center.y);

            Ray rightSide = raymarch(camera.position,camera.angle - (( RaymarchingFrame.visualizerResolution- ((double) RaymarchingFrame.visualizerResolution /2))/RaymarchingFrame.visualizerResolution)*RaymarchingFrame.visualizerFOV);
            March rightFirst = rightSide.marches.get(0);
            March rightLast = rightSide.marches.get(rightSide.marches.size()-1);
            g2.drawLine((int) rightFirst.center.x, (int) rightFirst.center.y, (int) rightLast.center.x, (int) rightLast.center.y);
        }




        camera.drawObject(g2);
    }



}
