package com.boyarsky.dapos.core.tx;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProcessingResult {
    private String message;
    private GasData gasData = new GasData(0, 0);
    private ErrorCode code;
    private Transaction tx;
    private Exception e;

    public ProcessingResult(String message, ErrorCode code, Transaction tx, Exception e) {
        this.message = message;
        this.code = code;
        this.tx = tx;
        this.e = e;
    }
}
