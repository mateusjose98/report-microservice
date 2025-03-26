package com.dev.queryexecutor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
public class Result {
    private List<Map<String, Object>> records;
    private Long totalCount;
    private int page;
    private int size;
    private int totalPages;

    public Result(List<Map<String, Object>> records, Long totalCount, int page, int size) {
        this.records = records;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalCount / size);
    }

    public static Result empty() {
        return new Result(List.of(), 0L, 0, 0);
    }

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
