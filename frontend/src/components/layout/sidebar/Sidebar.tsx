import { NavLink } from 'react-router-dom'
import './Sidebar.css'

const navItems = [
  { to: '/', icon: '⌂', label: 'Dashboard' },
  { to: '/topics', icon: '◎', label: 'Topics' },
  { to: '/reviews', icon: '↻', label: 'Reviews' },
  { to: '/progress', icon: '▧', label: 'Progress' },
]

type SidebarProps = {
    collapsed: boolean
    onToggle: () => void
}

export default function Sidebar({collapsed, onToggle}: SidebarProps) {
    return (
        <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
            <div className="sidebar-top">
                {!collapsed && <span className="sidebar-brand">Adquiz</span>}
                <button className="sidebar-toggle" onClick={onToggle}>
                    {collapsed ? '→' : '←'}
                </button>
            </div>

            <nav className="sidebar-nav">
                {navItems.map(item => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive}) =>
                            `nav-item ${ isActive ? 'active' : ''}`

                        }>
                        <span className="nav-icon">{item.icon}</span>
                        {!collapsed && <span className="nav-label">{item.label}</span>}
                    </NavLink>
                ))}
            </nav>
        </aside>
    )
}