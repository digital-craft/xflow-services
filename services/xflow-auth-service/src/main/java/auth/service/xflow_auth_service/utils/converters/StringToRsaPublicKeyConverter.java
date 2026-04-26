package auth.service.xflow_auth_service.config.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@ConfigurationPropertiesBinding
public class StringToRsaPublicKeyConverter implements Converter<String, RSAPublicKey> {

    @Override
    public RSAPublicKey convert(String source) {
        try {
            byte[] decoded = Base64.getDecoder().decode(source.trim());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalArgumentException("Critical error: Unable to decode RSA public key (Expected format: X509 DER Base64)", e);
        }
    }
}