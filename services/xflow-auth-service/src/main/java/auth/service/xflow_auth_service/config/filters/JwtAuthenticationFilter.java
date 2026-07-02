package auth.service.xflow_auth_service.config.filters;

import auth.service.xflow_auth_service.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String email;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            jwt = authHeader.substring(7);
            email = jwtService.extractUsername(jwt);
            
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    String role = jwtService.extractRole(jwt);
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            List.of(authority)
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "The provided JWT token has expired", "TOKEN_EXPIRED");
        } catch (JwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or malformed JWT token", "INVALID_TOKEN");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed", "AUTH_ERROR");
        }
    }

    private void sendError(HttpServletResponse response, int statusCode, String message, String code) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String statusText = statusCode == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : "Forbidden";
        String jsonResponse = String.format("""
            {
                "status": %d,
                "error": "%s",
                "message": "%s",
                "code": "%s"
            }
            """, statusCode, statusText, message, code);
        response.getWriter().write(jsonResponse);
    }
}