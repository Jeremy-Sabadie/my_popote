package fr.mypopote.my_popote_api.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Représente une caractéristique pouvant être attribuée à une recette.
 *
 * Les tags sont stockés en base afin de pouvoir enrichir le référentiel
 * sans devoir modifier le code métier à chaque nouvelle valeur.
 */
@Entity
@Table(name = "tag")
public class Tag {

    /**
     * Identifiant technique généré par MariaDB.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Libellé affiché à l'utilisateur.
     *
     * Exemple : "Sèche", "Rapide" ou "Riche en protéines".
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Groupe permettant d'organiser les tags dans l'interface.
     *
     * Il reste volontairement stocké comme une String afin
     * de pouvoir faire évoluer les groupes sans modifier le code Java.
     */
    @Column(name = "tag_group", nullable = false, length = 50)
    private String groupName;

    /**
     * Date de création du tag.
     */
    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    protected Tag() {
        // Constructeur requis par JPA
    }

    public Tag(String name, String groupName) {
        this.name = name;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}