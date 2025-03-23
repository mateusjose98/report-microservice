package com.dev.queryexecutor;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@RestController
@RequestMapping("/reports")
public class QueryexecutorApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryexecutorApplication.class, args);
	}

	@Autowired
	private ReportService reportService;

	@GetMapping(value = "/{reportName}", produces="application/json")
	public ResponseEntity<Result> executeReport(
			@PathVariable String reportName,
			@RequestParam Map<String, String> params,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(reportService.execute(reportName, params, page, size));
	}
}


@Service
class ReportService {

	private final DynamicSqlQueryLoader queryLoader;
	private final JdbcTemplate jdbcTemplate;

	public ReportService(DynamicSqlQueryLoader queryLoader, JdbcTemplate jdbcTemplate) {
		this.queryLoader = queryLoader;
		this.jdbcTemplate = jdbcTemplate;
	}

	public Result execute(String reportName, Map<String, String> params, int page, int size) {
		// Automaticamente adicionar paginação se disponível
		params.putIfAbsent("limit", String.valueOf(size));
		params.putIfAbsent("offset", String.valueOf(page * size));

		DynamicSqlQueryLoader.QueryData selectData = queryLoader.getQueryData(reportName, "select");
		DynamicSqlQueryLoader.QueryData countData = queryLoader.getQueryData(reportName, "count");

		List<Object> selectParams = buildDynamicParams(selectData.paramsOrder, params);
		List<Object> countParams = buildDynamicParams(countData.paramsOrder, params);

		List<Map<String, Object>> records = jdbcTemplate.queryForList(selectData.sql, selectParams.toArray());
		Long totalCount = jdbcTemplate.queryForObject(countData.sql, Long.class, countParams.toArray());

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
	public QueryData getQueryData(String reportName, String queryType) {
		String key = reportName + "_" + queryType;
		QueryData queryData = queryCache.get(key);
		if (queryData == null) {
			throw new IllegalArgumentException("Query não encontrada: " + key);
		}
		return queryData;
	}

	public static class QueryData {
		public final String sql;
		public final List<String> paramsOrder;

		public QueryData(String sql, String params) {
			this.sql = sql;
			this.paramsOrder = Arrays.stream(params.split(","))
					.map(String::trim)
					.collect(Collectors.toList());
		}
	}


}
