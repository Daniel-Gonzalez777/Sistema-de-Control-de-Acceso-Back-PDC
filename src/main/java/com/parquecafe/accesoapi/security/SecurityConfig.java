package com.parquecafe.accesoapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/error").permitAll()

                        // --- Las 4 pantallas de portería: público, sin login ---
                        .requestMatchers(HttpMethod.GET, "/api/ingreso/validar/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ingreso/historial").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ingreso/dentro").permitAll()
                        .requestMatchers("/api/visitas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/visitantes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/empleados-directos/**").permitAll()

                        // Lectura de empleados/concesionarios: también pública, porque
                        // las pantallas públicas de arriba las usan para sus selectores
                        // (a quién visita, filtro por empresa, etc). Crear/editar/eliminar
                        // sigue exigiendo login de Admin (se controla con @PreAuthorize).
                        .requestMatchers(HttpMethod.GET, "/api/empleados/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/concesionarios/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
