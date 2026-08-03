package map.service.xflow_map_service.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import map.service.xflow_map_service.utils.storage.IFileStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements IFileStorage {

    private final Path basePath;

    public LocalStorageService(@Value("${local.storage.path:./uploads/}") String basePathStr) {
        this.basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
        
        File dir = this.basePath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public String uploadFile(String fileName, byte[] content) {
        Path targetPath = this.basePath.resolve(fileName).normalize();
        try {
            Files.write(targetPath, content);
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file: " + fileName, e);
        }
    }

    @Override
    public byte[] downloadFile(String fileName) {
        Path filePath = this.basePath.resolve(fileName).normalize();
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error downloading file: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        Path filePath = this.basePath.resolve(fileName).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file: " + fileName, e);
        }
    }
}