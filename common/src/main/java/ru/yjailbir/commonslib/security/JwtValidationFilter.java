package ru.yjailbir.commonslib.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class JwtValidationFilter extends OncePerRequestFilter {
    private final RestTemplate restTemplate;

    public JwtValidationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            String authServiceUrl = "http://accounts-service/validate";
            ResponseEntity<String> validationResponse = restTemplate.postForEntity(
                    authServiceUrl,
                    Map.of("token", token),
                    String.class
            );

            if (validationResponse.getStatusCode().is2xxSuccessful()) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(validationResponse.getStatusCode().value());
                response.getWriter().write(validationResponse.getBody());
            }

        } catch (RestClientResponseException e) {
            response.setStatus(e.getStatusCode().value());
            response.getWriter().write(e.getResponseBodyAsString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Auth service unavailable");
        }
    }
}
