package com.dev.queryexecutor;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
class DynamicSqlQueryLoader  {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, QueryData> queryCache = new ConcurrentHashMap<>();

    public DynamicSqlQueryLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES) // Refresh hourly
    public void refreshQueries() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT report_name, query_type, query, params FROM report_management");
        queryCache.clear();
        System.out.println("Refreshed queries: " + rows.size());
        rows.forEach(row -> {
            String key = row.get("report_name") + "_" + row.get("query_type");
            String query = (String) row.get("query");
            String params = (String) row.get("params");
            queryCache.put(key, new QueryData(query, params));
        });
    }
    public Optional<QueryData> getQueryData(String reportName, String queryType) {
        String key = reportName + "_" + queryType;
        return Optional.ofNullable(queryCache.get(key));
    }



}
