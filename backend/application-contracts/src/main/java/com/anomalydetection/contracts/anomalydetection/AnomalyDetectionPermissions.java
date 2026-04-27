package com.anomalydetection.contracts.anomalydetection;

public final class AnomalyDetectionPermissions {
  private AnomalyDetectionPermissions() {}

  public static final String GROUP = "AnomalyDetection";

  public static final String LOGIC_DEFAULT = "AnomalyDetection.Logic.Default";
  public static final String LOGIC_CREATE = "AnomalyDetection.Logic.Create";
  public static final String LOGIC_EDIT = "AnomalyDetection.Logic.Edit";
  public static final String LOGIC_DELETE = "AnomalyDetection.Logic.Delete";
  public static final String LOGIC_APPROVE = "AnomalyDetection.Logic.Approve";

  public static final String RESULT_DEFAULT = "AnomalyDetection.Result.Default";
  public static final String RESULT_CREATE = "AnomalyDetection.Result.Create";
  public static final String RESULT_EDIT = "AnomalyDetection.Result.Edit";
  public static final String RESULT_DELETE = "AnomalyDetection.Result.Delete";
}
