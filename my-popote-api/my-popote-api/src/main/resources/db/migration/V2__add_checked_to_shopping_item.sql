-- Ajoute l'état coché/décoché d'un article de la liste de courses.
-- Les articles existants sont considérés comme non cochés par défaut.

ALTER TABLE shopping_item
    ADD COLUMN checked BOOLEAN NOT NULL DEFAULT FALSE;