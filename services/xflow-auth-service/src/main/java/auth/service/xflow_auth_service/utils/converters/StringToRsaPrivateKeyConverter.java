package auth.service.xflow_auth_service.config.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
@ConfigurationPropertiesBinding
public class StringToRsaPrivateKeyConverter implements Converter<String, RSAPrivateKey> {

    @Override
    public RSAPrivateKey convert(String source) {
        try {
            byte[] decoded = Base64.getDecoder().decode(source.trim());
            
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalArgumentException("Critical error: Unable to decode RSA private key (Expected format: PKCS8 DER Base64)", e);
        }
    }
}