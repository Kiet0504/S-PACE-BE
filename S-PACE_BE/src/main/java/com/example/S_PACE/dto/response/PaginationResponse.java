package com.example.S_PACE.dto.response;

public class PaginationResponse {
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    // Constructors
    public PaginationResponse() {}

    public PaginationResponse(int page, int limit, long total, int totalPages) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public String toString() {
        return "PaginationResponse{" +
                "page=" + page +
                ", limit=" + limit +
                ", total=" + total +
                ", totalPages=" + totalPages +
                '}';
    }
}

