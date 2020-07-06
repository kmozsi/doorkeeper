package com.karanteam.doorkeeper.data;

import java.awt.Dimension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Office {
    private Dimension dimension;
}
