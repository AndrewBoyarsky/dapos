package com.boyarsky.dapos.core.repository;

public class DbParamImpl implements DbParam {
    private String name;
    private Comparable c;

    public DbParamImpl(String name, Comparable c) {
        this.name = name;
        this.c = c;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Comparable value() {
        return c;
    }
}
