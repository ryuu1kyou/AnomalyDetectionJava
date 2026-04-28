package com.anomalydetection.contracts.blobstoring;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlobStorageService {
  BlobDto store(String containerName, String blobName, String mimeType, byte[] content);
  Optional<byte[]> retrieve(UUID id);
  List<BlobDto> listByContainer(String containerName);
  boolean delete(UUID id);
}
