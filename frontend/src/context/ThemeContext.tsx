import {createContext, useContext, useEffect, useState} from 'react'

type ThemeContextType = {
    isDark: boolean
    toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextType | null>(null)

export function ThemeProvider({ children } : { children: React.ReactNode }) {
    const [isDark, setIsDark] = useState(
        () => localStorage.getItem('theme') === 'dark'
    )

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
        localStorage.setItem('theme', isDark ? 'dark' : 'light')
    }, [isDark])

    const toggleTheme = () => setIsDark(prev => !prev)

    return (
        <ThemeContext.Provider value={{isDark, toggleTheme}}>
            {children}
        </ThemeContext.Provider>
    )
}

export function useTheme() {
    const ctx = useContext(ThemeContext)
    if(!ctx) throw new Error('useTheme must be used within a ThemeProvider')
    return ctx
}