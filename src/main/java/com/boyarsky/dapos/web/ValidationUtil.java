package com.boyarsky.dapos.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ValidationUtil {
    public static String dumpException(Throwable e) throws IOException {
        if (e != null) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                 PrintStream ps = new PrintStream(os)) {
                e.printStackTrace(ps);
                return new String(os.toByteArray());
            }
        }
        return null;
    }
}
