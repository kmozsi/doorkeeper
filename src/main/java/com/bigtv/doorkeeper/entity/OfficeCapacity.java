package com.bigtv.doorkeeper.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OfficeCapacity {

    // TODO Maybe the daily capacity (capacity * allowedPercentage / 100 could be cached)
    @Id
    @GeneratedValue
    private int id;
    private int capacity;
    private int allowedPercentage;

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
