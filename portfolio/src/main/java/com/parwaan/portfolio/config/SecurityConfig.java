package com.parwaan.portfolio.config;

// import com.parwaan.portfolio.repository.UserRepository;
import com.parwaan.portfolio.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                    .requestMatchers("/api/auth/register").permitAll()
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/reset-password").permitAll()
                    .requestMatchers("/api/auth/forgot-password").permitAll()
                    .requestMatchers("/api/donations/**").permitAll()
                    .requestMatchers("/donations/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/projects").permitAll() // Anyone can see the list
                    .requestMatchers(HttpMethod.GET, "/api/projects/**").authenticated() // Must be logged in for details
                    .requestMatchers("/api/projects/**").hasAuthority("ADMIN") // All other methods (POST, PUT, DELETE) require ADMIN
                    .requestMatchers("/api/chat/**").permitAll() // TEMP: allow unauthenticated access for development
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
