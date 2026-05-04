package com.anomalydetection.contracts.safety;

public final class DecisionLedgerPermissions {
  private DecisionLedgerPermissions() {}

  public static final String GROUP = "SafetyTrace";

  public static final String DEFAULT = "SafetyTrace.DecisionLedger.Default";
  public static final String CREATE  = "SafetyTrace.DecisionLedger.Create";
  public static final String EDIT    = "SafetyTrace.DecisionLedger.Edit";
  public static final String DELETE  = "SafetyTrace.DecisionLedger.Delete";
  public static final String APPROVE = "SafetyTrace.DecisionLedger.Approve";
}
