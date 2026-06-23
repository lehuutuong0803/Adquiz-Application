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
        
    )
}