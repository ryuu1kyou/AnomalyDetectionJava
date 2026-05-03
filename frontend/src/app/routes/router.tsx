import { createBrowserRouter } from 'react-router-dom'
import AuthGuard from '../components/AuthGuard'
import RootLayout from '../layouts/RootLayout'
import HomePage from '../pages/HomePage'
import ProjectsPage from '../pages/ProjectsPage'
import ProjectDetailPage from '../projects/pages/ProjectDetailPage'
import ProjectListPage from '../projects/pages/ProjectListPage'
import CallbackPage from '../pages/CallbackPage'
import CanSignalListPage from '../../modules/cansignals/CanSignalListPage'
import DetectionTemplateListPage from '../../modules/detectiontemplates/DetectionTemplateListPage'
import AnomalyDetectionListPage from '../../modules/anomalydetection/AnomalyDetectionListPage'
import IntegrationListPage from '../../modules/integration/IntegrationListPage'
import CanSignalSpecPage from '../../modules/cansspecification/CanSignalSpecPage'
import SafetyPage from '../../modules/safety/SafetyPage'
import KnowledgeBasePage from '../../modules/knowledgebase/KnowledgeBasePage'
import OemTraceabilityPage from '../../modules/oemtraceability/OemTraceabilityPage'
import SimilarPatternSearchPage from '../../modules/similarpatternsearch/SimilarPatternSearchPage'
import AuditLogPage from '../../modules/auditlog/AuditLogPage'
import SettingsPage from '../../modules/settings/SettingsPage'
import FeaturesPage from '../../modules/features/FeaturesPage'
import PermissionsPage from '../../modules/permissions/PermissionsPage'
import { RequirePermission } from '../../shared/auth/RequirePermission'
import {
  AdminPermissions,
  AnomalyDetectionPermissions,
  CanSignalPermissions,
  DetectionTemplatePermissions,
  IntegrationPermissions,
  KnowledgeBasePermissions,
  OemTraceabilityPermissions,
  ProjectPermissions,
  SafetyTracePermissions,
  SimilarPatternSearchPermissions,
} from '../../shared/auth/permissions'

export const router = createBrowserRouter([
  {
    path: '/callback',
    element: <CallbackPage />,
  },
  {
    path: '/',
    element: <AuthGuard><RootLayout /></AuthGuard>,
    children: [
      { index: true, element: <HomePage /> },
      // Core domain
      { path: 'can-signals', element: <RequirePermission permission={CanSignalPermissions.DEFAULT}><CanSignalListPage /></RequirePermission> },
      { path: 'can-signal-specs', element: <RequirePermission permission={CanSignalPermissions.DEFAULT}><CanSignalSpecPage /></RequirePermission> },
      { path: 'detection-templates', element: <RequirePermission permission={DetectionTemplatePermissions.DEFAULT}><DetectionTemplateListPage /></RequirePermission> },
      { path: 'anomaly-detection', element: <RequirePermission permission={AnomalyDetectionPermissions.LOGIC_DEFAULT}><AnomalyDetectionListPage /></RequirePermission> },
      // Projects
      { path: 'projects', element: <RequirePermission permission={ProjectPermissions.DEFAULT}><ProjectsPage /></RequirePermission> },
      { path: 'projects/list', element: <RequirePermission permission={ProjectPermissions.DEFAULT}><ProjectListPage /></RequirePermission> },
      { path: 'projects/:projectId', element: <RequirePermission permission={ProjectPermissions.DEFAULT}><ProjectDetailPage /></RequirePermission> },
      // Quality / Safety
      { path: 'safety', element: <RequirePermission permission={SafetyTracePermissions.DEFAULT}><SafetyPage /></RequirePermission> },
      { path: 'knowledge-base', element: <RequirePermission permission={KnowledgeBasePermissions.DEFAULT}><KnowledgeBasePage /></RequirePermission> },
      { path: 'oem-traceability', element: <RequirePermission permission={OemTraceabilityPermissions.APPROVAL_DEFAULT}><OemTraceabilityPage /></RequirePermission> },
      { path: 'similar-pattern-search', element: <RequirePermission permission={SimilarPatternSearchPermissions.DEFAULT}><SimilarPatternSearchPage /></RequirePermission> },
      // Integration
      { path: 'integration', element: <RequirePermission permission={IntegrationPermissions.DEFAULT}><IntegrationListPage /></RequirePermission> },
      // Administration — requires identity admin permissions
      { path: 'audit-log', element: <RequirePermission permission={AdminPermissions.AUDIT_LOG}><AuditLogPage /></RequirePermission> },
      { path: 'settings', element: <RequirePermission permission={AdminPermissions.SETTINGS}><SettingsPage /></RequirePermission> },
      { path: 'features', element: <RequirePermission permission={AdminPermissions.FEATURES}><FeaturesPage /></RequirePermission> },
      { path: 'permissions', element: <RequirePermission permission={AdminPermissions.PERMISSIONS}><PermissionsPage /></RequirePermission> },
    ],
  },
])
