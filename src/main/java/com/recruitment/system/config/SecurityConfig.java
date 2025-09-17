package com.recruitment.system.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Cấu hình Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/debug/**").permitAll() // Debug endpoints
                .requestMatchers("/api/jobs/search/**").permitAll()
                .requestMatchers("/api/jobs/public/**").permitAll()
                .requestMatchers("/api/companies/public/**").permitAll()
                .requestMatchers("/api/profile/public/**").permitAll() // Public profile endpoints
                .requestMatchers(HttpMethod.GET, "/api/jobs/*").permitAll() // Get job details is public
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Employer/Recruiter endpoints - Specific endpoints first
                .requestMatchers("/api/jobs/my-jobs").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/jobs").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN") // Create jobs
                .requestMatchers(HttpMethod.PUT, "/api/jobs/*").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN") // Update jobs
                .requestMatchers(HttpMethod.DELETE, "/api/jobs/*").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN") // Delete jobs
                .requestMatchers("/api/employer/**").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN")
                .requestMatchers("/api/jobs/manage/**").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN")
                .requestMatchers("/api/applications/manage/**").hasAnyRole("EMPLOYER", "RECRUITER", "ADMIN")
                
                // Applicant endpoints
                .requestMatchers("/api/applicant/**").hasRole("APPLICANT")
                .requestMatchers("/api/applications/my/**").hasRole("APPLICANT")
                .requestMatchers("/api/profiles/my/**").hasRole("APPLICANT")
                
                // Any authenticated user
                .requestMatchers("/api/user/**").authenticated()
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}