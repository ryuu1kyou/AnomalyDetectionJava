package com.anomalydetection.infrastructure.projects;

import com.anomalydetection.domain.projects.AnomalyDetectionProject;
import com.anomalydetection.domain.projects.ProjectSearchCriteria;
import com.anomalydetection.domain.projects.ProjectSearchResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/** Implementation for {@link JpaAnomalyDetectionProjectRepositoryCustom}. */
public class JpaAnomalyDetectionProjectRepositoryImpl
    implements JpaAnomalyDetectionProjectRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public ProjectSearchResult searchProjects(ProjectSearchCriteria criteria) {
    var cb = em.getCriteriaBuilder();

    // --- select query ---
    var cq = cb.createQuery(AnomalyDetectionProject.class);
    var root = cq.from(AnomalyDetectionProject.class);
    var predicates = buildPredicates(criteria, cb, root);
    cq.where(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
    cq.orderBy(parseOrder(criteria.sorting(), cb, root));

    var query = em.createQuery(cq);
    query.setFirstResult(Math.max(0, criteria.skip()));
    query.setMaxResults(Math.max(1, criteria.take()));
    List<AnomalyDetectionProject> items = query.getResultList();

    // --- count query ---
    var countCq = cb.createQuery(Long.class);
    var countRoot = countCq.from(AnomalyDetectionProject.class);
    countCq.select(cb.count(countRoot));
    var countPredicates = buildPredicates(criteria, cb, countRoot);
    countCq.where(countPredicates.toArray(jakarta.persistence.criteria.Predicate[]::new));

    long total = em.createQuery(countCq).getSingleResult();
    return new ProjectSearchResult(items, total);
  }

  private static List<jakarta.persistence.criteria.Predicate> buildPredicates(
      ProjectSearchCriteria criteria,
      jakarta.persistence.criteria.CriteriaBuilder cb,
      jakarta.persistence.criteria.Root<AnomalyDetectionProject> root) {

    var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

    if (criteria.filter() != null && !criteria.filter().isBlank()) {
      String like = "%" + criteria.filter().trim().toLowerCase() + "%";
      predicates.add(
          cb.or(
              cb.like(cb.lower(root.get("projectCode")), like),
              cb.like(cb.lower(root.get("name")), like)));
    }

    if (criteria.status() != null && !criteria.status().isBlank()) {
      predicates.add(cb.equal(root.get("status"), criteria.status()));
    }

    if (criteria.priority() != null && !criteria.priority().isBlank()) {
      predicates.add(cb.equal(root.get("priority"), criteria.priority()));
    }

    if (criteria.vehicleModel() != null && !criteria.vehicleModel().isBlank()) {
      String like = "%" + criteria.vehicleModel().trim().toLowerCase() + "%";
      predicates.add(cb.like(cb.lower(root.get("vehicleModel")), like));
    }

    if (criteria.primarySystem() != null && !criteria.primarySystem().isBlank()) {
      String like = "%" + criteria.primarySystem().trim().toLowerCase() + "%";
      predicates.add(cb.like(cb.lower(root.get("primarySystem")), like));
    }

    return predicates;
  }

  private static jakarta.persistence.criteria.Order parseOrder(
      String sorting,
      jakarta.persistence.criteria.CriteriaBuilder cb,
      jakarta.persistence.criteria.Root<AnomalyDetectionProject> root) {

    if (sorting == null || sorting.isBlank()) {
      return cb.asc(cb.lower(root.get("projectCode")));
    }

    // Take first token only (ignore multiple sort keys for now)
    String[] parts = sorting.trim().split("\\s+");
    String field = parts[0];
    boolean desc = parts.length > 1 && parts[1].equalsIgnoreCase("desc");

    // Whitelist fields to avoid injection.
    return switch (field) {
      case "projectCode" -> desc
          ? cb.desc(cb.lower(root.get("projectCode")))
          : cb.asc(cb.lower(root.get("projectCode")));
      case "name" -> desc
          ? cb.desc(cb.lower(root.get("name")))
          : cb.asc(cb.lower(root.get("name")));
      case "status" -> desc ? cb.desc(root.get("status")) : cb.asc(root.get("status"));
      case "priority" -> desc ? cb.desc(root.get("priority")) : cb.asc(root.get("priority"));
      case "vehicleModel" -> desc
          ? cb.desc(cb.lower(root.get("vehicleModel")))
          : cb.asc(cb.lower(root.get("vehicleModel")));
      case "primarySystem" -> desc
          ? cb.desc(cb.lower(root.get("primarySystem")))
          : cb.asc(cb.lower(root.get("primarySystem")));
      default -> cb.asc(cb.lower(root.get("projectCode")));
    };
  }
}
