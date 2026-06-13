@echo off
REM ==========================================
REM Script de lancement rapide pour Windows
REM (Equivalent d'un Makefile)
REM ==========================================

REM ⚠️ MODIFIEZ CETTE LIGNE AVEC LE CHEMIN VERS VOTRE JAVAFX SUR LE PC DE L'ECOLE
SET JAVAFX_PATH=C:\Users\louay\OneDrive\Bureau\javafx-sdk-21.0.11\lib

echo [1/2] Compilation des fichiers Java...
if not exist bin mkdir bin
dir /s /b src\main\java\*.java > sources.txt
javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -d bin @sources.txt
del sources.txt

IF %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Echec de la compilation.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Lancement de la simulation...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp bin simulation.Main
