package com.karanteam.doorkeeper.data;

import org.opencv.core.Size;

public enum OfficePositionOrientation {
    NORTH(9, 4, 0, 20, 20),
    EAST(16, 9, 1, 20, 20),
    SOUTH(10, 16, 2, 20, 20),
    WEST(4, 10, 3, 20, 20);

    final int centerX;
    final int centerY;
    final int rotations;
    final int width;
    final int height;

    OfficePositionOrientation(int centerX, int centerY, int rotations, int width, int height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.rotations = rotations;
        this.width = width;
        this.height = height;
    }

    public static OfficePositionOrientation getByRotations(final int rotations) {
        for (OfficePositionOrientation orientation : OfficePositionOrientation.values()) {
            if (rotations % 4 == orientation.rotations) {
                return orientation;
            }
        }
        return null;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public Size getSize() {
        return new Size(width, height);
    }
}
