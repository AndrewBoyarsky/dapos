package com.boyarsky.dapos.core;

import com.boyarsky.dapos.core.TimeSource;
import org.springframework.stereotype.Component;

@Component
public class TimeSourceImpl implements TimeSource {
    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }
}
