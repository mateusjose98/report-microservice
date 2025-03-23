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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
		int limit = size;
		int offset = page * size;

		Map<String, String> queryParams = new HashMap<>(params);
		queryParams.put("limit", String.valueOf(limit));
		queryParams.put("offset", String.valueOf(offset));

		String selectSql = queryLoader.getQuery(reportName, "select", queryParams);
		String countSql = queryLoader.getQuery(reportName, "count", params);

		List<Map<String, Object>> records = jdbcTemplate.queryForList(selectSql);
		Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class);

		return new Result(records, totalCount, page, size);
	}


}

@Component
class DynamicSqlQueryLoader  {

	private final JdbcTemplate jdbcTemplate;
	private final Map<String, String> queryCache = new ConcurrentHashMap<>();

	public DynamicSqlQueryLoader(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES) // Refresh hourly
	public void refreshQueries() {

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT report_name, query_type, query FROM report_management");
		queryCache.clear();
		rows.forEach(row -> {
			String key = row.get("report_name") + "_" + row.get("query_type");
			queryCache.put(key, (String) row.get("query"));
		});
	}
	public String getQuery(String reportName, String queryType, Map<String, String> params) {
		String key = reportName + "_" + queryType;
		String template = queryCache.get(key);
		if (template == null) {
			throw new IllegalArgumentException("Query not found: " + key);
		}
		return replaceVariables(template, params);
	}

	private String replaceVariables(String sql, Map<String, String> variables) {
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			sql = sql.replace("${" + entry.getKey() + "}$", entry.getValue());
		}
		return sql;
	}
}
