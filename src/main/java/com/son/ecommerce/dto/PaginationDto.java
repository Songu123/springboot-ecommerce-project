package com.son.ecommerce.dto;

import java.util.List;

/**
 * DTO for pagination information
 * Used to standardize pagination across all admin controllers
 */
public class PaginationDto<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private int totalItems;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private int startIndex;
    private int endIndex;

    public PaginationDto(List<T> allItems, int currentPage, int pageSize) {
        this.pageSize = pageSize;
        this.totalItems = allItems.size();
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Validate page
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages && totalPages > 0) currentPage = totalPages;
        this.currentPage = currentPage;

        // Calculate start and end indices
        this.startIndex = (currentPage - 1) * pageSize;
        this.endIndex = Math.min(startIndex + pageSize, totalItems);

        // Get paginated content
        if (startIndex >= totalItems) {
            this.content = List.of();
        } else {
            this.content = allItems.subList(startIndex, endIndex);
        }

        this.hasNext = currentPage < totalPages;
        this.hasPrevious = currentPage > 1;
    }

    // Getters
    public List<T> getContent() {
        return content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    // For display
    public int getDisplayStartIndex() {
        return totalItems == 0 ? 0 : startIndex + 1;
    }

    public int getDisplayEndIndex() {
        return endIndex;
    }
}

