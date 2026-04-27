package com.anomalydetection.contracts.oemtraceability;

public final class OemTraceabilityPermissions {
  private OemTraceabilityPermissions() {}

  public static final String GROUP = "OemTraceability";

  public static final String APPROVAL_DEFAULT = "OemTraceability.Approvals.Default";
  public static final String APPROVAL_CREATE = "OemTraceability.Approvals.Create";
  public static final String APPROVAL_MANAGE = "OemTraceability.Approvals.Manage";

  public static final String CUSTOMIZATION_DEFAULT = "OemTraceability.Customizations.Default";
  public static final String CUSTOMIZATION_CREATE = "OemTraceability.Customizations.Create";
  public static final String CUSTOMIZATION_MANAGE = "OemTraceability.Customizations.Manage";
}
