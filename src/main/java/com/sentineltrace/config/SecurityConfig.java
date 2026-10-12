package com.sentineltrace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin#Sentinel2026"))
                .roles("ADMIN", "ANALYST")
                .build();

        UserDetails analyst = User.builder()
                .username("analyst")
                .password(passwordEncoder.encode("Analyst#Sentinel2026"))
                .roles("ANALYST")
                .build();

        return new InMemoryUserDetailsManager(admin, analyst);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF only for stateless API endpoints; leave enabled for browser forms
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Protected management routes require authentication and ADMIN role
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                // Publicly accessible dashboard, APIs, and static assets for demo operations
                .requestMatchers(
                    "/",
                    "/dashboard",
                    "/events",
                    "/incidents",
                    "/incidents/**",
                    "/rules",
                    "/demo/**",
                    "/api/events",
                    "/api/events/**",
                    "/api/incidents",
                    "/api/incidents/**",
                    "/api/dashboard/**",
                    "/api/demo/**",
                    "/api/rules",
                    "/api/rules/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
            .logout(logout -> logout
                .logoutSuccessUrl("/dashboard")
                .permitAll()
            );

        return http.build();
    }
}
