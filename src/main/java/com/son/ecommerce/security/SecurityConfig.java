package com.son.ecommerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Use stateful sessions for web (form login), stateless only for pure API callers
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth
                // ── Public static resources ──────────────────────────────
                .requestMatchers(
                    "/", "/products", "/products/**",
                    "/login", "/register",
                    "/forgot-password", "/reset-password",
                    "/css/**", "/js/**", "/img/**", "/images/**",
                    "/assets/**", "/static/**", "/fonts/**",
                    "/sass/**", "/uploads/**",
                    "/favicon.ico"
                ).permitAll()

                // ── Public API auth endpoints ────────────────────────────
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/product/**",
                    "/api/chat/**",
                    "/ws/**"
                ).permitAll()

                // ── Admin area — must have ROLE_ADMIN or ROLE_MANAGER ─────────
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER")

                // ── Authenticated-only endpoints ─────────────────────────
                .requestMatchers("/api/cart/**", "/cart/**", "/checkout/**",
                                 "/orders/**", "/profile/**").authenticated()

                // ── Everything else requires login ───────────────────────
                .anyRequest().authenticated()
            )

            // JWT filter runs before username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Form login — smart redirect: admin → /admin/dashboard, others → /
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(new SmartLoginSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )

            // Redirect to /login (not /api/...) when accessing secured URLs
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"success\":false, \"message\":\"Yêu cầu đăng nhập (authenticated). Vui lòng đăng nhập để tiếp tục.\"}");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect(request.getContextPath() + "/login?denied");
                })
            );

        return http.build();
    }

    /**
     * Smart success handler: redirect admins to dashboard, others to homepage.
     */
    static class SmartLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication)
                throws IOException, jakarta.servlet.ServletException {
            
            // Xóa bộ nhớ cache request (nếu có lưu URL /api/ lỗi) khỏi Session
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            }
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_MANAGER"));

            if (isAdmin) {
                getRedirectStrategy().sendRedirect(request, response, "/admin/dashboard");
            } else {
                getRedirectStrategy().sendRedirect(request, response, "/");
            }
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
