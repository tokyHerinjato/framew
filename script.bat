@echo off


set SRC_DIR=src
set LIB_DIR=lib
set OUT_DIR=out
set OUT_JAR=FrontServlet.jar


:ErrorExit
echo %1
exit /b 1


echo Compilation des classes Java...
mkdir %OUT_DIR%
if %errorlevel% neq 0 call :ErrorExit "Échec de la création du répertoire %OUT_DIR%."


echo Listage des fichiers sources Java...
del sources.txt 2>nul
for /r %SRC_DIR% %%f in (*.java) do echo %%f >> sources.txt


javac -cp "%LIB_DIR%/*" -d %OUT_DIR% @sources.txt
if %errorlevel% neq 0 (
    call :ErrorExit "Échec de la compilation. Veuillez vérifier les erreurs."
) else (
    echo Compilation réussie.
)


echo Création du fichier JAR...
jar cf %OUT_JAR% -C %OUT_DIR% .
if %errorlevel% neq 0 (
    call :ErrorExit "Échec de la création du fichier JAR."
) else (
    echo Fichier JAR créé: %OUT_JAR%
)


echo Nettoyage...
del sources.txt
rmdir /s /q %OUT_DIR%
if %errorlevel% neq 0 (
    call :ErrorExit "Échec de la suppression du répertoire %OUT_DIR%."
) else (
    echo Répertoire %OUT_DIR% supprimé.
)

echo Terminé.
exit /b 0
