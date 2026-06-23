import { useEffect, useState } from 'react'
import keycloak from './keycloak'
import client from '../api/client'

export function AuthProvider({ children } : {children: React.ReactNode}) {
    const [ready, setReady] = useState(false)

    useEffect(() => {
        keycloak.init({onLoad: 'check-sso'}).then(() => {
            client.interceptors.request.use((config) => {
                if (keycloak.token) {
                    client.defaults.headers.common['Authorization'] = `Bearer ${keycloak.token}`
                }
                return config
            })
            setReady(true)
        })
    }, [])

    if (!ready) return null

    return <>{children}</>
}

export function login() {
    keycloak.login()
}

export function logout() {
    keycloak.logout({ redirectUri: window.location.origin })
}

export function register() {
    keycloak.register()
}

export function isAuthenticated() {
    return keycloak.authenticated ?? false
}