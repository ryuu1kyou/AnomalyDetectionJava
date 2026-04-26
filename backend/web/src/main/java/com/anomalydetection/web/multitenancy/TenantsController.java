package com.anomalydetection.web.multitenancy;

import com.anomalydetection.application.multitenancy.TenantAppService;
import com.anomalydetection.contracts.multitenancy.CreateTenantDto;
import com.anomalydetection.contracts.multitenancy.GetTenantsInputDto;
import com.anomalydetection.contracts.multitenancy.TenantDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/tenants")
public class TenantsController {

  private final TenantAppService appService;

  public TenantsController(TenantAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public PagedResultDto<TenantDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount) {
    return appService.getList(new GetTenantsInputDto(filter, skipCount, maxResultCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TenantDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public TenantDto create(@RequestBody CreateTenantDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}/activate")
  public ResponseEntity<TenantDto> activate(@PathVariable UUID id) {
    return appService.setActive(id, true).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}/deactivate")
  public ResponseEntity<TenantDto> deactivate(@PathVariable UUID id) {
    return appService.setActive(id, false).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
