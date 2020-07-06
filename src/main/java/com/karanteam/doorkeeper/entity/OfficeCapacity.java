package com.karanteam.doorkeeper.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OfficeCapacity {

    @Id
    @GeneratedValue
    private int id;
    private int capacity;
    private int allowedPercentage;
    private int minimalDistance;
    private int maxMapCapacity;

    public void setMinimalDistance(final int minimalDistance) {
        if (minimalDistance < 0) {
            throw new IllegalArgumentException("Cannot accept negative value as minimal distance!");
        }
        this.minimalDistance = minimalDistance;
    }

    public void setCapacity(final int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Cannot accept negative value as capacity!");
        }
        this.capacity = capacity;
    }

    public void setAllowedPercentage(final int allowedPercentage) {
        if (allowedPercentage < 0 || allowedPercentage > 100) {
            throw new IllegalArgumentException("Allowed percentage must be between 1 and 100!");
        }
        this.allowedPercentage = allowedPercentage;
    }

    public Integer getDailyCapacity() {
        return capacity * allowedPercentage / 100;
    }
}
