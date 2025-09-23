package com.recruitment.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho đăng nhập thành công
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;       // Access token
    private String refreshToken;      // Refresh token
    private String tokenType = "Bearer"; // Token type
    private Long expiresIn;           // milliseconds
    private Long refreshExpiresIn;    // milliseconds
    private UserResponse user;

    public static AuthResponse of(String accessToken,
                                  String refreshToken,
                                  long expiresIn,
                                  long refreshExpiresIn,
                                  UserResponse user) {
        AuthResponse response = new AuthResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.expiresIn = expiresIn;
        response.refreshExpiresIn = refreshExpiresIn;
        response.user = user;
        return response;
    }
}