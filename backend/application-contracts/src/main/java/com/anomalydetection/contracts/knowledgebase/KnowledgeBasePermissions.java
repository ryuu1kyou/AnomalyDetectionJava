package com.anomalydetection.contracts.knowledgebase;

public final class KnowledgeBasePermissions {
  private KnowledgeBasePermissions() {}

  public static final String GROUP = "KnowledgeBase";

  public static final String DEFAULT = "KnowledgeBase.Articles.Default";
  public static final String CREATE = "KnowledgeBase.Articles.Create";
  public static final String EDIT = "KnowledgeBase.Articles.Edit";
  public static final String DELETE = "KnowledgeBase.Articles.Delete";
  public static final String PUBLISH = "KnowledgeBase.Articles.Publish";
}
