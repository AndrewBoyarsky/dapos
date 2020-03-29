package com.boyarsky.dapos.core;

import java.time.LocalDateTime;

public interface TimeSource {
    /**
     * @return Unix epoch time in millis
     */
    long getTime();
}
