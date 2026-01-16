#!/bin/bash

PROJECT_DIR=~/workspace/projects/cpu-monitor
BUILD_DIR=$PROJECT_DIR/app/build
ANDROID_SDK=~/android-sdk
ANDROID_JAR=$ANDROID_SDK/platforms/android-34/android.jar
BUILD_TOOLS=$ANDROID_SDK/build-tools/34.0.0

echo "CPU Monitor Build Script"
echo "========================"

mkdir -p $BUILD_DIR/{classes,res,outputs}

echo "1. Compiling R.java..."
$BUILD_TOOLS/aapt package -f -m \
    -J $PROJECT_DIR/app/src/main/java \
    -M $PROJECT_DIR/app/src/main/AndroidManifest.xml \
    -S $PROJECT_DIR/app/src/main/res \
    -I $ANDROID_JAR

echo "2. Compiling Java sources..."
javac -d $BUILD_DIR/classes \
    -classpath $ANDROID_JAR \
    -sourcepath $PROJECT_DIR/app/src/main/java \
    $PROJECT_DIR/app/src/main/java/com/fivelidz/cpumonitor/*.java

echo "3. Creating DEX file..."
$BUILD_TOOLS/d8 \
    --output $BUILD_DIR \
    --classpath $ANDROID_JAR \
    $BUILD_DIR/classes/com/fivelidz/cpumonitor/*.class

echo "4. Creating APK..."
$BUILD_TOOLS/aapt package -f \
    -M $PROJECT_DIR/app/src/main/AndroidManifest.xml \
    -S $PROJECT_DIR/app/src/main/res \
    -I $ANDROID_JAR \
    -F $BUILD_DIR/outputs/cpu-monitor-unsigned.apk \
    $BUILD_DIR

echo "5. Adding DEX to APK..."
cd $BUILD_DIR
$BUILD_TOOLS/aapt add ../outputs/cpu-monitor-unsigned.apk classes.dex

echo "6. Signing APK..."
$BUILD_TOOLS/apksigner sign \
    --ks-key-alias androidkey \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out $BUILD_DIR/outputs/cpu-monitor.apk \
    $BUILD_DIR/outputs/cpu-monitor-unsigned.apk

echo "Build complete! APK available at:"
echo "$BUILD_DIR/outputs/cpu-monitor.apk"
echo ""
echo "To install: pm install $BUILD_DIR/outputs/cpu-monitor.apk"