# Simulation de Propagation de Feu de Forêt

Projet ING1 — Spécialité Informatique | CY Tech  
Thématique #2 : Simulation de cellules dans un plan 2D

## Description

Application Java avec interface graphique simulant la propagation d'un feu de forêt
sur une grille 2D. Chaque cellule possède des attributs physiques (humidité,
inflammabilité, résistance, densité de végétation, température) qui influencent
la propagation.

## Fonctionnalités

- Simulation pas-à-pas avec contrôle de la vitesse
- Clic sur la grille pour allumer un feu manuellement
- Zoom avant/arrière (boutons ou touches `+` / `-` / `0`)
- Vent directionnel avec intensité réglable
- 3 algorithmes de propagation sélectionnables
- Topologies prédéfinies chargeables en un clic
- Sauvegarde et chargement de grille au format CSV
- Export des statistiques de simulation en CSV
- Légende des types de terrain

## Algorithmes disponibles

- **Orthogonal** : propagation aux 4 voisins directs (N/S/E/O)
- **Moore** : propagation aux 8 voisins (+ diagonales, pondérées par 1/√2)
- **Radiale** : propagation dans un rayon R, probabilité ∝ 1/distance

## Topologies prédéfinies

- **Forêt dense** : végétation dense avec zones humides indestructibles comme refuges
- **Rivière en forêt** : rivière diagonale avec berges humides bloquant la propagation
- **Ville avec cour** : blocs urbains séparés par des rues coupe-feu, parc central avec fontaine

## Structure du projet

```
src/main/java/simulation/
├── modele/         → Cellule, Grille, EtatCellule, TypeTerrain, Vent, ComportementFeu
├── algorithme/     → AlgorithmePropagation (interface), implémentations
├── vue/            → VueSimulation, PanneauGrille, PanneauControle, LegendPanel, WrapLayout
├── chargeur/       → ChargeurTopologie, TopologiesPredefinis
├── export/         → ExporteurSimulationCsv, InstantaneSimulation
└── utilitaire/     → Constantes, FormulesPhysiques
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
