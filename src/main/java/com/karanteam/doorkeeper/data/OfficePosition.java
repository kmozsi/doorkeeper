package com.karanteam.doorkeeper.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfficePosition {

    private int x;
    private int y;
    private OfficePositionOrientation orientation;

}
