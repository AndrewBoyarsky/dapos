package com.boyarsky.dapos.core.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    @PositiveOrZero
    private int page = 0;
    @Positive
    @Max(100)
    private int limit = 15;
}
