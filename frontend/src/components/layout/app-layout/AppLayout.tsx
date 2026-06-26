import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { useTheme } from '../../../context/ThemeContext'
import { logout } from '../../../auth/AuthProvider'
import Sidebar from '../sidebar/Sidebar'
import './AppLayout.css'

export default function AppLayout() {
    const [collapsed, setCollapsed] = useState(false)
    const {isDark, toggleTheme} = useTheme()

    return (
        <div className={`app-layout ${collapsed ? 'sidebar-collapsed' : ''}`}>
            <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(prev => !prev)} />
            
            <div className="main-area">
                <header className="top-bar">
                    <div className="top-bar-right">
                        <button className="theme-toggle" onClick={toggleTheme}>
                            {isDark ? '☀️' : '🌙'}
                        </button>
                        <button className="logout-btn" onClick={logout}>
                            Logout
                        </button>
                    </div>
                </header>

                <main className="page-content">
                    <Outlet />
                </main>
            </div>
        </div>
    )
}