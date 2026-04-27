package com.anomalydetection.contracts.projects;

public final class ProjectPermissions {
  private ProjectPermissions() {}

  public static final String GROUP = "Projects";

  public static final String DEFAULT = "Projects.Projects.Default";
  public static final String CREATE = "Projects.Projects.Create";
  public static final String EDIT = "Projects.Projects.Edit";
  public static final String DELETE = "Projects.Projects.Delete";
  public static final String MANAGE_MEMBERS = "Projects.Projects.ManageMembers";
  public static final String MANAGE_MILESTONES = "Projects.Projects.ManageMilestones";
}
