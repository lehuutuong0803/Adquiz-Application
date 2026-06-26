import { AuthProvider, isAuthenticated, login } from './auth/AuthProvider'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './components/layout/app-layout/AppLayout'
import LandingPage from './pages/landing/LandingPage'
import Dashboard from './pages/dashboard/Dashboard'
import TopicSearch from './pages/topics/TopicSearch'
import QuizSession from './pages/quiz-session/QuizSession'
import SessionComplete from './pages/session-complete/SessionComplete'
import ReviewQueue from './pages/reviews/ReviewQueue'
import Progress from './pages/progress/Progress'

function ProtectedRoute({ children } : { children: React.ReactNode }) {
  if (!isAuthenticated()) {
    return <Navigate to="/landing" replace />
  }
  return <>{children}</>
}


export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes> 
          <Route path="/landing" element={<LandingPage />} />

          <Route element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }>

            <Route path="/" element={<Dashboard />} />
            <Route path="/topics" element={<TopicSearch />} />
            <Route path="/reviews" element={<ReviewQueue />} />
            <Route path="/progress" element={<Progress />} />
            <Route path="/sessions/:id" element={<QuizSession />} />
            <Route path="/sessions/:id/complete" element={<SessionComplete />} />
          </Route>
          
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      </AuthProvider> 
  )
}
