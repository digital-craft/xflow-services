package auth.service.xflow_auth_service.config.filters;

import org.springframework.stereotype.Component;
import auth.service.xflow_auth_service.utils.events.RequestContextHolder;
import auth.service.xflow_auth_service.services.JwtService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ClientIpFilter implements Filter {
    
    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            } else {
                ip = ip.split(",")[0].trim();
            }
            RequestContextHolder.setClientIp(ip);

            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtService.extractUsername(token);
                try {
                    if (email != null) {
                        RequestContextHolder.setUserEmail(email);
                    }
                } catch (Exception e) {
                    RequestContextHolder.setUserEmail("anonymous-or-invalid");
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            RequestContextHolder.clear();
        }
    }
}