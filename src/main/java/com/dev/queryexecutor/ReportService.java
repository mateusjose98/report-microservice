package com.dev.queryexecutor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
class ReportService {

    private final DynamicSqlQueryLoader queryLoader;
    private final JdbcTemplate jdbcTemplate;

    public ReportService(DynamicSqlQueryLoader queryLoader, JdbcTemplate jdbcTemplate) {
        this.queryLoader = queryLoader;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Result execute(String reportName, Map<String, String> params, int page, int size) {
        params.putIfAbsent("limit", String.valueOf(size));
        params.putIfAbsent("offset", String.valueOf(page * size));

        final var selectData = queryLoader.getQueryData(reportName, QueryType.SELECT.name());
        if(selectData.isEmpty()) {
            return Result.empty();
        }

        final var countData = queryLoader.getQueryData(reportName, QueryType.COUNT.name());

        List<Object> selectParams = buildDynamicParams(selectData.get().paramsOrder, params);
        Long totalCount = null;
        if(countData.isPresent()) {
            List<Object> countParams = buildDynamicParams(countData.get().paramsOrder, params);
            totalCount = jdbcTemplate.queryForObject(countData.get().sql, Long.class, countParams.toArray());
        }
        List<Map<String, Object>> records = jdbcTemplate.queryForList(selectData.get().sql, selectParams.toArray());
        return new Result(records, totalCount, page, size);
    }

    private List<Object> buildDynamicParams(List<String> paramsOrder, Map<String, String> paramsMap) {
        List<Object> paramsList = new ArrayList<>();
        for (String paramName : paramsOrder) {
            String paramValue = paramsMap.get(paramName);
            if (paramValue == null) {
                throw new IllegalArgumentException("Faltando parâmetro: " + paramName);
            }
            paramsList.add(sanitize(paramValue));
        }
        return paramsList;
    }

    private Object sanitize(String paramValue) {
        if (paramValue.matches("^-?\\d+$")) { // é inteiro?
            return Integer.parseInt(paramValue);
        } else if (paramValue.matches("^-?\\d*\\.\\d+$")) { // decimal?
            return Double.parseDouble(paramValue);
        }
        return paramValue; // por padrão string (se precisar pode ampliar essa validação)
    }


}
