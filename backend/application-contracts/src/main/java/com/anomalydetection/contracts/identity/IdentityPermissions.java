package com.anomalydetection.contracts.identity;

public final class IdentityPermissions {
  public static final String GROUP = "AnomalyDetection.Identity";

  public static final String USERS = GROUP + ".Users";
  public static final String USERS_VIEW = USERS + ".View";
  public static final String USERS_CREATE = USERS + ".Create";
  public static final String USERS_EDIT = USERS + ".Edit";
  public static final String USERS_DELETE = USERS + ".Delete";

  public static final String ROLES = GROUP + ".Roles";
  public static final String ROLES_VIEW = ROLES + ".View";
  public static final String ROLES_CREATE = ROLES + ".Create";
  public static final String ROLES_EDIT = ROLES + ".Edit";
  public static final String ROLES_DELETE = ROLES + ".Delete";

  public static final String TENANTS = GROUP + ".Tenants";
  public static final String TENANTS_VIEW = TENANTS + ".View";
  public static final String TENANTS_CREATE = TENANTS + ".Create";
  public static final String TENANTS_EDIT = TENANTS + ".Edit";
  public static final String TENANTS_DELETE = TENANTS + ".Delete";

  private IdentityPermissions() {}
}