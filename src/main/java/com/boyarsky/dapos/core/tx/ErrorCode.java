package com.boyarsky.dapos.core.tx;

import lombok.Data;

@Data
public class ErrorCode {
    private final String codeSpace;
    private final int code;

    public ErrorCode(String codeSpace, int code) {
        this.codeSpace = codeSpace;
        this.code = code;
    }

    public ErrorCode(int code) {
        this("NO_SCOPE", code);
    }

    public boolean isOk() {
        return code == 0;
    }
}
