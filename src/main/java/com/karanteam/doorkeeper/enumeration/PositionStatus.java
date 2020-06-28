package com.karanteam.doorkeeper.enumeration;

public enum PositionStatus {
    FREE(Color.RED), BOOKED(Color.YELLOW), TAKEN(Color.RED);

    private final Color color;

    PositionStatus(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
