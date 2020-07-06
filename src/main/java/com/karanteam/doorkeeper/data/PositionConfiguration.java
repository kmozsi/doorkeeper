package com.karanteam.doorkeeper.data;

import com.karanteam.doorkeeper.entity.OfficePosition;
import java.util.List;
import lombok.Data;

@Data
public class PositionConfiguration {
    private List<OfficePosition> positions;
}
