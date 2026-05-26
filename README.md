# Simulation de Propagation de Feu de Forêt

Projet ING1 — Spécialité Informatique | CY Tech  
Thématique #2 : Simulation de cellules dans un plan 2D

## Description

Application Java avec interface graphique simulant la propagation d'un feu de forêt
sur une grille 2D. Chaque cellule possède des attributs physiques (humidité,
inflammabilité, résistance, densité de végétation, température) qui influencent
la propagation.

## Algorithmes disponibles

- **Orthogonal** : propagation aux 4 voisins directs (N/S/E/O)
- **Moore** : propagation aux 8 voisins (+ diagonales, pondérées par 1/√2)
- **Radiale** : propagation dans un rayon R, probabilité ∝ 1/distance

## Structure du projet

```
src/
├── main/java/simulation/
│   ├── modele/         → Cellule, Grille, EtatCellule, Vent
│   ├── algorithme/     → AlgorithmePropagation (interface), implémentations
│   ├── vue/            → VueSimulation, PanneauGrille, PanneauControle
│   ├── chargeur/       → ChargeurTopologie (fichier / génération aléatoire)
│   └── utilitaire/     → Constantes, FormulesPhysiques
├── test/               → Tests unitaires
resources/
└── topologies/         → Fichiers .json de topologies pré-définies
```

## Lancer le projet

```bash
javac -d bin src/main/java/simulation/**/*.java
java -cp bin simulation.Simulateur
```

## Auteurs

- Muhammad Redha Boutmene
- Louay Dardouri
- Ayman Yazid
- Sidar Mese
- Anes Litim
- Sulliman Chetouani

## Encadrants

Eva ANSERMIN & Romuald GRIGNON
