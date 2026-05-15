package auth.service.xflow_auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "rsa")
public record RsaKeyConfig(
    RSAPublicKey publicKey,
    RSAPrivateKey privateKey,
    long expirationMs,
    long refreshExpirationMs
) {
}