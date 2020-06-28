package com.karanteam.doorkeeper.enumeration;

import java.util.function.Predicate;
import org.opencv.core.Scalar;

public enum Color {
    GREEN(
        new Scalar(214.0, 255.0, 178.0, 0.0),
        color -> color.val[1] > color.val[0] - 20 && color.val[1] > color.val[2] - 20
    ),
    RED(
        new Scalar(178.0, 178.0, 255.0, 0.0),
        color -> color.val[1] > color.val[0] - 20 && color.val[1] > color.val[2] - 20
    ),
    YELLOW(
        new Scalar(0.0, 220.0, 220.0),
        color -> color.val[1] > 200.0 && color.val[2] > 200.0 && color.val[0] < 50.0
    );

    private final Scalar color;
    private final Predicate<Scalar> condition;

    Color(Scalar color, Predicate<Scalar> condition) {
        this.color = color;
        this.condition = condition;
    }

    public Scalar getColor() {
        return color;
    }

    public boolean match(Scalar other, int threshold) {
        return this.condition.test(other)
            && Math.abs(color.val[0] - other.val[0]) < threshold
            && Math.abs(color.val[1] - other.val[1]) < threshold
            && Math.abs(color.val[2] - other.val[2]) < threshold;
    }
}
