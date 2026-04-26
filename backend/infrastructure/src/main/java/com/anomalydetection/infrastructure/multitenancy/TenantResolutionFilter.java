package com.anomalydetection.infrastructure.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that resolves the current tenant from the incoming request and
 * sets it on {@link CurrentTenantHolder}.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>{@code X-Tenant-Id} header</li>
 *   <li>{@code __tenant} query parameter</li>
 *   <li>Subdomain (e.g. {@code tenant1.example.com})</li>
 * </ol>
 *
 * <p>If no tenant is resolved, the request proceeds in host mode (no tenant filter).
 */
@Component
@Order(1)
public class TenantResolutionFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TenantResolutionFilter.class);

  static final String HEADER_NAME = "X-Tenant-Id";
  static final String PARAM_NAME = "__tenant";

  private final CurrentTenantHolder tenantHolder;

  public TenantResolutionFilter(CurrentTenantHolder tenantHolder) {
    this.tenantHolder = tenantHolder;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    UUID tenantId = resolveTenant(request);
    if (tenantId != null) {
      tenantHolder.setTenantId(tenantId);
      log.debug("Tenant resolved: {}", tenantId);
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      // Clear tenant after request completes
      tenantHolder.setTenantId(null);
    }
  }

  private UUID resolveTenant(HttpServletRequest request) {
    // 1. Header
    String header = request.getHeader(HEADER_NAME);
    if (header != null && !header.isBlank()) {
      return parseUuid(header);
    }
    // 2. Query parameter
    String param = request.getParameter(PARAM_NAME);
    if (param != null && !param.isBlank()) {
      return parseUuid(param);
    }
    // 3. Subdomain (simple extraction)
    String host = request.getServerName();
    if (host != null && host.contains(".") && !host.equals("localhost")) {
      String subdomain = host.substring(0, host.indexOf('.'));
      // Subdomain could be a tenant ID or a name; for now try UUID parse
      return parseUuid(subdomain);
    }
    return null;
  }

  private static UUID parseUuid(String value) {
    try {
      return UUID.fromString(value.trim());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid tenant ID format: {}", value);
      return null;
    }
  }
}