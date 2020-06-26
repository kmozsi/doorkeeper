package com.karanteam.doorkeeper.data;

import com.karanteam.doorkeeper.entity.OfficePosition;
import lombok.Data;

import java.util.List;

@Data
public class PositionConfiguration {

    private List<OfficePosition> positions;

}
