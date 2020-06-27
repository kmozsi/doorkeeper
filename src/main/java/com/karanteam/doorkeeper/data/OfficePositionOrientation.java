package com.karanteam.doorkeeper.data;

public enum OfficePositionOrientation {
    NORTH(9, 4, 0),
    EAST(16, 9, 1),
    SOUTH(10, 16, 2),
    WEST(4, 10, 3);

    final int centerX;
    final int centerY;
    final int rotations;

    OfficePositionOrientation(int centerX, int centerY, int rotations) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.rotations = rotations;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public static OfficePositionOrientation getByRotations(final int rotations) {
        for (OfficePositionOrientation orientation : OfficePositionOrientation.values()) {
            if (rotations % 4 == orientation.rotations) {
                return orientation;
            }
        }
        return null;
    }
}
