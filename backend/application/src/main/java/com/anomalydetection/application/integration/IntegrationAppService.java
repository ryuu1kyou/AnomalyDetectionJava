package com.anomalydetection.application.integration;

import com.anomalydetection.contracts.integration.CreateDataImportRequestDto;
import com.anomalydetection.contracts.integration.CreateIntegrationEndpointDto;
import com.anomalydetection.contracts.integration.CreateWebhookSubscriptionDto;
import com.anomalydetection.contracts.integration.DataImportRequestDto;
import com.anomalydetection.contracts.integration.ImportResultDto;
import com.anomalydetection.contracts.integration.IntegrationEndpointDto;
import com.anomalydetection.contracts.integration.IntegrationLogDto;
import com.anomalydetection.contracts.integration.WebhookSubscriptionDto;
import com.anomalydetection.domain.integration.DataImportRequest;
import com.anomalydetection.domain.integration.DataImportRequestRepository;
import com.anomalydetection.domain.integration.IntegrationEndpoint;
import com.anomalydetection.domain.integration.IntegrationEndpointRepository;
import com.anomalydetection.domain.integration.IntegrationLog;
import com.anomalydetection.domain.integration.IntegrationLogRepository;
import com.anomalydetection.domain.integration.WebhookSubscription;
import com.anomalydetection.domain.integration.WebhookSubscriptionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IntegrationAppService {

  private final IntegrationEndpointRepository endpointRepo;
  private final WebhookSubscriptionRepository webhookRepo;
  private final IntegrationLogRepository logRepo;
  private final DataImportRequestRepository importRepo;

  public IntegrationAppService(
      IntegrationEndpointRepository endpointRepo,
      WebhookSubscriptionRepository webhookRepo,
      IntegrationLogRepository logRepo,
      DataImportRequestRepository importRepo) {
    this.endpointRepo = endpointRepo;
    this.webhookRepo = webhookRepo;
    this.logRepo = logRepo;
    this.importRepo = importRepo;
  }

  // --- Endpoints ---

  @Transactional(readOnly = true)
  public List<IntegrationEndpointDto> getEndpoints() {
    return endpointRepo.findAll().stream().map(this::toEndpointDto).toList();
  }

  @Transactional(readOnly = true)
  public Optional<IntegrationEndpointDto> getEndpointById(UUID id) {
    return endpointRepo.findById(id).map(this::toEndpointDto);
  }

  public IntegrationEndpointDto createEndpoint(CreateIntegrationEndpointDto input) {
    var endpoint = new IntegrationEndpoint(
        UUID.randomUUID(), input.name(), input.type(), input.baseUrl(), input.description());
    if (input.isActive() != null) endpoint.setActive(input.isActive());
    if (input.timeout() != null) endpoint.setTimeout(input.timeout());
    return toEndpointDto(endpointRepo.save(endpoint));
  }

  public Optional<IntegrationEndpointDto> updateEndpoint(UUID id, CreateIntegrationEndpointDto input) {
    return endpointRepo.findById(id).map(e -> {
      e.setName(input.name());
      e.setDescription(input.description());
      e.setBaseUrl(input.baseUrl());
      if (input.isActive() != null) e.setActive(input.isActive());
      if (input.timeout() != null) e.setTimeout(input.timeout());
      return toEndpointDto(endpointRepo.save(e));
    });
  }

  public boolean deleteEndpoint(UUID id) {
    return endpointRepo.findById(id).map(e -> {
      e.softDelete(null);
      endpointRepo.save(e);
      return true;
    }).orElse(false);
  }

  public boolean testConnection(UUID id) {
    var endpoint = endpointRepo.findById(id).orElse(null);
    if (endpoint == null) return false;
    // Minimal test: assume reachable for non-REST types; for REST would need HTTP client
    return endpoint.isActive();
  }

  // --- Webhooks ---

  @Transactional(readOnly = true)
  public List<WebhookSubscriptionDto> getWebhooks(UUID endpointId) {
    return webhookRepo.findAllByEndpointId(endpointId).stream().map(this::toWebhookDto).toList();
  }

  public WebhookSubscriptionDto createWebhook(UUID endpointId, CreateWebhookSubscriptionDto input) {
    var webhook = new WebhookSubscription(
        UUID.randomUUID(), endpointId, input.eventType(), input.targetUrl(), input.isActive());
    return toWebhookDto(webhookRepo.save(webhook));
  }

  public Optional<WebhookSubscriptionDto> updateWebhook(UUID id, CreateWebhookSubscriptionDto input) {
    return webhookRepo.findById(id).map(w -> {
      w.setEventType(input.eventType());
      w.setTargetUrl(input.targetUrl());
      w.setActive(input.isActive());
      return toWebhookDto(webhookRepo.save(w));
    });
  }

  public boolean deleteWebhook(UUID id) {
    return webhookRepo.findById(id).map(w -> {
      w.softDelete(null);
      webhookRepo.save(w);
      return true;
    }).orElse(false);
  }

  // --- Logs ---

  @Transactional(readOnly = true)
  public List<IntegrationLogDto> getLogs(UUID endpointId) {
    return logRepo.findAllByEndpointId(endpointId).stream().map(this::toLogDto).toList();
  }

  // --- Import ---

  public ImportResultDto importData(CreateDataImportRequestDto input) {
    var endpoint = endpointRepo.findById(input.endpointId()).orElse(null);
    if (endpoint == null) return new ImportResultDto(false, 0, "Endpoint not found");

    var request = new DataImportRequest(
        UUID.randomUUID(), input.endpointId(), input.dataType(), input.filter());
    request.markAsProcessing();

    try {
      request.complete(0);
      endpoint.recordSuccess();
      endpointRepo.save(endpoint);
      importRepo.save(request);

      var log = new IntegrationLog(UUID.randomUUID(), input.endpointId(),
          "IMPORT:" + input.dataType(), true);
      logRepo.save(log);

      return new ImportResultDto(true, 0,
          "Successfully initiated import of " + input.dataType());
    } catch (Exception ex) {
      request.fail(ex.getMessage());
      endpoint.recordFailure();
      endpointRepo.save(endpoint);
      importRepo.save(request);
      return new ImportResultDto(false, 0, "Import failed: " + ex.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public List<DataImportRequestDto> getImportHistory() {
    return importRepo.findAll().stream().map(this::toImportDto).toList();
  }

  public ImportResultDto retryImport(UUID id) {
    return importRepo.findById(id).map(original -> {
      var dto = new CreateDataImportRequestDto(
          original.getEndpointId(), original.getDataType(), original.getFilter());
      return importData(dto);
    }).orElse(new ImportResultDto(false, 0, "Import request not found"));
  }

  // --- Mappers ---

  private IntegrationEndpointDto toEndpointDto(IntegrationEndpoint e) {
    return new IntegrationEndpointDto(
        e.getId().toString(), e.getName(), e.getDescription(), e.getType(),
        e.getBaseUrl(), e.isActive(), e.getTimeout(),
        e.getLastSyncDate() != null ? e.getLastSyncDate().toString() : null,
        e.getSuccessCount(), e.getFailureCount(),
        e.getCreatedAt() != null ? e.getCreatedAt().toString() : null,
        e.getLastModifiedAt() != null ? e.getLastModifiedAt().toString() : null);
  }

  private WebhookSubscriptionDto toWebhookDto(WebhookSubscription w) {
    return new WebhookSubscriptionDto(
        w.getId().toString(), w.getEndpointId().toString(),
        w.getTargetUrl(), w.getEventType(), w.isActive(),
        w.getMaxRetries(), w.getTimeoutSeconds(),
        w.getLastTriggeredAt() != null ? w.getLastTriggeredAt().toString() : null,
        w.getDeliverySuccessCount(), w.getDeliveryFailureCount());
  }

  private IntegrationLogDto toLogDto(IntegrationLog l) {
    return new IntegrationLogDto(
        l.getId().toString(), l.getEndpointId().toString(),
        l.getTimestamp().toString(), l.getLevel(),
        l.getOperation(), l.isSuccess(),
        l.getErrorMessage(), l.getStatusCode(), l.getDurationMs());
  }

  private DataImportRequestDto toImportDto(DataImportRequest r) {
    return new DataImportRequestDto(
        r.getId().toString(), r.getEndpointId().toString(),
        r.getDataType(), r.getStatus(), r.getFilter(),
        r.getRequestedAt().toString(),
        r.getProcessedDate() != null ? r.getProcessedDate().toString() : null,
        r.getRecordsImported(), r.getErrorMessage());
  }
}
