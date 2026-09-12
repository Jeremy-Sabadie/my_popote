import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

import { RecipeService } from '../../core/services/recipe.service';
import { Recipe, RecipeRequest, RecipeTag } from '../../models/recipe.model';

/**
 * Page de consultation et de gestion de la bibliothèque de recettes.
 *
 * Les détails et le formulaire sont affichés dans une modale afin
 * d'éviter les déplacements dans la page et de garder les actions
 * importantes facilement accessibles.
 */
@Component({
  selector: 'app-recipes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './recipes.component.html',
  styleUrl: './recipes.component.css',
})
export class RecipesComponent implements OnInit {
  recipes: Recipe[] = [];
  tags: RecipeTag[] = [];

  selectedTagIds = new Set<number>();

  selectedRecipe: Recipe | null = null;
  editingRecipe: Recipe | null = null;

  loading = true;
  saving = false;
  downloadingRecipe = false;
  showRecipeForm = false;

  errorMessage = '';
  formErrorMessage = '';

  readonly seasons = [
    { value: 'SPRING', label: 'Printemps' },
    { value: 'SUMMER', label: 'Été' },
    { value: 'AUTUMN', label: 'Automne' },
    { value: 'WINTER', label: 'Hiver' },
    { value: 'ALL_YEAR', label: 'Toute l’année' },
  ];

  readonly recipeForm;

  constructor(
    private readonly recipeService: RecipeService,
    private readonly formBuilder: FormBuilder,
  ) {
    this.recipeForm = this.formBuilder.nonNullable.group({
      name: ['', [Validators.required, Validators.maxLength(150)]],
      servings: [2, [Validators.required, Validators.min(1)]],
      estimatedCost: [null as number | null, Validators.min(0)],
      instructions: ['', Validators.maxLength(10000)],
      seasons: this.formBuilder.nonNullable.control<string[]>(['ALL_YEAR']),
      tagIds: this.formBuilder.nonNullable.control<number[]>([]),
      ingredients: this.formBuilder.array([this.createIngredientForm()]),
    });
  }

  ngOnInit(): void {
    this.loadPage();
  }

  get ingredients(): FormArray {
    return this.recipeForm.controls.ingredients;
  }

  /**
   * La modale est ouverte lorsqu'on consulte ou édite une recette.
   */
  get modalOpen(): boolean {
    return this.showRecipeForm || this.selectedRecipe !== null;
  }

