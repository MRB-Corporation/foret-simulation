#!/bin/bash
# ==========================================
# Script de lancement rapide pour Linux/Mac
# (Equivalent d'un Makefile)
# ==========================================

# ⚠️ MODIFIEZ CETTE LIGNE AVEC LE CHEMIN VERS VOTRE JAVAFX SUR LE PC DE L'ECOLE
JAVAFX_PATH="/chemin/vers/javafx-sdk-21.0.11/lib"

echo "[1/2] Compilation des fichiers Java..."
mkdir -p bin
find src/main/java -name "*.java" > sources.txt
javac --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml -d bin @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo "[ERREUR] Echec de la compilation."
    exit 1
fi

echo "[2/2] Lancement de la simulation..."
java --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml -cp bin simulation.Main
