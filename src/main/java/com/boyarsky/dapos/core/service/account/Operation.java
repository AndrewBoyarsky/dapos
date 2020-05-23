package com.boyarsky.dapos.core.service.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation {
    private long id;
    private long height;
    private String type;
    private long amount;
}
