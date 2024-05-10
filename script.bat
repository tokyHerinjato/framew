@echo off
REM Compile Framework

REM Définir les répertoires de destination
set "DESTINATION_DIR=D:\Andry\ITU\S4\Mr Naina\Sprint\Sprint-0\test\WEB-INF"
set "LIB_SERVLET=C:\xampp\tomcat\lib\servlet-api.jar"

REM Vérifier si le répertoire de destination existe, sinon le créer
if not exist "%DESTINATION_DIR%\lib" (
    mkdir "%DESTINATION_DIR%\lib"
)

REM Compilation des fichiers Java
cd src
echo javac -cp %LIB_SERVLET% -d ..\classes *.java
javac -cp "%LIB_SERVLET%" -d ..\classes *.java


