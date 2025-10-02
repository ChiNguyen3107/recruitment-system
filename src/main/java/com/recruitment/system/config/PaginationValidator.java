package com.recruitment.system.config;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class PaginationValidator {

    public static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_SORT_DIR = Set.of("ASC", "DESC");

    public static Pageable buildPageable(int page, int size, String sortBy, String sortDir, Set<String> allowedSortBy) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        if (allowedSortBy == null || allowedSortBy.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sortBy hợp lệ không được rỗng");
        }

        String safeSortBy = allowedSortBy.contains(sortBy) ? sortBy : allowedSortBy.iterator().next();
        String safeSortDir = (sortDir != null && ALLOWED_SORT_DIR.contains(sortDir.toUpperCase())) ? sortDir.toUpperCase() : "DESC";

        Sort sort = safeSortDir.equals("ASC") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        return PageRequest.of(safePage, safeSize, sort);
    }
}


