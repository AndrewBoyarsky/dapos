package com.boyarsky.dapos.core.repository;

import lombok.Data;

@Data
public class Pagination {
    private int page = 0;
    private int limit = 15;
}
