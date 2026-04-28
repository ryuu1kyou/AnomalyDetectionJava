package com.anomalydetection.web.blobstoring;

import com.anomalydetection.application.blobstoring.BlobStoringAppService;
import com.anomalydetection.contracts.blobstoring.BlobDto;
import java.util.List;
import java.util.UUID;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/app/blobs")
public class BlobController {

  private final BlobStoringAppService blobService;

  public BlobController(BlobStoringAppService blobService) {
    this.blobService = blobService;
  }

  @GetMapping
  public List<BlobDto> listByContainer(@RequestParam String container) {
    return blobService.listByContainer(container);
  }

  @PostMapping
  public ResponseEntity<BlobDto> upload(
      @RequestParam String container,
      @RequestParam MultipartFile file) throws Exception {
    String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
    BlobDto dto = blobService.store(container, file.getOriginalFilename(), mimeType, file.getBytes());
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> download(@PathVariable UUID id) {
    return blobService.retrieve(id)
        .map(content -> ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
            .contentType(Objects.requireNonNull(MediaType.APPLICATION_OCTET_STREAM))
            .body(content))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return blobService.delete(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
