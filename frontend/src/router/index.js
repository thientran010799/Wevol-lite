import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  {
    path: '/',
    component: () => import('@/components/AppShell.vue'),
    children: [
      { path: 'dashboard',          component: () => import('@/views/DashboardView.vue') },
      { path: 'dashboard/timeline', component: () => import('@/views/TimelineView.vue') },
      { path: 'memories',           component: () => import('@/views/MemoriesView.vue') },
      { path: 'memories/:id',       component: () => import('@/views/MemoryDetailView.vue'), props: true },
      { path: 'gallery',            component: () => import('@/views/GalleryView.vue') },
      { path: 'gallery/:folderId',  component: () => import('@/views/GalleryFolderView.vue'), props: true },
      { path: 'trips',              component: () => import('@/views/TripsView.vue') },
      { path: 'trips/:id',          component: () => import('@/views/TripDetailView.vue'), props: true },
      { path: 'profile',            component: () => import('@/views/ProfileView.vue') },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

export default router
