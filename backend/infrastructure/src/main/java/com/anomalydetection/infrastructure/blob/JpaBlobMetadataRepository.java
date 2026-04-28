package com.anomalydetection.infrastructure.blob;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBlobMetadataRepository extends JpaRepository<BlobMetadataEntity, UUID> {

  @Query("SELECT b.id, b.containerName, b.blobName, b.mimeType, b.sizeBytes, b.createdAt FROM BlobMetadataEntity b WHERE b.containerName = :containerName")
  List<Object[]> findMetadataByContainerName(String containerName);

  List<BlobMetadataEntity> findByContainerName(String containerName);
}
