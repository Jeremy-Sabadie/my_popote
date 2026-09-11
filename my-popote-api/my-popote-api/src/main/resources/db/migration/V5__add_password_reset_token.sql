-- ============================================================
-- My Popote
-- V5 - Réinitialisation sécurisée du mot de passe
-- ============================================================
--
-- Le token transmis à l'utilisateur n'est jamais stocké en clair.
-- Seul son hash SHA-256 est conservé en base.
--
-- Un token :
-- - appartient à un utilisateur ;
-- - possède une date d'expiration ;
-- - ne peut être utilisé qu'une seule fois.
-- ============================================================

CREATE TABLE password_reset_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_password_reset_token_hash (token_hash),

    KEY idx_password_reset_token_user_id (user_id),
    KEY idx_password_reset_token_expires_at (expires_at)
);