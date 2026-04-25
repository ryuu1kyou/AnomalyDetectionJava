import { createBrowserRouter } from 'react-router-dom'
import RootLayout from '../layouts/RootLayout'
import HomePage from '../pages/HomePage'
import ProjectsPage from '../pages/ProjectsPage'
import ProjectDetailPage from '../projects/pages/ProjectDetailPage'
import ProjectListPage from '../projects/pages/ProjectListPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'projects', element: <ProjectsPage /> },
      { path: 'projects/list', element: <ProjectListPage /> },
      { path: 'projects/:projectId', element: <ProjectDetailPage /> },
    ],
  },
])
