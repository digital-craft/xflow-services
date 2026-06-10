package auth.service.xflow_auth_service.utils.security;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.UUID;

@Component
public class SecurityUtils {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRandomPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    public String generateRandomPin() {
        int num = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(num);
    }
    
    public void sendCredentialsEmail(String email, String password, String pin) {
        System.out.println("----------------------------------------------------------------");
        System.out.println("SENDING EMAIL (MOCK) to : " + email);
        System.out.println("Your temporary password : " + password);
        System.out.println("Your quick login PIN : " + pin);
        System.out.println("----------------------------------------------------------------");
    }
}