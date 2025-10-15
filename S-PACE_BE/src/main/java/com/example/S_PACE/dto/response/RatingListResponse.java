package com.example.S_PACE.dto.response;

import java.util.List;

public class RatingListResponse {
    private List<RatingResponse> ratings;
    private PaginationResponse pagination;

    // Constructors
    public RatingListResponse() {}

    public RatingListResponse(List<RatingResponse> ratings, PaginationResponse pagination) {
        this.ratings = ratings;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<RatingResponse> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingResponse> ratings) {
        this.ratings = ratings;
    }

    public PaginationResponse getPagination() {
        return pagination;
    }

    public void setPagination(PaginationResponse pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        return "RatingListResponse{" +
                "ratings=" + ratings +
                ", pagination=" + pagination +
                '}';
    }
}

