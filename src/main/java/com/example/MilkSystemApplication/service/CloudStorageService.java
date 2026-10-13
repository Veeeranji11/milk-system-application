package com.example.MilkSystemApplication.service;

public interface CloudStorageService {
    /**
     * Uploads document/PDF to cloud object storage.
     * @param directory directory or bucket prefix e.g. "invoices/tenant-xyz"
     * @param fileName file name e.g. "INV-202609-001.pdf"
     * @param content byte array of file
     * @param contentType MIME type e.g. "application/pdf"
     * @return accessible URL or storage reference
     */
    String uploadFile(String directory, String fileName, byte[] content, String contentType);

    /**
     * Retrieves file content from storage.
     */
    byte[] getFile(String path);

    /**
     * Deletes file from storage.
     */
    boolean deleteFile(String path);
}
