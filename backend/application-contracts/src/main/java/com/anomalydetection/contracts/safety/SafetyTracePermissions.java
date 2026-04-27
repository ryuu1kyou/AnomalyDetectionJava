package com.anomalydetection.contracts.safety;

public final class SafetyTracePermissions {
  private SafetyTracePermissions() {}

  public static final String GROUP = "SafetyTrace";

  public static final String DEFAULT = "SafetyTrace.Records.Default";
  public static final String CREATE = "SafetyTrace.Records.Create";
  public static final String EDIT = "SafetyTrace.Records.Edit";
  public static final String DELETE = "SafetyTrace.Records.Delete";
  public static final String APPROVE = "SafetyTrace.Records.Approve";
  public static final String AUDIT_EXPORT = "SafetyTrace.Audit.Export";
}
