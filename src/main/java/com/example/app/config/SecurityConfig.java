package com.example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                    // 一般ユーザはGETのみ許可、編集・削除・作成は認証必須
                    .requestMatchers(HttpMethod.GET, "/api/screen/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/tag/list").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/upload").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/screen/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/screen/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/screen/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/tag").hasRole("ADMIN")
                    .requestMatchers("/app/**", "/scripts/**", "/views/**", "/").permitAll()
                    .anyRequest().permitAll()
                )
            .httpBasic(basic -> {})
            .formLogin(form -> form
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                // Allow GET /logout for anchor link compatibility
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/")
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 管理者/一般ユーザのインメモリユーザを設定
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        var user = User.withUsername("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }
}
