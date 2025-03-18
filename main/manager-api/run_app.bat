@echo off
echo 正在启动小智ESP32管理API...
echo 当前目录: %CD%

REM 使用mvn命令启动
call mvn -f pom.xml clean compile spring-boot:run -Dspring-boot.run.profiles=dev

if %ERRORLEVEL% neq 0 (
    echo Maven命令失败，尝试使用java -jar直接运行...
    if exist target\xiaozhi-esp32-api.jar (
        java -jar target\xiaozhi-esp32-api.jar --spring.profiles.active=dev
    ) else (
        echo 找不到目标JAR文件，尝试重新打包...
        call mvn -f pom.xml clean package -DskipTests
        if exist target\xiaozhi-esp32-api.jar (
            java -jar target\xiaozhi-esp32-api.jar --spring.profiles.active=dev
        ) else (
            echo 无法启动应用程序，请检查Maven配置和构建日志。
            pause
            exit /b 1
        )
    )
)
pause 