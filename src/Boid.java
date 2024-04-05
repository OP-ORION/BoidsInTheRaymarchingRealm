import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Boid extends CollisionObject implements Drawable{
    static Point target = new Point();
    DoublePoint position;
    DoublePoint velocity;
    BoidSettings settings;
    boolean markedForRemoval = false;

    public Boid(DoublePoint p, DoublePoint v, Color c, BoidSettings s) {
        super(p);
        position = p;
        velocity = v;
        color = c;
        settings = s;
    }
    public void updateVelocity(Grid grid,RaymarchingPanel environment) {
        DoublePoint desiredVelocityChange = new DoublePoint();


        DoublePoint interactWithOtherBoids = interactWithOtherBoidsChangeNeeded(grid);
        DoublePoint Target = targetChangeNeeded(); // move towards target if in range
        DoublePoint avoidWall = AvoidWallChangeNeeded(environment);

        desiredVelocityChange.x += Target.x + avoidWall.x + interactWithOtherBoids.x;
        desiredVelocityChange.y += Target.y + avoidWall.y + interactWithOtherBoids.y;

        velocity = DoublePoint.lerp(velocity, new DoublePoint(velocity.x + desiredVelocityChange.x, velocity.y + desiredVelocityChange.y), 0.75);

        // drag
        velocity.x /= BoidSettings.DRAG;
        velocity.y /= BoidSettings.DRAG;
        fixSpeed();
    }

    public void update(Grid grid,RaymarchingPanel environment) {
        position.x += velocity.x;
        position.y += velocity.y;
        updateVelocity(grid,environment);
    }

    public void fixSpeed() {
        double magnitude = Math.sqrt((velocity.x * velocity.x) + (velocity.y * velocity.y));
        if (magnitude < BoidSettings.MIN_SPEED || magnitude > BoidSettings.MAX_SPEED) {
            double scale = magnitude < BoidSettings.MIN_SPEED ? BoidSettings.MIN_SPEED / magnitude : BoidSettings.MAX_SPEED / magnitude;
            velocity.x *= scale;
            velocity.y *= scale;
        }
    }
    public DoublePoint interactWithOtherBoidsChangeNeeded(Grid grid){ // REWORK THIS OH MY GOD THIS IS SO BAD HOLY SHITGRAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        DoublePoint desiredVelocityChange = new DoublePoint();
        // stats to be updated
        DoublePoint avgPos = new DoublePoint();
        DoublePoint avgVelocity = new DoublePoint();
        int boidsInViewRangeAndFriendly = 0;

        List<Boid> localBoids = grid.getNeighbors(this, BoidSettings.VIEW_RANGE);

        // update stats
        for (Boid checkBoid : localBoids) {

            boolean difColor = !this.color.equals(checkBoid.color);
            double xDif = (position.x - checkBoid.position.x);
            double yDif = (position.y - checkBoid.position.y);
            double distance = Math.sqrt((xDif * xDif) + ((yDif * yDif)));
            double relativeSpeed = this.velocity.distance(checkBoid.velocity);

            if (difColor && relativeSpeed > BoidSettings.BATTLE_SPEED_MIN && distance < BoidSettings.BATTLE_RANGE) {
                //checkBoid.markedForRemoval = true;
                //this.markedForRemoval = true;
                return desiredVelocityChange;
            }

            if (distance <= BoidSettings.SEPARATION_RANGE) {
                // Separation
                desiredVelocityChange.translate((xDif * BoidSettings.SEPARATION_FORCE), (yDif * BoidSettings.SEPARATION_FORCE));
            } else if (distance <= BoidSettings.VIEW_RANGE) {
                if (difColor) {
                    // Color Separation
                    desiredVelocityChange.translate((xDif * BoidSettings.DIF_COLOR_SEP_FORCE), (yDif * BoidSettings.DIF_COLOR_SEP_FORCE));
                } else {
                    // cohesion and alignment helper
                    boidsInViewRangeAndFriendly++;
                    avgPos.translate(checkBoid.position.x, checkBoid.position.y);
                    avgVelocity.translate(checkBoid.velocity.x, checkBoid.velocity.y);
                }
            }

        }

        if (boidsInViewRangeAndFriendly != 0) {
            avgVelocity.x /= boidsInViewRangeAndFriendly;
            avgVelocity.y /= boidsInViewRangeAndFriendly;
            avgPos.x /= boidsInViewRangeAndFriendly;
            avgPos.y /= boidsInViewRangeAndFriendly;

            //cohesion
            if (position.distance(avgPos) > BoidSettings.COHESION_RANGE) {
                desiredVelocityChange.x += ((-position.x + avgPos.x) * BoidSettings.COHESION_FORCE);
                desiredVelocityChange.y += ((-position.y + avgPos.y) * BoidSettings.COHESION_FORCE);
            }

            //alignment
            desiredVelocityChange.x += ((-velocity.x + avgVelocity.x) * BoidSettings.ALIGNMENT_FORCE);
            desiredVelocityChange.y += ((-velocity.y + avgVelocity.y) * BoidSettings.ALIGNMENT_FORCE);
        }
        return desiredVelocityChange;
    }
    public DoublePoint targetChangeNeeded() {
        DoublePoint desiredVelocityChange = new DoublePoint();
        if (target != null && position.distance(target) < BoidSettings.TARGET_MAX_RANGE && position.distance(target) > BoidSettings.TARGET_MIN_RANGE) { // check if close enough to target to move towards
            // Targeting
            desiredVelocityChange.x += ((-position.x + target.x) * BoidSettings.TARGET_FORCE);
            desiredVelocityChange.y += ((-position.y + target.y) * BoidSettings.TARGET_FORCE);
        }
        return desiredVelocityChange;
    }

    public DoublePoint AvoidWallChangeNeeded(RaymarchingPanel environment) {
        DoublePoint changeNeeded = new DoublePoint();

        // turn around if about to hit something
        double middleAngle = Math.atan2(this.velocity.y,this.velocity.x);
        double speed = Math.sqrt((this.velocity.x*this.velocity.x) + (this.velocity.y*this.velocity.y))/ settings.BOID_SPEED_COMPRESSION_CHANGE -1;
        Ray middleRay = environment.raymarch(new DoublePoint(
                        this.position.x + (Math.cos(middleAngle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed,
                this.position.y + (Math.sin(middleAngle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed
                ),middleAngle);
        double surfaceAngle = middleRay.normalAtHit;
        if(middleRay.distance <= this.velocity.getMagnitude() || middleRay.distance <= BoidSettings.BORDER_MARGIN  ){ // if going to hit wall, reflect
            velocity.x = Math.cos(((2*surfaceAngle) - middleAngle) - Math.PI * 2/2) * (BoidSettings.BORDER_MARGIN - middleRay.distance);
            velocity.y = Math.sin(((2*surfaceAngle) - middleAngle) - Math.PI * 2/2) * (BoidSettings.BORDER_MARGIN - middleRay.distance);
        }

        changeNeeded.x *= BoidSettings.TURN_FACTOR;
        changeNeeded.y *= BoidSettings.TURN_FACTOR;

        return changeNeeded;
    }


    @Override
    public double getSignedDistance(DoublePoint p) {
        // Calculate direction angle
        double angle = Math.atan2(this.velocity.y, this.velocity.x);
        double speed = Math.sqrt((this.velocity.x*this.velocity.x) + (this.velocity.y*this.velocity.y))/ settings.BOID_SPEED_COMPRESSION_CHANGE;
        if (speed < settings.MIN_BOID_DRAW_SPEED){
            speed = settings.MIN_BOID_DRAW_SPEED;
        }

        // Calculate the three vertices of the triangle
        Point2D p1 = new Point2D.Double((this.position.x + (Math.cos(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE),(this.position.y + (Math.sin(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE));
        Point2D p2 = new Point2D.Double((this.position.x + (Math.cos(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),(this.position.y + (Math.sin(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed));
        Point2D p3 = new Point2D.Double((this.position.x + (Math.cos(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),(this.position.y + (Math.sin(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed));
        Line2D.Double line1 = new Line2D.Double(p1,p2);
        Line2D.Double line2 = new Line2D.Double(p2,p3);
        Line2D.Double line3 = new Line2D.Double(p3,p1);

        return Math.min(line1.ptSegDist(p.x,p.y),Math.min(line2.ptSegDist(p.x,p.y),line3.ptSegDist(p.x,p.y))) * (contains(p) ? -1 : 1);
    }

    @Override
    public boolean contains(DoublePoint p) {
        // Calculate the three vertices of the triangle
        double angle = Math.atan2(this.velocity.y, this.velocity.x);
        double speed = Math.sqrt((this.velocity.x*this.velocity.x) + (this.velocity.y*this.velocity.y))/ settings.BOID_SPEED_COMPRESSION_CHANGE;
        if (speed < settings.MIN_BOID_DRAW_SPEED){
            speed = settings.MIN_BOID_DRAW_SPEED;
        }
        Point2D p1 = new Point2D.Double((this.position.x + (Math.cos(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE),(this.position.y + (Math.sin(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE));
        Point2D p2 = new Point2D.Double((this.position.x + (Math.cos(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),(this.position.y + (Math.sin(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed));
        Point2D p3 = new Point2D.Double((this.position.x + (Math.cos(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),(this.position.y + (Math.sin(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed));

        Polygon polygon = new Polygon();
        polygon.addPoint((int) p1.getX(), (int) p1.getY());
        polygon.addPoint((int) p2.getX(), (int) p2.getY());
        polygon.addPoint((int) p3.getX(), (int) p3.getY());

        return polygon.contains(p.x,p.y);
    }

    @Override
    public void drawObject(Graphics2D g2) {
        g2.setColor(this.color);

        // Calculate direction angle
        double angle = Math.atan2(this.velocity.y, this.velocity.x);
        double speed = Math.sqrt((this.velocity.x*this.velocity.x) + (this.velocity.y*this.velocity.y))/ settings.BOID_SPEED_COMPRESSION_CHANGE;
        if (speed < settings.MIN_BOID_DRAW_SPEED){
            speed = settings.MIN_BOID_DRAW_SPEED;
        }

        // Calculate the three vertices of the triangle
        int[] xPoints = {
                (int) (this.position.x + (Math.cos(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE),
                (int) (this.position.x + (Math.cos(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),
                (int) (this.position.x + (Math.cos(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed)
        };
        int[] yPoints = {
                (int) (this.position.y + (Math.sin(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE),
                (int) (this.position.y + (Math.sin(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),
                (int) (this.position.y + (Math.sin(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed)
        };

        Point2D p1 = new Point2D.Double((this.position.x + (Math.cos(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE),(this.position.y + (Math.sin(angle) * settings.BOID_DRAW_SIZE)*speed*settings.BOID_SPEED_LENGTH_CHANGE));
        Point2D p2 = new Point2D.Double((this.position.x + (Math.cos(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),(this.position.y + (Math.sin(angle + Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed));
        Point2D p3 = new Point2D.Double((this.position.x + (Math.cos(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed),(this.position.y + (Math.sin(angle - Math.PI * 3 / 4) * settings.BOID_DRAW_SIZE)/speed));

        Polygon polygon = new Polygon();
        polygon.addPoint((int) p1.getX(), (int) p1.getY());
        polygon.addPoint((int) p2.getX(), (int) p2.getY());
        polygon.addPoint((int) p3.getX(), (int) p3.getY());

        g2.fillPolygon(polygon);
    }
}
