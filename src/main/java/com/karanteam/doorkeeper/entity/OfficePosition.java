package com.karanteam.doorkeeper.entity;

import com.karanteam.doorkeeper.data.OfficePositionOrientation;
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

}
