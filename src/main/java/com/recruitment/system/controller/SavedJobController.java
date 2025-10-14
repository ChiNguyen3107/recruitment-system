package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.SavedJobActionResponse;
import com.recruitment.system.service.SavedJobService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class SavedJobController {
    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}/save")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Lưu tin tuyển dụng vào danh sách việc làm đã lưu của ứng viên")
    public ResponseEntity<ApiResponse<SavedJobActionResponse>> saveJob(
            @PathVariable Long jobId,
            Authentication authentication
    ) {
        try{
            ApiResponse<SavedJobActionResponse> res =
                    savedJobService.saveJob(authentication.getName(), jobId);

            // Nếu duplicate: service trả success=false -> 409
            if(!res.isSuccess()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
        catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }

    }

    @DeleteMapping("/{jobId}/unsave")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Bỏ lưu tin tuyển dụng khỏi danh sách việc làm đã lưu của ứng viên")
    public ResponseEntity<ApiResponse<SavedJobActionResponse>> unsaveJob(
            @PathVariable Long jobId,
            Authentication authentication
    ) {
        try {
            ApiResponse<SavedJobActionResponse> res =
                    savedJobService.unsaveJob(authentication.getName(), jobId);
            return ResponseEntity.ok(res);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/saved")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Lấy danh sách tin tuyển dụng đã lưu của ứng viên")
    public ResponseEntity<ApiResponse<Page<JobPostingResponse>>> getSavedJobs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            ApiResponse<Page<JobPostingResponse>> res =
                    savedJobService.getSavedJobs(authentication.getName(), page, size);
            return ResponseEntity.ok(res);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

}