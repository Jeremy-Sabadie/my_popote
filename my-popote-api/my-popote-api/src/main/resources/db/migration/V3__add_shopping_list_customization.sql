-- ============================================================
-- My Popote
-- V3 - Personnalisation de la liste de courses
-- ============================================================
--
-- Deux nouveaux besoins sont couverts :
--
-- 1. L'utilisateur peut signaler qu'il possède déjà un article.
-- 2. L'utilisateur peut ajouter manuellement une envie qui ne
--    provient d'aucune recette.
--
-- Un article manuel n'est pas obligé d'être lié au référentiel
-- global des ingrédients. Son nom est stocké dans custom_name.
-- ============================================================

ALTER TABLE shopping_item
    MODIFY COLUMN ingredient_id BIGINT NULL;

ALTER TABLE shopping_item
    ADD COLUMN custom_name VARCHAR(150) NULL,
    ADD COLUMN already_owned BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN manual BOOLEAN NOT NULL DEFAULT FALSE;