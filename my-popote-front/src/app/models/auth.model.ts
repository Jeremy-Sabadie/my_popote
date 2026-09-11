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
 * Réponse commune renvoyée par l'API après
 * une connexion ou une inscription réussie.
 */
export interface AuthResponse {
  id: number;
  email: string;
  firstName: string;
  accessToken: string;
}

/**
 * Demande d'envoi d'un lien de réinitialisation.
 */
export interface ForgotPasswordRequest {
  email: string;
}

/**
 * Nouveau mot de passe associé au token reçu par e-mail.
 */
export interface ResetPasswordRequest {
  token: string;
  password: string;
}

/**
 * Réponse simple retournée par les opérations
 * de récupération du compte.
 */
export interface MessageResponse {
  message: string;
}
