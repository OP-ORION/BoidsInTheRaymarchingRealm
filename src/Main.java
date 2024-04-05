import java.awt.*;

public class Main {
    private static final int WIDTH = 2048;
    private static final int HEIGHT = 1024;
    public static void main(String[] args) {
        //System.out.println("Hello world!");
        EventQueue.invokeLater(()-> {
            try {
                new RaymarchingFrame(WIDTH, HEIGHT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}