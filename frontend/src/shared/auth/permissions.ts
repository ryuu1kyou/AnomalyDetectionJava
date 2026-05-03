/** Mirror of backend permission constants.
 * Keep in sync with backend/application-contracts/.../XxxPermissions.java
 */

export const CanSignalPermissions = {
  DEFAULT: 'CanSignal.Default',
  CREATE: 'CanSignal.Create',
  EDIT: 'CanSignal.Edit',
  DELETE: 'CanSignal.Delete',
} as const

export const AnomalyDetectionPermissions = {
  LOGIC_DEFAULT: 'AnomalyDetection.Logic.Default',
  LOGIC_CREATE: 'AnomalyDetection.Logic.Create',
  LOGIC_EDIT: 'AnomalyDetection.Logic.Edit',
  LOGIC_DELETE: 'AnomalyDetection.Logic.Delete',
  LOGIC_APPROVE: 'AnomalyDetection.Logic.Approve',
} as const

export const DetectionTemplatePermissions = {
  DEFAULT: 'DetectionTemplates.Default',
  CREATE: 'DetectionTemplates.Create',
  EDIT: 'DetectionTemplates.Edit',
  DELETE: 'DetectionTemplates.Delete',
} as const

export const ProjectPermissions = {
  DEFAULT: 'Projects.Projects.Default',
  CREATE: 'Projects.Projects.Create',
  EDIT: 'Projects.Projects.Edit',
  DELETE: 'Projects.Projects.Delete',
  MANAGE_MEMBERS: 'Projects.Projects.ManageMembers',
  MANAGE_MILESTONES: 'Projects.Projects.ManageMilestones',
} as const

export const SafetyTracePermissions = {
  DEFAULT: 'SafetyTrace.Records.Default',
  CREATE: 'SafetyTrace.Records.Create',
  EDIT: 'SafetyTrace.Records.Edit',
  DELETE: 'SafetyTrace.Records.Delete',
  APPROVE: 'SafetyTrace.Records.Approve',
  AUDIT_EXPORT: 'SafetyTrace.Audit.Export',
} as const

export const KnowledgeBasePermissions = {
  DEFAULT: 'KnowledgeBase.Articles.Default',
  CREATE: 'KnowledgeBase.Articles.Create',
  EDIT: 'KnowledgeBase.Articles.Edit',
  DELETE: 'KnowledgeBase.Articles.Delete',
  PUBLISH: 'KnowledgeBase.Articles.Publish',
} as const

export const OemTraceabilityPermissions = {
  APPROVAL_DEFAULT: 'OemTraceability.Approvals.Default',
  APPROVAL_CREATE: 'OemTraceability.Approvals.Create',
  APPROVAL_MANAGE: 'OemTraceability.Approvals.Manage',
  CUSTOMIZATION_DEFAULT: 'OemTraceability.Customizations.Default',
  CUSTOMIZATION_CREATE: 'OemTraceability.Customizations.Create',
  CUSTOMIZATION_MANAGE: 'OemTraceability.Customizations.Manage',
} as const

export const SimilarPatternSearchPermissions = {
  DEFAULT: 'SimilarPatternSearch.Default',
  SEARCH_SIGNALS: 'SimilarPatternSearch.SearchSignals',
  COMPARE_TEST_DATA: 'SimilarPatternSearch.CompareTestData',
} as const

export const IntegrationPermissions = {
  DEFAULT: 'Integration.Default',
  CREATE: 'Integration.Create',
  MANAGE: 'Integration.Manage',
  IMPORT_DATA: 'Integration.ImportData',
} as const

export type PermissionString =
  | typeof CanSignalPermissions[keyof typeof CanSignalPermissions]
  | typeof AnomalyDetectionPermissions[keyof typeof AnomalyDetectionPermissions]
  | typeof DetectionTemplatePermissions[keyof typeof DetectionTemplatePermissions]
  | typeof ProjectPermissions[keyof typeof ProjectPermissions]
  | typeof SafetyTracePermissions[keyof typeof SafetyTracePermissions]
  | typeof KnowledgeBasePermissions[keyof typeof KnowledgeBasePermissions]
  | typeof OemTraceabilityPermissions[keyof typeof OemTraceabilityPermissions]
  | typeof SimilarPatternSearchPermissions[keyof typeof SimilarPatternSearchPermissions]
  | typeof IntegrationPermissions[keyof typeof IntegrationPermissions]
