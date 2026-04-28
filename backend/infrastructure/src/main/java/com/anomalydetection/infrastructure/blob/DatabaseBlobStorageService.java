package com.anomalydetection.infrastructure.blob;

import com.anomalydetection.contracts.blobstoring.BlobDto;
import com.anomalydetection.contracts.blobstoring.BlobStorageService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatabaseBlobStorageService implements BlobStorageService {

  private final JpaBlobMetadataRepository repository;

  public DatabaseBlobStorageService(JpaBlobMetadataRepository repository) {
    this.repository = repository;
  }

  @Override
  public BlobDto store(String containerName, String blobName, String mimeType, byte[] content) {
    var entity = new BlobMetadataEntity(containerName, blobName, null, mimeType, content);
    entity = repository.save(entity);
    return toDto(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<byte[]> retrieve(UUID id) {
    UUID nonNullId = Objects.requireNonNull(id);
    return repository.findById(nonNullId).map(BlobMetadataEntity::getContent);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BlobDto> listByContainer(String containerName) {
    return repository.findByContainerName(containerName).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  public boolean delete(UUID id) {
    UUID nonNullId = Objects.requireNonNull(id);
    if (!repository.existsById(nonNullId)) return false;
    repository.deleteById(nonNullId);
    return true;
  }

  private BlobDto toDto(BlobMetadataEntity e) {
    return new BlobDto(e.getId(), e.getContainerName(), e.getBlobName(),
        e.getMimeType(), e.getSizeBytes(), e.getCreatedAt());
  }
}
