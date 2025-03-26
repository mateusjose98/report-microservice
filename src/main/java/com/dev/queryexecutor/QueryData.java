package com.dev.queryexecutor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QueryData {
    public final String sql;
    public final List<String> paramsOrder;

    public QueryData(String sql, String params) {
        this.sql = sql;
        this.paramsOrder = Arrays.stream(params.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
