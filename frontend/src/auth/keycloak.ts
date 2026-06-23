import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
    url: 'http://localhost:8080',
    realm: 'adquiz',
    clientId: 'adquiz-frontend',
})

export default keycloak;