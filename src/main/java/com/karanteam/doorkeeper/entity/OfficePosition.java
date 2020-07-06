package com.karanteam.doorkeeper.entity;

import com.karanteam.doorkeeper.data.OfficePositionOrientation;
import com.karanteam.doorkeeper.enumeration.PositionStatus;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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

    private int getCenterX() {
        return x + orientation.getCenterX();
    }

    private int getCenterY() {
        return y + orientation.getCenterY();
    }

    public double distanceFrom(OfficePosition other) {
        double ac = Math.abs(getCenterY() - other.getCenterY());
        double cb = Math.abs(getCenterX() - other.getCenterX());
        return Math.hypot(ac, cb);
    }
}
