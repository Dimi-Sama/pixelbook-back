package com.futuretech.pixelbook.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futuretech.pixelbook.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                email = jwtUtil.getEmailFromToken(jwt);
                
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                
                chain.doFilter(request, response);
            } catch (ExpiredJwtException e) {
                // Token expiré
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token expiré");
                error.put("message", "Votre session a expiré, veuillez vous reconnecter");
                
                objectMapper.writeValue(response.getWriter(), error);
            } catch (Exception e) {
                // Autre erreur de token
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token invalide");
                error.put("message", "Token d'authentification invalide");
                
                objectMapper.writeValue(response.getWriter(), error);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/users/login") || 
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/v3/api-docs");
    }
} 