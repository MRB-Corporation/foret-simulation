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

- **Forêt Amazonienne** : Très dense, très humide, traversée par le fleuve Amazone, propagation très difficile.
- **Forêt des Landes (France)** : Forêt de pins très géométrique séparée par des pare-feux en terre, très inflammable.
- **Forêt Boréale (Canada)** : Forêt très hétérogène parsemée de milliers de petits lacs naturels.
- **Brousse Australienne (Outback)** : Très clairsemée, atrocement sèche et hautement inflammable, entrecoupée de bancs de sable.

## Structure du projet

```text
src/main/java/simulation/
├── entities/   → Cell, Grid, Wind, GridStats, SavedState
├── services/   → Simulator, SpreadAlgorithm (interfaces et implémentations)
├── ui/         → MainApp, GridCanvas, ControlPane, StatsPane, CLI
├── dao/        → BinarySerializer, SimulationCsvExporter, PredefinedTopologies
├── config/     → Constants
└── utils/      → PhysicsFormulas
```

## Prérequis

- **Java Development Kit (JDK) 21** ou supérieur.
- **Apache Maven** (installé et ajouté au `PATH` de votre système).

## Lancer le projet

Ce projet peut être lancé de deux façons différentes, selon si le logiciel Maven est installé ou non sur votre ordinateur.

### Option A : Via Maven (Recommandée)
Si Apache Maven est installé sur votre machine, c'est la méthode la plus propre car elle utilise le fichier `pom.xml`. Placez-vous à la racine du projet et exécutez :

```bash
mvn clean javafx:run
```

### Option B : Via les scripts manuels (PC de l'école)
Si Maven n'est pas installé (par exemple sur les ordinateurs de l'école), nous avons fourni des scripts d'exécution automatique (équivalents d'un Makefile).

1. Ouvrez le fichier `run.bat` (sous Windows) ou `run.sh` (sous Linux/Mac).
2. Modifiez la ligne `JAVAFX_PATH=...` en y mettant le chemin vers le dossier `lib` du JavaFX SDK présent sur votre machine ou sur votre clé USB.
3. Exécutez le script dans votre terminal :

**Sur Windows :**
```bash
.\run.bat
```

**Sur Linux/Mac :**
```bash
bash run.sh
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
