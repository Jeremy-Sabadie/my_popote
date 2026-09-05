CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_email (email)
);

CREATE TABLE recipe (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    servings INT NOT NULL DEFAULT 1,
    estimated_cost DECIMAL(10,2) NULL,
    instructions TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_recipe_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    KEY idx_recipe_user_id (user_id),
    KEY idx_recipe_category (category)
);

CREATE TABLE ingredient (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_ingredient_name (name)
);

CREATE TABLE recipe_ingredient (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit VARCHAR(30) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_recipe_ingredient_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipe(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recipe_ingredient_ingredient
        FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(id)
        ON DELETE RESTRICT,

    UNIQUE KEY uk_recipe_ingredient (
        recipe_id,
        ingredient_id,
        unit
    ),

    KEY idx_recipe_ingredient_recipe_id (recipe_id),
    KEY idx_recipe_ingredient_ingredient_id (ingredient_id)
);

CREATE TABLE recipe_season (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    season VARCHAR(20) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_recipe_season_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipe(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_recipe_season (
        recipe_id,
        season
    ),

    KEY idx_recipe_season_recipe_id (recipe_id)
);

CREATE TABLE meal_plan (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    include_weekend BOOLEAN NOT NULL DEFAULT FALSE,
    max_budget DECIMAL(10,2) NULL,
    estimated_cost DECIMAL(10,2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_meal_plan_user
        FOREIGN KEY (user_id)
        REFERENCES app_user(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_meal_plan_user_week (
        user_id,
        week_start_date
    ),

    KEY idx_meal_plan_user_id (user_id),
    KEY idx_meal_plan_week_start_date (week_start_date)
);

CREATE TABLE planned_meal (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meal_plan_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_planned_meal_meal_plan
        FOREIGN KEY (meal_plan_id)
        REFERENCES meal_plan(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_planned_meal_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipe(id)
        ON DELETE RESTRICT,

    UNIQUE KEY uk_planned_meal_slot (
        meal_plan_id,
        meal_date,
        meal_type
    ),

    KEY idx_planned_meal_meal_plan_id (meal_plan_id),
    KEY idx_planned_meal_recipe_id (recipe_id),
    KEY idx_planned_meal_date (meal_date)
);

CREATE TABLE shopping_list (
    id BIGINT NOT NULL AUTO_INCREMENT,
    meal_plan_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_shopping_list_meal_plan
        FOREIGN KEY (meal_plan_id)
        REFERENCES meal_plan(id)
        ON DELETE CASCADE,

    UNIQUE KEY uk_shopping_list_meal_plan (meal_plan_id)
);

CREATE TABLE shopping_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shopping_list_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    is_checked BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id),

    CONSTRAINT fk_shopping_item_shopping_list
        FOREIGN KEY (shopping_list_id)
        REFERENCES shopping_list(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_shopping_item_ingredient
        FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(id)
        ON DELETE RESTRICT,

    UNIQUE KEY uk_shopping_item (
        shopping_list_id,
        ingredient_id,
        unit
    ),

    KEY idx_shopping_item_shopping_list_id (shopping_list_id),
    KEY idx_shopping_item_ingredient_id (ingredient_id)
);