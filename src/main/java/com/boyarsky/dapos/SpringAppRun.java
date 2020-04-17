package com.boyarsky.dapos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class SpringAppRun {
    public static void main(String[] args) {
        SpringApplication.run(SpringAppRun.class);
    }
}
