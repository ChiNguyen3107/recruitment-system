package com.recruitment.system.controller;


import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.NotificationResponse;
import com.recruitment.system.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách thông báo")
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(notificationService.list(userId, isRead, pageable));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu 1 thông báo là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id,
                                                      @AuthenticationPrincipal(expression = "id") Long userId) {
        notificationService.markRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }

    @PatchMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc")
    public ResponseEntity<ApiResponse<Object>> markAllRead(
            @AuthenticationPrincipal(expression = "id") Long userId){
        int updated = notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả đã đọc",
                java.util.Map.of("updatedCount", updated)));
    }

    @GetMapping("/count-unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đếm số lượng thông báo chưa đọc")
    public ResponseEntity<ApiResponse<Object>> countUnread(@AuthenticationPrincipal(expression = "id") Long userId) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.success("OK", java.util.Map.of("count", count)));
    }
}
