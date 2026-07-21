package auth.service.xflow_auth_service.services;

import io.jsonwebtoken.*;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.config.RsaKeyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RsaKeyConfig rsaKeyConfig;

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("id", user.getId().toString())
            .claim("role", user.getRole().name())
            .claim("email", user.getEmail())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(rsaKeyConfig.expirationMs())))
            .signWith(rsaKeyConfig.privateKey())
            .compact();
    }

    public String generateAnonymousToken(String anonymousId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(anonymousId)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(rsaKeyConfig.expirationMs())))
            .signWith(rsaKeyConfig.privateKey())
            .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(rsaKeyConfig.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            // Token has expired
            throw e;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            // Token format is invalid
            throw new JwtException("Malformed JWT token: " + e.getMessage(), e);
        } catch (io.jsonwebtoken.SignatureException e) {
            // Token signature is invalid
            throw new JwtException("Invalid JWT signature: " + e.getMessage(), e);
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            // Token is not supported
            throw new JwtException("Unsupported JWT token: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // Token is empty or null
            throw new JwtException("Empty JWT token: " + e.getMessage(), e);
        } catch (JwtException e) {
            // Generic JWT exception
            throw e;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
