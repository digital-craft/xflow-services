package auth.service.xflow_auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    public String message;
    public long timestamp;
    public T results;

    public ApiResponse(String message, T results) {
        this.message = message;
        this.results = results;
        this.timestamp = System.currentTimeMillis();
    }
}
