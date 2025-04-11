package org.example.user_interface;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Visualization {
    private static final int BANDS = 64;
    private static final int WIDTH = 10;
    private static final int HEIGHT = 100;
    private final Group root;
    private final Rectangle[] bars;

    public Visualization() {
        root = new Group();
        bars = new Rectangle[BANDS];

        for (int i = 0; i < BANDS; i++) {
            Rectangle bar = new Rectangle();
            bar.setX(i * (WIDTH + 2));
            bar.setY(HEIGHT);
            bar.setWidth(WIDTH);
            bar.setHeight(0);
            bar.setFill(Color.LIME);
            bars[i] = bar;
            root.getChildren().add(bar);
        }
    }

    public void update(float[] magnitudes) {
        root.getChildren().clear();
        for (int i = 0; i < magnitudes.length; i++) {
            double barHeight = Math.max(10, (60 + magnitudes[i]) * 2);
            Rectangle bar = new Rectangle(i * 10, HEIGHT - barHeight, 8, barHeight);

            bar.setFill(Color.hsb((i * 360.0 / magnitudes.length), 1.0, 1.0));
            root.getChildren().add(bar);
        }
    }

    public Group getRoot() {
        return root;
    }
}
