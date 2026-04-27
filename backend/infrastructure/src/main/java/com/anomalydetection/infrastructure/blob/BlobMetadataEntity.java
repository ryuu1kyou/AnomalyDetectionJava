package com.anomalydetection.infrastructure.blob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blobs")
public class BlobMetadataEntity {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "container_name", nullable = false, length = 256)
  private String containerName;

  @Column(name = "blob_name", nullable = false, length = 1024)
  private String blobName;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "mime_type", length = 256)
  private String mimeType;

  @Column(name = "size_bytes")
  private Long sizeBytes;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected BlobMetadataEntity() {}

  public BlobMetadataEntity(String containerName, String blobName, UUID tenantId,
      String mimeType, byte[] content) {
    this.id = UUID.randomUUID();
    this.containerName = containerName;
    this.blobName = blobName;
    this.tenantId = tenantId;
    this.mimeType = mimeType;
    this.content = content;
    this.sizeBytes = content != null ? (long) content.length : 0L;
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getContainerName() { return containerName; }
  public String getBlobName() { return blobName; }
  public UUID getTenantId() { return tenantId; }
  public String getMimeType() { return mimeType; }
  public Long getSizeBytes() { return sizeBytes; }
  public byte[] getContent() { return content; }
  public Instant getCreatedAt() { return createdAt; }
}