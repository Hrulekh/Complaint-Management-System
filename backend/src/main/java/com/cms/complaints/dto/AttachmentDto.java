package com.cms.complaints.dto;

import java.time.LocalDateTime;

public class AttachmentDto {
    private Long id;
    private String fileName;
    private String storedName;
    private String contentType;
    private Long sizeBytes;
    private UserDto uploadedBy;
    private LocalDateTime uploadedAt;

    public AttachmentDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public UserDto getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UserDto uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
