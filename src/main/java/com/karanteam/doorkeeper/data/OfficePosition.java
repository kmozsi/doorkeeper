package com.karanteam.doorkeeper.data;

import lombok.Builder;
import lombok.Data;

@Data
public class OfficePosition {

    private int x;
    private int y;
    private OfficePositionOrientation orientation;

}
