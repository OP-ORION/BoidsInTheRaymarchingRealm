
public class BoidSettings {
    static int boundWidth = -1;// ignores preset,BOIDPANEL WILL CHANGE THIS
    static int boundHeight = -1; // ignores preset,BOIDPANEL WILL CHANGE THIS

    // behavior
    static double VIEW_RANGE = 50;
    static double SEPARATION_FORCE = 1;
    static double SEPARATION_RANGE = 5;
    static double ALIGNMENT_FORCE = 0.3;
    static double COHESION_FORCE = 0.125;
    static double COHESION_RANGE = 50;
    static double TARGET_FORCE = 0;
    static double BORDER_MARGIN = 15;
    static double TURN_FACTOR = 0.124;
    static double TARGET_MIN_RANGE = 25;
    static double TARGET_MAX_RANGE = 250;
    static double DIF_COLOR_SEP_FORCE = 0.05;
    static double MAX_SPEED = 25;
    static double MIN_SPEED = 15;
    static double DRAG = 1.02;
    static double BATTLE_RANGE = 10;
    static double BATTLE_SPEED_MIN = 60;

    // for drawing
    int BOID_DRAW_SIZE = 8;
    int BOID_SPEED_COMPRESSION_CHANGE = 25;
    int BOID_SPEED_LENGTH_CHANGE = 2;
    double MIN_BOID_DRAW_SPEED = 0.8;// fixes divide by zero error
}