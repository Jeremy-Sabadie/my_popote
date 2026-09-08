/**
 * Identifiants envoyés à l'API lors de la connexion.
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Informations nécessaires pour créer un compte.
 */
export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
}

/**
 * Réponse commune renvoyée par l'API après une connexion
 * ou une inscription réussie.
 */
export interface AuthResponse {
  email: string;
  firstName: string;
  accessToken: string;
}
