package com.boyarsky.dapos.core.tx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GasData {
    private int used;
    private int wanted;
}
