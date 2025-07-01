@echo off

set src_dir=src
set lib_dir=lib
set return_dir=return
set out_jar=D:\Andry\ITU\S5\Naina\Sprint\Sprint-13

echo compilation des classes Java ...
mkdir %return_dir%

echo listage des fichiers sources Java ...
del classes.txt 2>nul
for /r %src_dir% %%f in (*.java) do echo %%f >> classes.txt

echo compilation ............
echo javac -cp %lib_dir%\* -d %return_dir% @classes.txt
javac -cp %lib_dir%\* -d %return_dir% @classes.txt
echo verification des erreurs .....
@REM if %errorlevel% neq 0 (
@REM     @REM call :ErrorExit "Échec de la compilation. Veuillez vérifier les erreurs."
@REM     echo erreur détecté
@REM     pause
@REM ) else (
@REM     echo Compilation réussie.
@REM )

echo création du fichier JAR ...
@REM jar cf  -C %return_dir% .
echo jar -cf FrontServlet.jar -C return .
jar -cf FrontServlet.jar -C return .

if exist D:\Andry\ITU\S5\Naina\Sprint\Sprint-13\proj\WEB-INF\lib\FrontServlet.jar (
    del D:\Andry\ITU\S5\Naina\Sprint\Sprint-13\proj\WEB-INF\lib\FrontServlet.jar
)
copy FrontServlet.jar D:\Andry\ITU\S5\Naina\Sprint\Sprint-13\proj\WEB-INF\lib

echo nettoyage ....
del classes.txt
rmdir /s /q %return_dir%