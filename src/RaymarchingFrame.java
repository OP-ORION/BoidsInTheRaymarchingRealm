import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RaymarchingFrame extends JFrame {
    // not implemented in menu (boring settings)
    private static final int SettingsPanelWidth = 250;
    private static final int OneDimensionalVisualizerHeight = 250;
    private static final Color SettingsBackground = new Color(86, 90, 124);
    private static final Color textColor = new Color(0, 0, 0);
    final BufferedImage image = ImageIO.read(new File("DoomGun.png"));
    private final int visualizerDelay = 15;
    public static int visualizerResolution = 640;// must be multiple of frame width or edge will be wonky
    public static int distanceDarkenFalloff = 180;
    static double moveSpeed = 5;
    static double swivelSpeed = 0.1; // angle in radian for each mouse press

    // in "game" settings
    public static boolean visualizerOn;
    public static double visualizerFOV = 1.745329;// in radians
    public static boolean boidsEnabled;
    public static boolean fogEnabled;
    public static boolean ultraSuperDeathMode;
    public static boolean defaultEnabled;

    RaymarchingPanel raymarchingPanel;

    public RaymarchingFrame(int w, int h) throws IOException {
        // deco/looks
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(w, h));
        setVisible(true);

        // add panels for settings and main marching panel
        addRaymarchingPanel();
        add1DVisualizer();
        addSettingsPanel();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (ultraSuperDeathMode){
                    switch (e.getKeyChar()){
                        case 'w' : raymarchingPanel.camera.position.x += Math.cos(raymarchingPanel.camera.angle) * moveSpeed; raymarchingPanel.camera.position.y += Math.sin(raymarchingPanel.camera.angle) * moveSpeed;break;
                        case 'a' : raymarchingPanel.camera.angle -= swivelSpeed;break;
                        case 's' : raymarchingPanel.camera.position.x -= Math.cos(raymarchingPanel.camera.angle) * moveSpeed; raymarchingPanel.camera.position.y -= Math.sin(raymarchingPanel.camera.angle) * moveSpeed; break;
                        case 'd' : raymarchingPanel.camera.angle += swivelSpeed;break;
                        case ' ' :
                            CollisionObject hit = raymarchingPanel.raymarch(raymarchingPanel.camera.position,raymarchingPanel.camera.angle).hitObject;
                            if (hit.getClass().getName().equals("Boid")){
                                //KILL EM DEAD
                                hit.color = new Color(0,0,255);//dont acctually kill them, thats wrong, make em ghosts
                            }
                        ;break;
                    }
                }
            }
        });
        requestFocus();
    }

    private void addRaymarchingPanel(){
        raymarchingPanel = new RaymarchingPanel(getWidth() - SettingsPanelWidth, getHeight());
        add(raymarchingPanel);
    }

    private void addSettingsPanel() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setPreferredSize(new Dimension(SettingsPanelWidth, getHeight()));
        settingsPanel.setLayout(new GridLayout(6, 2));
        settingsPanel.setBackground(SettingsBackground);


        // this controls the FOV in degrees
        JLabel fOVLabel = new JLabel("FOV");
        fOVLabel.setForeground(textColor);
        settingsPanel.add(fOVLabel);
        JSpinner viewRangeSpinner = new JSpinner();
        viewRangeSpinner.setValue(visualizerFOV* (180/Math.PI));
        viewRangeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner source = (JSpinner)e.getSource();
                visualizerFOV = ((Integer)source.getValue()) / (57.29578);
            }
        });
        viewRangeSpinner.setBackground(SettingsBackground);
        viewRangeSpinner.setPreferredSize(new Dimension(100,25));
        settingsPanel.add(viewRangeSpinner);

        JLabel visuzlizerLabel = new JLabel("visualizer enabled");
        visuzlizerLabel.setForeground(textColor);
        settingsPanel.add(visuzlizerLabel);
        JCheckBox visualizerCheck = new JCheckBox();
        visualizerCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizerOn = visualizerCheck.isSelected();
            }
        });
        visualizerCheck.setBackground(SettingsBackground);
        visualizerCheck.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(visualizerCheck);

        JLabel fogLabel = new JLabel("fog enabled");
        fogLabel.setForeground(textColor);
        settingsPanel.add(fogLabel);
        JCheckBox fogCheckBox = new JCheckBox();
        fogCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fogEnabled = fogCheckBox.isSelected();
            }
        });
        fogCheckBox.setBackground(SettingsBackground);
        fogCheckBox.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(fogCheckBox);

        JLabel defaultBehaviorLabel = new JLabel("default draw enabled");
        defaultBehaviorLabel.setForeground(textColor);
        settingsPanel.add(defaultBehaviorLabel);
        JCheckBox defaultBehaviorCheckbox = new JCheckBox();
        defaultBehaviorCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultEnabled = defaultBehaviorCheckbox.isSelected();
            }
        });
        defaultBehaviorCheckbox.setBackground(SettingsBackground);
        defaultBehaviorCheckbox.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(defaultBehaviorCheckbox);

        JLabel boidsLabel = new JLabel("boids enabled");
        boidsLabel.setForeground(textColor);
        settingsPanel.add(boidsLabel);
        JCheckBox boidsCheckBox = new JCheckBox();
        boidsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boidsEnabled = boidsCheckBox.isSelected();
            }
        });
        boidsCheckBox.setBackground(SettingsBackground);
        boidsCheckBox.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(boidsCheckBox);

        JLabel doomLabel = new JLabel("<html>ULTRA-DEATH MODE<br>WARNING:<br> DO NOT ENABLE</html>");
        doomLabel.setForeground(textColor);
        settingsPanel.add(doomLabel);
        JCheckBox doomCheckBox = new JCheckBox();
        doomCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doomLabel.setText("<html>WASD TO MOVE<br>SPACE TO SHOOT<br>KILL THOSE BOIDS</html>");
                raymarchingPanel.camera.position = new DoublePoint(raymarchingPanel.getWidth()/2,raymarchingPanel.getHeight()/2);
                ultraSuperDeathMode = doomCheckBox.isSelected();
                visualizerCheck.setSelected(doomCheckBox.isSelected());
                fogEnabled = doomCheckBox.isSelected();
                fogCheckBox.setSelected(doomCheckBox.isSelected());
                visualizerOn = doomCheckBox.isSelected();
                defaultBehaviorCheckbox.setSelected(!doomCheckBox.isSelected());
                defaultEnabled = !doomCheckBox.isSelected();
                boidsCheckBox.setSelected(doomCheckBox.isSelected());
                boidsEnabled = doomCheckBox.isSelected();
            }
        });
        doomCheckBox.setBackground(SettingsBackground);
        doomCheckBox.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(doomCheckBox);



        add(settingsPanel, BorderLayout.EAST);

    }

    private void add1DVisualizer() throws IOException {

        JPanel visualizerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = getWidth();
                int height = getHeight();
                for (int i = 0; i <= visualizerResolution; i++) {

                    // shoot ray
                    Ray renderRay = raymarchingPanel.raymarch(
                            raymarchingPanel.camera.position,
                            raymarchingPanel.camera.angle - (((double) i - ((double) visualizerResolution / 2)) / visualizerResolution) * visualizerFOV
                    );

                    Color renderColor = renderRay.color;
                    // postprocessing
                    if (fogEnabled) {
                        renderColor = new Color(
                                Math.min((int) (renderRay.color.getRed() / renderRay.distance * distanceDarkenFalloff), renderRay.color.getRed()),
                                Math.min((int) (renderRay.color.getGreen() / renderRay.distance * distanceDarkenFalloff), renderRay.color.getGreen()),
                                Math.min((int) (renderRay.color.getBlue() / renderRay.distance * distanceDarkenFalloff), renderRay.color.getBlue())
                        );
                    }

                    // draw to screen
                    g.setColor(renderColor);
                    g.fillRect(i * (width / visualizerResolution), 0, (width / visualizerResolution), height);
                }
                if (ultraSuperDeathMode) {
                    g.drawImage(image, (width / 2) - image.getWidth(), height - image.getHeight(), this);
                }
            }
        };
        visualizerPanel.setVisible(true);
        visualizerPanel.setPreferredSize(new Dimension(getWidth(), OneDimensionalVisualizerHeight));
        add(visualizerPanel, BorderLayout.SOUTH);

        //update panel
        new Timer(visualizerDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                requestFocus();
                if (visualizerOn) {
                    visualizerPanel.setVisible(true);
                    visualizerPanel.repaint();
                } else {
                    visualizerPanel.setVisible(false);
                }
            }
        }).start();
    }

}