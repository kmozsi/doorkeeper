package com.karanteam.doorkeeper.data;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.awt.*;

@Data
@Builder
@AllArgsConstructor
public class Office {
    private Dimension dimension;
}
