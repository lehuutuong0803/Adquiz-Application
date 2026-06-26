import { useEffect, useState } from 'react'
import keycloak from './keycloak'
import client from '../api/client'

let initPromise: Promise<boolean> | null = null
let interceptorAdded = false

function initKeycloak(): Promise<boolean> {
  if (!initPromise) {
    initPromise = keycloak
      .init({ onLoad: 'check-sso', checkLoginIframe: false })
      .then(authenticated => {
        if (authenticated && !interceptorAdded) {
          client.interceptors.request.use(config => {
            if (keycloak.token) {
              config.headers.Authorization = `Bearer ${keycloak.token}`
            }
            return config
          })
          interceptorAdded = true
        }
        return authenticated
      })
      .catch(err => {
        console.warn('Keycloak init failed:', err)
        return false
      })
  }
  return initPromise
}


export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false)

  useEffect(() => {
    initKeycloak().then(() => setReady(true))
  }, [])

  if (!ready) return null

  return <>{children}</>
}

export function login() {
  keycloak.login({ redirectUri: window.location.origin + '/' })
}

export function register() {
  keycloak.register({ redirectUri: window.location.origin + '/' })
}

export function logout() {
  keycloak.logout({ redirectUri: window.location.origin + '/landing' })
}

export function isAuthenticated() {
    console.log('isAuthenticated called:', keycloak.authenticated)
  return keycloak.authenticated ?? false
}