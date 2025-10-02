package com.recruitment.system.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response chung cho API
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {

    /**
     * Thành công hay không
     */
    private boolean success;

    /**
     * Thông điệp thân thiện cho người dùng
     */
    private String message;

    /**
     * Mã lỗi/mã trạng thái ứng dụng (ví dụ: VALIDATION_ERROR, UNAUTHORIZED, ...)
     */
    private String code;

    /**
     * Lý do ngắn gọn cho lập trình viên/khách hàng
     */
    private String reason;

    /**
     * Dữ liệu trả về khi thành công
     */
    private T data;

    /**
     * Thông tin lỗi chi tiết (ví dụ: map field -> message), không chứa stacktrace
     */
    private Object error;

    /**
     * Ngữ cảnh đi kèm (ví dụ: path, method, requestId)
     */
    private Object context;

    // ---------- Factory methods ----------

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage("Thành công");
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMessage(message);
        res.setData(data);
        return res;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setMessage(message);
        return res;
    }

    public static <T> ApiResponse<T> error(String message, Object error) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setMessage(message);
        res.setError(error);
        return res;
    }

    public static <T> ApiResponse<T> error(String code, String message, String reason, Object error, Object context) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setCode(code);
        res.setMessage(message);
        res.setReason(reason);
        res.setError(error);
        res.setContext(context);
        return res;
    }
}