package com.anomalydetection.application.multitenancy;

import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.contracts.multitenancy.CreateTenantDto;
import com.anomalydetection.contracts.multitenancy.GetTenantsInputDto;
import com.anomalydetection.contracts.multitenancy.TenantDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.domain.multitenancy.Tenant;
import com.anomalydetection.domain.multitenancy.TenantRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantAppService {

  private final TenantRepository tenantRepository;

  public TenantAppService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + IdentityPermissions.TENANTS_VIEW + "')")
  public PagedResultDto<TenantDto> getList(GetTenantsInputDto input) {
    String filter = input != null && input.filter() != null ? input.filter().toLowerCase() : "";
    List<Tenant> all = tenantRepository.findAll();

    var filtered = all.stream()
        .filter(t -> filter.isBlank() || t.getName().toLowerCase().contains(filter))
        .toList();

    int skip = input != null && input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input != null && input.maxResultCount() != null ? Math.max(1, input.maxResultCount()) : 10;

    var page = filtered.stream().skip(skip).limit(take).map(this::toDto).toList();
    return PagedResultDto.of(page, filtered.size());
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + IdentityPermissions.TENANTS_VIEW + "')")
  public Optional<TenantDto> getById(UUID id) {
    return tenantRepository.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.TENANTS_CREATE + "')")
  public TenantDto create(CreateTenantDto input) {
    if (tenantRepository.existsByNormalizedName(input.name().toUpperCase())) {
      throw new IllegalArgumentException("Tenant already exists: " + input.name());
    }
    var tenant = new Tenant(UUID.randomUUID(), input.name());
    return toDto(tenantRepository.save(tenant));
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.TENANTS_EDIT + "')")
  public Optional<TenantDto> setActive(UUID id, boolean active) {
    return tenantRepository.findById(id).map(tenant -> {
      tenant.setActive(active);
      return toDto(tenantRepository.save(tenant));
    });
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.TENANTS_DELETE + "')")
  public boolean delete(UUID id) {
    if (!tenantRepository.existsById(id)) return false;
    tenantRepository.deleteById(id);
    return true;
  }

  private TenantDto toDto(Tenant t) {
    return new TenantDto(t.getId(), t.getName(), t.isActive());
  }
}
