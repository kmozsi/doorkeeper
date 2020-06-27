package com.karanteam.doorkeeper.entity;

import com.karanteam.doorkeeper.data.OfficePositionOrientation;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficePosition {

    @Id
    @GeneratedValue
    private Integer id;
    private int x;
    private int y;
    @NonNull
    private OfficePositionOrientation orientation;
    private PositionStatus status;

    public int getCenterX() {
        return x + orientation.getCenterX();
    }

    public int getCenterY() {
        return y + orientation.getCenterY();
    }

    public double distanceFrom(OfficePosition other) {
        double ac = Math.abs(y - other.y);
        double cb = Math.abs(x - other.x);
        return Math.hypot(ac, cb);
    }

}
