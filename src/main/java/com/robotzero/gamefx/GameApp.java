package com.robotzero.gamefx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.sslogger.Logger;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

public class GameApp extends GameApplication {
    private static final Logger log = Logger.get(GameApp.class);
    private Vec2D one = new Vec2D(0, 540);
    private Vec2D two = new Vec2D(0, 540);
    private Line line = new Line();
    private Rectangle rectangle = new Rectangle();
    private Circle circle = new Circle();
    private Vec2D newV = new Vec2D(two.getX(), two.getY());
    private Vec2D circleCenter = new Vec2D(1920 / 2, 1080 / 2);
    private Pane pane;
    private double theta = 0;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Breakout Underwater");
        gameSettings.setVersion("0.2");
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
        rectangle.setHeight(10);
        rectangle.setWidth(10);
        rectangle.setX(0);
        rectangle.setY(540);
        line.setStartX(1920 / 2);
        line.setStartY(1080 / 2);
        line.setEndX((1920 /2) + 520 );
        line.setEndY(540);
        line.setStroke(Color.RED);
        line.setStrokeWidth(5);
        circle.setRadius(520);
        circle.setCenterX(1920 / 2);
        circle.setCenterY(1080 / 2);
        circle.setStrokeWidth(5);
        circle.setStroke(Color.BLACK);

        pane = new Pane(rectangle, circle, line);
    }

    @Override
    protected void onUpdate(double tpf) {
        double ax, by;
        ax = this.newV.getX();
//        float x = width / 2 + cos(a) * r;
//        float y = height / 2 + sin(a) * r;
        int bb = (int) (100 * Math.sin(0.2 * ax / 3.14));
        Vec2D norm = this.normalizeCoord(this.newV);
        Vec2D circleNorm = this.normalizeCoord(new Vec2D(circleCenter.getX(), circleCenter.getY()));
        Vec2D radiusNorm = this.normalizeCoord(new Vec2D((1920 / 2) + 520, 540));
        Vec2D sins = new Vec2D(norm.getX(), 1 * Math.sin(norm.getX() * 100 / Math.PI));
        Vec2D circleNorm2 = new Vec2D(this.circleCenter.getX() + Math.cos(theta) * 520, this.circleCenter.getY() + Math.sin(theta) * 520);
        Vec2D circleNorm3 = new Vec2D(circleNorm.getX() + Math.cos(theta) * radiusNorm.getX(), circleNorm.getY() + Math.sin(theta));
        Vec2D backToScreen = toScreenCoord(sins);
        Vec2D backToScreenCirle = toScreenCoord(circleNorm3);
        this.newV = new Vec2D(backToScreen.getX() + 1, backToScreen.getY());
//        System.out.println(Math.cos(theta));
        theta = theta + tpf;
        try {
            Thread.sleep(10);
            rectangle.setX(backToScreen.getX());
            rectangle.setY(backToScreen.getY());
            line.setEndX(circleNorm2.getX());
            line.setEndY(circleNorm2.getY());
//            line.setEndX(backToScreenCirle.getX());
//            line.setEndY(backToScreenCirle.getY());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initUI() {
        getGameScene().addUINode(pane);
    }

    private static Vec2D normalizeCoord(Vec2D toTranslate) {
        double xx = toTranslate.getX() / (1920 / 2.0f);  // This will give a value from 0 - 2
        double yy = toTranslate.getY() / (1080 / 2.0f);  // This will give a value from 0 - 2
        xx -= 1.0f; // Subtract 1 to get a value from -1 to 1.
        yy -= 1.0f; // Subtract 1 to get a value from -1 to 1.
        return new Vec2D(xx, yy);
    };

    private static Vec2D toScreenCoord(Vec2D toTranslate) {
        float old_range = 2;
        float new_range = 1080;
        double newXX = 0 + (toTranslate.getX() - (-1)) * 1920 / old_range;
        double newYYy = 0 + (toTranslate.getY() - (-1)) * new_range / 2;
        return new Vec2D(newXX, newYYy);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
