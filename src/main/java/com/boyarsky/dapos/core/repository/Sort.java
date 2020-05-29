package com.boyarsky.dapos.core.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Sort {
    List<SortColumn> columns = new ArrayList<>();

    public static Sort defaultSort() {
        Sort sort = new Sort();
        sort.columns.add(new SortColumn("height", false));
        return sort;
    }

    public Sort add(String column, boolean asc) {
        columns.add(new SortColumn(column, asc));
        return this;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SortColumn {
        private String column;
        private boolean asc;
    }
}
