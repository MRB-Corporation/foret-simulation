# Architecture du Projet : Simulation de Feu de Forêt

Ce document décrit la structure complète de l'application. Le projet respecte le patron de conception **MVC (Modèle-Vue-Contrôleur)**, ce qui permet de séparer clairement l'affichage de la logique de calcul.

```text
foret-simulation/
├── docs/                             # Contient la documentation Javadoc générée
└── src/main/java/simulation/         # Code source principal
    │
    ├── Main.java                     # Point d'entrée brut (lance l'application JavaFX)
    │
    ├── config/                       # Fichiers de configuration globale
    │   └── Constants.java            # Contient toutes les constantes mathématiques (vitesse, température par défaut, probabilités)
    │
    ├── dao/                          # (Data Access Object) Gestion des sauvegardes et topologies
    │   ├── BinarySerializer.java     # Sauvegarde et charge l'état exact (en binaire .ffsave)
    │   ├── GridLoader.java           # Génère des grilles aléatoires ou charge depuis un CSV
    │   ├── PredefinedTopologies.java # Génère les forêts célèbres (Amazonie, Landes, Boréale) avec un bruit organique
    │   └── SimulationCsvExporter.java# Exporte les statistiques du feu dans un fichier CSV
    │
    ├── entities/                     # (MODÈLE) Les objets purs de la simulation
    │   ├── Cell.java                 # Représente une case de la forêt (humidité, température...)
    │   ├── CellState.java            # Enumération de l'état du feu (Intact, En feu, Brûlé)
    │   ├── Environment.java          # Gère la saison et l'ensoleillement (modificateurs globaux)
    │   ├── Grid.java                 # La matrice 2D contenant toutes les cellules (gère aussi le monde torique)
    │   ├── GridStats.java            # Calcule les pourcentages et moyennes à un instant T
    │   ├── SavedState.java           # Objet temporaire contenant une Grid et un Wind pour le chargement
    │   ├── SimulationSnapshot.java   # Photographie des statistiques à un moment précis pour l'export CSV
    │   ├── TerrainType.java          # Enumération des types de sols (Forêt, Eau, Ville, etc.)
    │   └── Wind.java                 # Modélise la vitesse et la direction du vent
    │
    ├── services/                     # (CONTRÔLEUR) La logique métier et les calculs
    │   ├── Simulator.java            # Le "cerveau" : fait avancer le temps, gère le feu et le vent
    │   ├── FireBehaviour.java        # Interface pour les objets qui peuvent réagir au feu
    │   ├── SpreadAlgorithm.java      # Interface pour les formes de propagation
    │   ├── OrthogonalSpread.java     # Propagation du feu en croix (+)
    │   ├── MooreSpread.java          # Propagation du feu en carré (8 voisins)
    │   └── RadialSpread.java         # Propagation du feu en cercle (par rayon)
    │
    ├── ui/                           # (VUE) L'interface graphique JavaFX
    │   ├── MainApp.java              # Construit la fenêtre principale et assemble les panneaux
    │   ├── CLI.java                  # Version en Ligne de Commande (sans interface graphique)
    │   ├── ControlPane.java          # Le panneau du bas avec les boutons (Play, Pause, Sliders)
    │   ├── GridCanvas.java           # Le composant qui dessine les petits carrés de couleur de la grille
    │   ├── LegendPane.java           # Le menu de gauche qui affiche les couleurs (légende)
    │   └── StatsPane.java            # Le menu de droite avec les graphiques en temps réel
    │
    └── utils/                        # Utilitaires mathématiques
        └── PhysicsFormulas.java      # Contient les calculs purs (Formule d'ignition, facteur vent, thermodynamique)
```

## Principe de Fonctionnement
1. L'utilisateur clique sur "Start" dans `ControlPane` (**Vue**).
2. Le `ControlPane` appelle la méthode `timeStep()` du `Simulator` (**Contrôleur**).
3. Le `Simulator` applique les formules de `PhysicsFormulas` pour modifier l'état des `Cell` dans la `Grid` (**Modèle**).
4. Le `Simulator` prévient le `GridCanvas` de se redessiner avec les nouvelles couleurs.
