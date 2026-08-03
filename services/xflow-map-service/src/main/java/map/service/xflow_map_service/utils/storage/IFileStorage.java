package map.service.xflow_map_service.utils.storage;

public interface IFileStorage {
    String uploadFile(String fileName, byte[] content);
    byte[] downloadFile(String fileName);
    void deleteFile(String fileName);
}