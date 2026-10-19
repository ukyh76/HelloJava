@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "BUILD_DIR=%PROJECT_DIR%target\web-classes"

where javac >nul 2>nul
if errorlevel 1 (
    echo 未找到 JDK 21。请安装 JDK，并确保 javac 已加入 PATH。
    exit /b 1
)

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

type nul > "%PROJECT_DIR%target\web-sources.txt"
for /r "%PROJECT_DIR%src\main\java" %%f in (*.java) do echo "%%f">> "%PROJECT_DIR%target\web-sources.txt"
javac --release 21 --add-modules jdk.httpserver -encoding UTF-8 -d "%BUILD_DIR%" @"%PROJECT_DIR%target\web-sources.txt"
if errorlevel 1 exit /b 1

xcopy "%PROJECT_DIR%src\main\resources\*" "%BUILD_DIR%\" /E /I /Y >nul
java --add-modules jdk.httpserver -cp "%BUILD_DIR%" com.campus.web.WebApp
