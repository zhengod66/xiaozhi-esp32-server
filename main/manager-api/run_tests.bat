@echo off
rem 設置UTF-8編碼
chcp 65001

echo ===== 編譯和運行測試 =====

rem 設置Java和Maven路徑
set JAVA_HOME=C:\Program Files\Java\jdk-21
set JAVA_BIN=%JAVA_HOME%\bin

rem 創建輸出目錄
if not exist "target\classes" mkdir target\classes
if not exist "target\test-classes" mkdir target\test-classes

rem 設置類路徑 - 只包含基本測試所需的依賴
set CLASSPATH=target\test-classes;target\classes
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.11.4\junit-jupiter-api-5.11.4.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\junit\platform\junit-platform-commons\1.11.4\junit-platform-commons-1.11.4.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\opentest4j\opentest4j\1.3.0\opentest4j-1.3.0.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\apiguardian\apiguardian-api\1.1.2\apiguardian-api-1.1.2.jar

echo 編譯基本測試類...
"%JAVA_BIN%\javac" -encoding UTF-8 -d target\test-classes -cp %CLASSPATH% src\test\java\xiaozhi\modules\device\DeviceTest.java src\test\java\xiaozhi\modules\device\ActivationCodeTest.java src\test\java\xiaozhi\modules\device\JwtTokenTest.java

echo 創建簡化版TestRunner...
echo package xiaozhi; > src\test\java\xiaozhi\SimpleTestRunner.java
echo. >> src\test\java\xiaozhi\SimpleTestRunner.java
echo import xiaozhi.modules.device.ActivationCodeTest; >> src\test\java\xiaozhi\SimpleTestRunner.java
echo import xiaozhi.modules.device.DeviceTest; >> src\test\java\xiaozhi\SimpleTestRunner.java
echo. >> src\test\java\xiaozhi\SimpleTestRunner.java
echo public class SimpleTestRunner { >> src\test\java\xiaozhi\SimpleTestRunner.java
echo     public static void main(String[] args) { >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         System.out.println("========== 開始運行基本測試 =========="); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo. >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         try { >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             System.out.println("\n----- 運行設備測試 -----"); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             DeviceTest deviceTest = new DeviceTest(); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             deviceTest.testSmoke(); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             System.out.println("設備測試通過！"); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         } catch (Exception e) { >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             System.out.println("設備測試失敗：" + e.getMessage()); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             e.printStackTrace(); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         } >> src\test\java\xiaozhi\SimpleTestRunner.java
echo. >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         try { >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             System.out.println("\n----- 運行激活碼測試 -----"); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             ActivationCodeTest activationCodeTest = new ActivationCodeTest(); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             activationCodeTest.testSmoke(); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             System.out.println("激活碼測試通過！"); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         } catch (Exception e) { >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             System.out.println("激活碼測試失敗：" + e.getMessage()); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo             e.printStackTrace(); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         } >> src\test\java\xiaozhi\SimpleTestRunner.java
echo. >> src\test\java\xiaozhi\SimpleTestRunner.java
echo         System.out.println("\n========== 測試完成 =========="); >> src\test\java\xiaozhi\SimpleTestRunner.java
echo     } >> src\test\java\xiaozhi\SimpleTestRunner.java
echo } >> src\test\java\xiaozhi\SimpleTestRunner.java

"%JAVA_BIN%\javac" -encoding UTF-8 -d target\test-classes -cp %CLASSPATH% src\test\java\xiaozhi\SimpleTestRunner.java

rem 如果編譯成功，運行測試
if %ERRORLEVEL% == 0 (
    echo 編譯成功，運行測試...
    "%JAVA_BIN%\java" -cp %CLASSPATH% xiaozhi.SimpleTestRunner
) else (
    echo 編譯失敗，請檢查錯誤信息。
)

echo ===== 測試完成 =====
pause 