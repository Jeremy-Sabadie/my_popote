-- ============================================================
-- My Popote
-- V4 - Tags multiples pour les recettes
-- ============================================================
--
-- Les tags deviennent des données stockées en base afin de pouvoir
-- les faire évoluer sans modifier le code métier.
--
-- La saisonnalité reste volontairement séparée : une saison décrit
-- une période de l'année alors qu'un tag décrit une caractéristique
-- nutritionnelle, alimentaire ou pratique.
-- ============================================================

CREATE TABLE tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    tag_group VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uk_tag_name (name),

    KEY idx_tag_group (tag_group)
);

CREATE TABLE recipe_tag (
    recipe_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY (recipe_id, tag_id),

    CONSTRAINT fk_recipe_tag_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipe(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recipe_tag_tag
        FOREIGN KEY (tag_id)
        REFERENCES tag(id)
        ON DELETE RESTRICT,

    KEY idx_recipe_tag_tag_id (tag_id)
);

-- Tags proposés pour le MVP.
-- Ils pourront ensuite être enrichis directement en base
-- sans modifier les entités Java.

INSERT INTO tag (name, tag_group) VALUES
    ('Riche en protéines', 'NUTRITION'),
    ('Sèche', 'NUTRITION'),
    ('Équilibré', 'NUTRITION'),
    ('Plat plaisir', 'NUTRITION'),

    ('Carné', 'DIET'),
    ('Végétarien', 'DIET'),
    ('Poisson', 'DIET'),

    ('Rapide', 'PRACTICAL'),
    ('Facile', 'PRACTICAL'),
    ('Meal-prep', 'PRACTICAL'),
    ('Économique', 'PRACTICAL');