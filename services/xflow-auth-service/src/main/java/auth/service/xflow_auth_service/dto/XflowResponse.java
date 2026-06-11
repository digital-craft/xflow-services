package auth.service.xflow_auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class XflowResponse<T> {
    public String message;
    public long timestamp;
    public T results;

    public XflowResponse(String message, T results) {
        this.message = message;
        this.results = results;
        this.timestamp = System.currentTimeMillis();
    }
}
