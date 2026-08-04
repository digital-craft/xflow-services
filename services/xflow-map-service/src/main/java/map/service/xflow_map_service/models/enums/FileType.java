package map.service.xflow_map_service.models.enums;

import java.util.Arrays;
import java.util.Optional;

public enum FileType {
    PNG("image/png"),
    JPG("image/jpeg"),
    PDF("application/pdf");

    private final String mimeType;

    FileType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static Optional<FileType> fromMimeType(String mimeType) {
        if (mimeType == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(type -> type.mimeType.equalsIgnoreCase(mimeType.trim()))
                .findFirst();
    }
}
