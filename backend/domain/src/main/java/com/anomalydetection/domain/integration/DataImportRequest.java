package com.anomalydetection.domain.integration;

import com.anomalydetection.domain.base.FullAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "data_import_requests")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLRestriction("is_deleted = false")
public class DataImportRequest extends FullAuditedEntity<UUID> {

  @Id
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "endpoint_id", columnDefinition = "BINARY(16)", nullable = false)
  private UUID endpointId;

  @Column(name = "data_type", nullable = false, length = 100)
  private String dataType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ImportStatus status;

  @Column(length = 2000)
  private String filter;

  @Column(columnDefinition = "LONGTEXT")
  private String data;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "processed_date")
  private Instant processedDate;

  @Column(name = "records_imported", nullable = false)
  private int recordsImported;

  @Column(name = "error_message", columnDefinition = "LONGTEXT")
  private String errorMessage;

  protected DataImportRequest() {}

  public DataImportRequest(UUID id, UUID endpointId, String dataType, String filter) {
    this.id = id;
    this.endpointId = endpointId;
    this.dataType = dataType;
    this.filter = filter;
    this.requestedAt = Instant.now();
    this.status = ImportStatus.Pending;
    this.recordsImported = 0;
  }

  public void markAsProcessing() {
    this.status = ImportStatus.Processing;
  }

  public void complete(int recordCount) {
    this.status = ImportStatus.Completed;
    this.recordsImported = recordCount;
    this.processedDate = Instant.now();
  }

  public void fail(String errorMsg) {
    this.status = ImportStatus.Failed;
    this.errorMessage = errorMsg;
    this.processedDate = Instant.now();
  }

  @Override
  public UUID getId() { return id; }

  public UUID getTenantId() { return tenantId; }
  public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

  public UUID getEndpointId() { return endpointId; }
  public String getDataType() { return dataType; }
  public ImportStatus getStatus() { return status; }
  public String getFilter() { return filter; }
  public String getData() { return data; }
  public void setData(String data) { this.data = data; }
  public Instant getRequestedAt() { return requestedAt; }
  public Instant getProcessedDate() { return processedDate; }
  public int getRecordsImported() { return recordsImported; }
  public String getErrorMessage() { return errorMessage; }
}
