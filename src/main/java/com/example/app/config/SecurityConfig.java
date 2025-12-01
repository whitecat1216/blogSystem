package com.example.app.config;

import com.example.app.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

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
                    .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/screen/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/screen/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/screen/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/tag").hasRole("ADMIN")
                    .requestMatchers("/login.html", "/register.html", "/app/**", "/scripts/**", "/views/**", "/styles/**", "/", "/static/**").permitAll()
                    .anyRequest().permitAll()
                )
            // Basic認証は開発用。常時admin状態を避けるためフォームログインのみ利用。
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
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
        // 平文パスワード（本番環境では非推奨）
        return NoOpPasswordEncoder.getInstance();
    }

    // データベースからユーザー情報を取得する UserDetailsService
    // @Lazy を使用して循環依存を回避
    @Bean
    public UserDetailsService userDetailsService(@Lazy UserService userService) {
        return username -> {
            Map<String, Object> user = userService.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            
            String password = (String) user.get("password");
            String role = (String) user.get("role");
            Boolean enabled = (Boolean) user.get("enabled");
            
            return User.withUsername(username)
                    .password(password)
                    .roles(role)
                    .disabled(!enabled)
                    .build();
        };
    }
}