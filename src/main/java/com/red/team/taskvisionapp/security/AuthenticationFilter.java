package com.red.team.taskvisionapp.security;

import com.red.team.taskvisionapp.model.dto.request.JwtClaims;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.service.JwtService;
import com.red.team.taskvisionapp.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && jwtService.verifyJwtToken(bearerToken)) {
            JwtClaims jwtClaims = jwtService.getJwtClaims(bearerToken);
            String email = jwtClaims.getEmail();
            User userAccount = userService.getUserByEmail(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userAccount.getEmail(),
                    null,
                    userAccount.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
