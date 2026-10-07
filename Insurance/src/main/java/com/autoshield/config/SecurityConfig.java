package com.autoshield.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // "/error" is included so that if a page fails to render for any
                        // reason, Spring shows its normal error page instead of the
                        // request being treated as unauthenticated and bounced to /login.
                        .requestMatchers("/", "/login", "/register", "/forgot-password",
                                "/reset-password", "/error", "/css/**", "/js/**", "/images/**", "/webfonts/**").permitAll()
                        // Claims review is shared by Insurance Officers and Admins, so this
                        // more specific rule must come BEFORE the general /admin/** rule below.
                        .requestMatchers("/admin/claims", "/admin/claims/**").hasAnyRole("OFFICER", "ADMIN")
                        .requestMatchers("/officer/**").hasAnyRole("OFFICER", "ADMIN")
                        // Vehicle Surveyor's own narrow area (Epic 3, US-06).
                        .requestMatchers("/surveyor/**").hasAnyRole("SURVEYOR", "ADMIN")
                        // Finance Officer's payment verification area (Epic 4, US-04/US-05).
                        .requestMatchers("/finance/**").hasAnyRole("FINANCE", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // Sends each role to its own landing page instead of dumping
                        // everyone on the customer "buy insurance" page.
                        .successHandler(roleBasedSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                    (request, response, authenticationException) ->
                        response.sendRedirect(request.getContextPath() + "/login?required=true")))
                // Spring Security 6.2+ defers loading the CSRF token until something
                // actually reads it (a BREACH-attack protection). On large Thymeleaf
                // pages like ours, Tomcat can start streaming the response before
                // Thymeleaf reaches the <form> tag that needs the token, so the
                // session can no longer be created ("response already committed").
                // Forcing eager resolution (below) avoids that race entirely.
                .csrf(csrf -> csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }

    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/packages"; // default: ROLE_USER (customer)
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin/dashboard";
                    break;
                } else if (authority.getAuthority().equals("ROLE_OFFICER")) {
                    redirectUrl = "/officer/dashboard";
                    break;
                } else if (authority.getAuthority().equals("ROLE_SURVEYOR")) {
                    redirectUrl = "/surveyor/dashboard";
                    break;
                } else if (authority.getAuthority().equals("ROLE_FINANCE")) {
                    redirectUrl = "/finance/dashboard";
                    break;
                }
            }
            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }

    /**
     * Reads the CSRF token attribute on every request, which forces Spring
     * Security to resolve/create it immediately (and create the HTTP session
     * if needed) before any part of the response has been written.
     */
    private static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}