  private loadPage(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      recipes: this.recipeService.getRecipes(),
      tags: this.recipeService.getTags(),
    }).subscribe({
      next: ({ recipes, tags }) => {
        this.recipes = recipes;
        this.tags = tags;
        this.loading = false;
      },

      error: () => {
        this.errorMessage =
          'Impossible de charger les recettes pour le moment.';

        this.loading = false;
      },
    });
  }

  private createIngredientForm(ingredientName = '', quantity = 1, unit = 'g') {
    return this.formBuilder.nonNullable.group({
      ingredientName: [ingredientName, Validators.required],
      quantity: [quantity, [Validators.required, Validators.min(0.01)]],
      unit: [unit, Validators.required],
    });
  }

  /**
   * Ouvre directement la modale en mode création.
   */
  openRecipeForm(): void {
    this.selectedRecipe = null;
    this.editingRecipe = null;

    this.resetRecipeForm();

    this.formErrorMessage = '';
    this.showRecipeForm = true;
  }

  /**
   * Ferme complètement la modale.
   */
  closeModal(): void {
    if (this.saving || this.downloadingRecipe) {
      return;
    }

    this.showRecipeForm = false;
    this.selectedRecipe = null;
    this.editingRecipe = null;

    this.resetRecipeForm();
  }

  /**
   * Affiche une recette sans déplacer la page.
   */
  viewRecipe(recipe: Recipe): void {
    this.selectedRecipe = recipe;
    this.editingRecipe = null;
    this.showRecipeForm = false;
  }

  /**
   * Passe directement du détail au formulaire de modification.
   */
  editRecipe(recipe: Recipe): void {
    this.selectedRecipe = recipe;
    this.editingRecipe = recipe;

    this.recipeForm.patchValue({
      name: recipe.name,
      servings: recipe.servings,
      estimatedCost: recipe.estimatedCost,
      instructions: recipe.instructions ?? '',
      seasons: [...recipe.seasons],
      tagIds: recipe.tags.map((tag) => tag.id),
    });

    this.ingredients.clear();

    recipe.ingredients.forEach((ingredient) => {
      this.ingredients.push(
        this.createIngredientForm(
          ingredient.ingredientName,
          ingredient.quantity,
          ingredient.unit,
        ),
      );
    });

    if (this.ingredients.length === 0) {
      this.ingredients.push(this.createIngredientForm());
    }

    this.formErrorMessage = '';
    this.showRecipeForm = true;
  }

  /**
   * Annule une modification et revient simplement au détail.
   */
  cancelForm(): void {
    if (this.saving) {
      return;
    }

    if (this.editingRecipe && this.selectedRecipe) {
      this.showRecipeForm = false;
      this.editingRecipe = null;
      this.formErrorMessage = '';

      return;
    }

    this.closeModal();
  }

  addIngredient(): void {
    this.ingredients.push(this.createIngredientForm());
  }

  removeIngredient(index: number): void {
    if (this.ingredients.length === 1) {
      return;
    }

    this.ingredients.removeAt(index);
  }

  /**
   * Gère la saisonnalité sans permettre un état invalide.
   */
  toggleSeason(season: string): void {
    const control = this.recipeForm.controls.seasons;
    const current = [...control.value];

    if (season === 'ALL_YEAR') {
      control.setValue(['ALL_YEAR']);
      return;
    }

    const withoutAllYear = current.filter((value) => value !== 'ALL_YEAR');

    if (withoutAllYear.includes(season)) {
      const remaining = withoutAllYear.filter((value) => value !== season);

      control.setValue(remaining.length > 0 ? remaining : ['ALL_YEAR']);

      return;
    }

    control.setValue([...withoutAllYear, season]);
  }

  isSeasonSelected(season: string): boolean {
    return this.recipeForm.controls.seasons.value.includes(season);
  }

  toggleRecipeTag(tagId: number): void {
    const control = this.recipeForm.controls.tagIds;
    const selectedTags = [...control.value];

    if (selectedTags.includes(tagId)) {
      control.setValue(
        selectedTags.filter((selectedTagId) => selectedTagId !== tagId),
      );

      return;
    }

    control.setValue([...selectedTags, tagId]);
  }

  isRecipeTagSelected(tagId: number): boolean {
    return this.recipeForm.controls.tagIds.value.includes(tagId);
  }

  /**
   * Valide le formulaire puis demande confirmation avant
   * d'enregistrer la modification d'une recette existante.
   */
  async saveRecipe(): Promise<void> {
    if (this.saving) {
      return;
    }

    if (this.recipeForm.invalid) {
      this.recipeForm.markAllAsTouched();
      this.formErrorMessage = this.buildFormValidationMessage();
      return;
    }

    this.formErrorMessage = '';

    const request = this.buildRecipeRequest();

    if (this.editingRecipe) {
      const recipeToUpdate = this.editingRecipe;

      const result = await Swal.fire({
        title: 'Enregistrer les modifications ?',
        text: `Les modifications de "${recipeToUpdate.name}" seront enregistrées.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Enregistrer',
        cancelButtonText: 'Annuler',
        reverseButtons: true,
      });

      if (!result.isConfirmed) {
        return;
      }

      this.saving = true;
      this.updateRecipe(recipeToUpdate, request);

      return;
    }

    this.saving = true;
    this.createRecipe(request);
  }

  private buildRecipeRequest(): RecipeRequest {
    const value = this.recipeForm.getRawValue();

    return {
      name: value.name.trim(),

      /*
       * Champ historique encore attendu par le backend.
       */
      category: this.editingRecipe?.category ?? 'OTHER',

      servings: value.servings,
      estimatedCost: value.estimatedCost,
      instructions: value.instructions.trim() || null,

      ingredients: value.ingredients.map((ingredient) => ({
        ingredientName: ingredient.ingredientName.trim(),
        quantity: ingredient.quantity,
        unit: ingredient.unit.trim(),
      })),

      seasons: value.seasons,
      tagIds: value.tagIds,
    };
  }

  private createRecipe(request: RecipeRequest): void {
    this.recipeService.createRecipe(request).subscribe({
      next: (recipe) => {
        this.recipes = [recipe, ...this.recipes];

        this.selectedRecipe = recipe;
        this.showRecipeForm = false;
        this.editingRecipe = null;
        this.saving = false;

        this.resetRecipeForm();

        void Swal.fire({
          title: 'Recette ajoutée',
          text: `"${recipe.name}" a bien été enregistrée.`,
          icon: 'success',
          timer: 1400,
          showConfirmButton: false,
        });
      },

      error: (error: HttpErrorResponse) => {
        this.saving = false;

        console.error('Erreur lors de la création de la recette :', error);

        this.formErrorMessage = this.buildSaveErrorMessage(error, false);
      },
    });
  }

  private updateRecipe(originalRecipe: Recipe, request: RecipeRequest): void {
    this.recipeService.updateRecipe(originalRecipe.id, request).subscribe({
      next: (updatedRecipe) => {
        this.recipes = this.recipes.map((recipe) =>
          recipe.id === updatedRecipe.id ? updatedRecipe : recipe,
        );

        this.selectedRecipe = updatedRecipe;
        this.editingRecipe = null;
        this.showRecipeForm = false;
        this.saving = false;

        this.resetRecipeForm();

        void Swal.fire({
          title: 'Recette modifiée',
          text: 'Les modifications ont bien été enregistrées.',
          icon: 'success',
          timer: 1400,
          showConfirmButton: false,
        });
      },

      error: (error: HttpErrorResponse) => {
        this.saving = false;

        console.error(
          `Échec de la modification de la recette ${originalRecipe.id} :`,
          error,
        );

        this.formErrorMessage = this.buildSaveErrorMessage(error, true);
      },
    });
  }

  /**
   * Transforme les principaux statuts HTTP en messages utiles.
   */
  private buildSaveErrorMessage(
    error: HttpErrorResponse,
    editing: boolean,
  ): string {
    switch (error.status) {
      case 0:
        return 'Impossible de joindre le serveur. Vérifiez que l’API est disponible.';

      case 400:
        return 'Certaines informations de la recette sont invalides. Vérifiez les champs saisis.';

      case 401:
        return 'Votre session n’est plus valide. Veuillez vous reconnecter.';

      case 403:
        return 'Vous n’êtes pas autorisé à modifier cette recette.';

      case 404:
        return editing
          ? 'Cette recette n’existe plus ou n’est plus accessible.'
          : 'La ressource demandée est introuvable.';

      default:
        return editing
          ? 'Les modifications n’ont pas pu être enregistrées. Veuillez réessayer.'
          : 'La recette n’a pas pu être enregistrée. Veuillez réessayer.';
    }
  }

  /**
   * Télécharge uniquement la recette actuellement consultée.
   */
  downloadRecipe(recipe: Recipe): void {
    if (this.downloadingRecipe) {
      return;
    }

    this.downloadingRecipe = true;

    this.recipeService.downloadRecipe(recipe.id).subscribe({
      next: (recipeFile) => {
        const downloadUrl = URL.createObjectURL(recipeFile);
        const link = document.createElement('a');

        link.href = downloadUrl;
        link.download = this.buildRecipeFilename(recipe);

        document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(downloadUrl);

        this.downloadingRecipe = false;
      },

      error: (error: HttpErrorResponse) => {
        this.downloadingRecipe = false;

        console.error(
          `Erreur lors du téléchargement de la recette ${recipe.id} :`,
          error,
        );

        void Swal.fire({
          title: 'Téléchargement impossible',
          text: 'La recette n’a pas pu être téléchargée pour le moment.',
          icon: 'error',
          confirmButtonText: 'Fermer',
        });
      },
    });
  }

  /**
   * Produit un nom de fichier lisible sans caractères problématiques.
   */
  private buildRecipeFilename(recipe: Recipe): string {
    const safeName = recipe.name
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');

    return `my-popote-${safeName || `recette-${recipe.id}`}.txt`;
  }

  async deleteRecipe(recipe: Recipe): Promise<void> {
    const result = await Swal.fire({
      title: 'Supprimer cette recette ?',
      text: `"${recipe.name}" sera définitivement supprimée de votre bibliothèque.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      reverseButtons: true,
    });

    if (!result.isConfirmed) {
      return;
    }

    this.recipeService.deleteRecipe(recipe.id).subscribe({
      next: () => {
        this.recipes = this.recipes.filter(
          (currentRecipe) => currentRecipe.id !== recipe.id,
        );

        this.closeModal();

        void Swal.fire({
          title: 'Recette supprimée',
          icon: 'success',
          timer: 1300,
          showConfirmButton: false,
        });
      },

      error: () => {
        void Swal.fire({
          title: 'Suppression impossible',
          text: 'La recette n’a pas pu être supprimée pour le moment.',
          icon: 'error',
          confirmButtonText: 'Fermer',
        });
      },
    });
  }

  /**
   * Les champs réellement indispensables sont volontairement limités.
   */
  private buildFormValidationMessage(): string {
    const controls = this.recipeForm.controls;

    if (controls.name.hasError('required')) {
      return 'Veuillez indiquer un nom pour la recette.';
    }

    if (controls.name.hasError('maxlength')) {
      return 'Le nom de la recette est trop long.';
    }

    if (
      controls.servings.hasError('required') ||
      controls.servings.hasError('min')
    ) {
      return 'Le nombre de portions doit être au minimum de 1.';
    }

    if (controls.estimatedCost.hasError('min')) {
      return 'Le coût estimé ne peut pas être négatif.';
    }

    for (let index = 0; index < this.ingredients.length; index++) {
      const ingredient = this.ingredients.at(index);

      if (ingredient.get('ingredientName')?.hasError('required')) {
        return `Veuillez indiquer le nom de l’ingrédient ${index + 1}.`;
      }

      if (ingredient.get('quantity')?.invalid) {
        return `Veuillez vérifier la quantité de l’ingrédient ${index + 1}.`;
      }

      if (ingredient.get('unit')?.hasError('required')) {
        return `Veuillez indiquer l’unité de l’ingrédient ${index + 1}.`;
      }
    }

    return 'Veuillez vérifier les informations saisies.';
  }

  private resetRecipeForm(): void {
    this.recipeForm.reset({
      name: '',
      servings: 2,
      estimatedCost: null,
      instructions: '',
      seasons: ['ALL_YEAR'],
      tagIds: [],
    });

    this.ingredients.clear();
    this.ingredients.push(this.createIngredientForm());

    this.formErrorMessage = '';
  }

  toggleTag(tagId: number): void {
    if (this.selectedTagIds.has(tagId)) {
      this.selectedTagIds.delete(tagId);
    } else {
      this.selectedTagIds.add(tagId);
    }

    this.loadFilteredRecipes();
  }

  isTagSelected(tagId: number): boolean {
    return this.selectedTagIds.has(tagId);
  }

  private loadFilteredRecipes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.recipeService.getRecipes([...this.selectedTagIds]).subscribe({
      next: (recipes) => {
        this.recipes = recipes;
        this.loading = false;
      },

      error: () => {
        this.errorMessage =
          'Impossible de filtrer les recettes pour le moment.';

        this.loading = false;
      },
    });
  }

  clearFilters(): void {
    this.selectedTagIds.clear();
    this.loadFilteredRecipes();
  }

  seasonLabel(season: string): string {
    const labels: Record<string, string> = {
      SPRING: 'Printemps',
      SUMMER: 'Été',
      AUTUMN: 'Automne',
      WINTER: 'Hiver',
      ALL_YEAR: 'Toute l’année',
    };

    return labels[season.toUpperCase()] ?? season;
  }
}
