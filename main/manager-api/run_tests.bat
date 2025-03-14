@echo off
echo ===== 编译和运行测试 =====

rem 创建输出目录
if not exist "target\classes" mkdir target\classes
if not exist "target\test-classes" mkdir target\test-classes

rem 设置类路径
set JUNIT_PATH=C:\path\to\junit-jupiter-api-5.8.2.jar;C:\path\to\junit-jupiter-engine-5.8.2.jar

rem 编译测试类
echo 编译测试类...
javac -d target\test-classes src\test\java\xiaozhi\modules\device\DeviceTest.java src\test\java\xiaozhi\modules\device\ActivationCodeTest.java src\test\java\xiaozhi\TestRunner.java

rem 如果编译成功，运行测试
if %ERRORLEVEL% == 0 (
    echo 编译成功，运行测试...
    java -classpath target\test-classes xiaozhi.TestRunner
) else (
    echo 编译失败，请检查错误信息。
)

echo ===== 测试完成 =====
pause 