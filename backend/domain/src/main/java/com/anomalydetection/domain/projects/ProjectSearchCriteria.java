package com.anomalydetection.domain.projects;

/**
 * Criteria object for project list search.
 *
 * <p>This stays in the domain layer to avoid Spring Data's Pageable/Page types.
 */
public record ProjectSearchCriteria(
    String filter,
    String status,
    String priority,
    String vehicleModel,
    String primarySystem,
    int skip,
    int take,
    String sorting) {
}
