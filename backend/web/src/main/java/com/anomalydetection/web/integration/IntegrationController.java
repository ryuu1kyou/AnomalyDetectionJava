package com.anomalydetection.web.integration;

import com.anomalydetection.application.integration.IntegrationAppService;
import com.anomalydetection.contracts.integration.CreateDataImportRequestDto;
import com.anomalydetection.contracts.integration.CreateIntegrationEndpointDto;
import com.anomalydetection.contracts.integration.CreateWebhookSubscriptionDto;
import com.anomalydetection.contracts.integration.DataImportRequestDto;
import com.anomalydetection.contracts.integration.ImportResultDto;
import com.anomalydetection.contracts.integration.IntegrationEndpointDto;
import com.anomalydetection.contracts.integration.IntegrationLogDto;
import com.anomalydetection.contracts.integration.WebhookSubscriptionDto;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/integration")
public class IntegrationController {

  private final IntegrationAppService appService;

  public IntegrationController(IntegrationAppService appService) {
    this.appService = appService;
  }

  // --- Endpoints ---

  @GetMapping("/endpoints")
  public List<IntegrationEndpointDto> getEndpoints() {
    return appService.getEndpoints();
  }

  @GetMapping("/endpoints/{id}")
  public ResponseEntity<IntegrationEndpointDto> getEndpoint(@PathVariable UUID id) {
    return appService.getEndpointById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/endpoints")
  public IntegrationEndpointDto createEndpoint(@RequestBody CreateIntegrationEndpointDto input) {
    return appService.createEndpoint(input);
  }

  @PutMapping("/endpoints/{id}")
  public ResponseEntity<IntegrationEndpointDto> updateEndpoint(
      @PathVariable UUID id, @RequestBody CreateIntegrationEndpointDto input) {
    return appService.updateEndpoint(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/endpoints/{id}")
  public ResponseEntity<Void> deleteEndpoint(@PathVariable UUID id) {
    return appService.deleteEndpoint(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  @PostMapping("/endpoints/{id}/test")
  public ResponseEntity<Boolean> testConnection(@PathVariable UUID id) {
    return ResponseEntity.ok(appService.testConnection(id));
  }

  // --- Webhooks ---

  @GetMapping("/endpoints/{endpointId}/webhooks")
  public List<WebhookSubscriptionDto> getWebhooks(@PathVariable UUID endpointId) {
    return appService.getWebhooks(endpointId);
  }

  @PostMapping("/endpoints/{endpointId}/webhooks")
  public WebhookSubscriptionDto createWebhook(
      @PathVariable UUID endpointId, @RequestBody CreateWebhookSubscriptionDto input) {
    return appService.createWebhook(endpointId, input);
  }

  @PutMapping("/webhooks/{id}")
  public ResponseEntity<WebhookSubscriptionDto> updateWebhook(
      @PathVariable UUID id, @RequestBody CreateWebhookSubscriptionDto input) {
    return appService.updateWebhook(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/webhooks/{id}")
  public ResponseEntity<Void> deleteWebhook(@PathVariable UUID id) {
    return appService.deleteWebhook(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }

  // --- Logs ---

  @GetMapping("/endpoints/{endpointId}/logs")
  public List<IntegrationLogDto> getLogs(@PathVariable UUID endpointId) {
    return appService.getLogs(endpointId);
  }

  // --- Import ---

  @PostMapping("/import")
  public ImportResultDto importData(@RequestBody CreateDataImportRequestDto input) {
    return appService.importData(input);
  }

  @GetMapping("/import/history")
  public List<DataImportRequestDto> getImportHistory() {
    return appService.getImportHistory();
  }

  @PostMapping("/import/{id}/retry")
  public ImportResultDto retryImport(@PathVariable UUID id) {
    return appService.retryImport(id);
  }
}
