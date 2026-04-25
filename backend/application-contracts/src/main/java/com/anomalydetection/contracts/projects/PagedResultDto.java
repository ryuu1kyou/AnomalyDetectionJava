package com.anomalydetection.contracts.projects;

import java.util.List;

/** Simple paged result DTO (ABP style). */
public record PagedResultDto<T>(List<T> items, long totalCount) {
  public static <T> PagedResultDto<T> of(List<T> items, long totalCount) {
    return new PagedResultDto<>(items, totalCount);
  }
}
