package com.anomalydetection.application.blobstoring;

import com.anomalydetection.contracts.blobstoring.BlobDto;
import com.anomalydetection.contracts.blobstoring.BlobStorageService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlobStoringAppService {

  private final BlobStorageService blobStorageService;

  public BlobStoringAppService(BlobStorageService blobStorageService) {
    this.blobStorageService = blobStorageService;
  }

  public BlobDto store(String containerName, String blobName, String mimeType, byte[] content) {
    return blobStorageService.store(containerName, blobName, mimeType, content);
  }

  @Transactional(readOnly = true)
  public Optional<byte[]> retrieve(UUID id) {
    return blobStorageService.retrieve(id);
  }

  @Transactional(readOnly = true)
  public List<BlobDto> listByContainer(String containerName) {
    return blobStorageService.listByContainer(containerName);
  }

  public boolean delete(UUID id) {
    return blobStorageService.delete(id);
  }
}
