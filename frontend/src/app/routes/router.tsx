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
      { path: 'can-signals', element: <CanSignalListPage /> },
      { path: 'can-signal-specs', element: <CanSignalSpecPage /> },
      { path: 'detection-templates', element: <DetectionTemplateListPage /> },
      { path: 'anomaly-detection', element: <AnomalyDetectionListPage /> },
      // Projects
      { path: 'projects', element: <ProjectsPage /> },
      { path: 'projects/list', element: <ProjectListPage /> },
      { path: 'projects/:projectId', element: <ProjectDetailPage /> },
      // Quality / Safety
      { path: 'safety', element: <SafetyPage /> },
      { path: 'knowledge-base', element: <KnowledgeBasePage /> },
      { path: 'oem-traceability', element: <OemTraceabilityPage /> },
      // Integration
      { path: 'integration', element: <IntegrationListPage /> },
    ],
  },
])
