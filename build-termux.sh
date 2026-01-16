#!/bin/bash

PROJECT_DIR=~/workspace/projects/cpu-monitor
BUILD_DIR=$PROJECT_DIR/app/build
ANDROID_JAR=~/android-sdk/platforms/android-34/android.jar

echo "CPU Monitor Build Script (Termux)"
echo "================================="

mkdir -p $BUILD_DIR/{classes,res,outputs,gen}

echo "1. Generating R.java..."
aapt package -f -m \
    -J $BUILD_DIR/gen \
    -M $PROJECT_DIR/app/src/main/AndroidManifest.xml \
    -S $PROJECT_DIR/app/src/main/res \
    -I $ANDROID_JAR

echo "2. Compiling Java sources..."
javac -d $BUILD_DIR/classes \
    -classpath $ANDROID_JAR \
    -sourcepath "$PROJECT_DIR/app/src/main/java:$BUILD_DIR/gen" \
    $BUILD_DIR/gen/com/fivelidz/cpumonitor/R.java \
    $PROJECT_DIR/app/src/main/java/com/fivelidz/cpumonitor/*.java

echo "3. Creating DEX..."
dx --dex --output=$BUILD_DIR/classes.dex \
    $BUILD_DIR/classes/

echo "4. Creating APK..."
aapt package -f \
    -M $PROJECT_DIR/app/src/main/AndroidManifest.xml \
    -S $PROJECT_DIR/app/src/main/res \
    -I $ANDROID_JAR \
    -F $BUILD_DIR/outputs/cpu-monitor-unsigned.apk

echo "5. Adding DEX to APK..."
cd $BUILD_DIR
zip -r outputs/cpu-monitor-unsigned.apk classes.dex

echo "6. Creating keystore if needed..."
if [ ! -f ~/.android/debug.keystore ]; then
    keytool -genkey -v \
        -keystore ~/.android/debug.keystore \
        -storepass android \
        -alias androidkey \
        -keypass android \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

echo "7. Signing APK..."
apksigner sign \
    --ks ~/.android/debug.keystore \
    --ks-pass pass:android \
    --out ~/workspace/builds/cpu-monitor.apk \
    $BUILD_DIR/outputs/cpu-monitor-unsigned.apk

echo ""
echo "✅ Build complete!"
echo "📱 APK: ~/workspace/builds/cpu-monitor.apk"
echo ""
echo "To install: pm install ~/workspace/builds/cpu-monitor.apk"