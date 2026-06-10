package auth.service.xflow_auth_service.utils.events;

public class RequestContextHolder {
    private static final ThreadLocal<String> clientIpHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userEmailHolder = new ThreadLocal<>();

    public static void setClientIp(String ip) {
        clientIpHolder.set(ip);
    }

    public static void setUserEmail(String email) {
        userEmailHolder.set(email);
    }

    public static String getClientIp() {
        return clientIpHolder.get();
    }

    public static String getUserEmail() {
        return userEmailHolder.get();
    }

    // Très important pour éviter les fuites de mémoire dans les pools de threads
    public static void clear() {
        clientIpHolder.remove();
        userEmailHolder.remove();
    }
}