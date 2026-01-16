#!/bin/bash
# CPU Monitor APK Build Script
# Based on successful orbital build method

set -e

PROJECT_DIR="$HOME/workspace/projects/cpu-monitor"
APP_DIR="$PROJECT_DIR/app"
SRC_DIR="$APP_DIR/src/main"
BUILD_DIR="$PROJECT_DIR/build_manual"
SDK_DIR="$HOME/android-sdk"
BUILD_TOOLS="$SDK_DIR/build-tools/34.0.0"
PLATFORM="$SDK_DIR/platforms/android-34"

AAPT="$BUILD_TOOLS/aapt"
D8="$BUILD_TOOLS/d8"
ZIPALIGN="$BUILD_TOOLS/zipalign"
APKSIGNER="$BUILD_TOOLS/apksigner"
ANDROID_JAR="$PLATFORM/android.jar"

echo "=== CPU Monitor Build Script ==="
echo "Building in: $BUILD_DIR"

# Clean
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"/{gen,classes,bin}

echo "[1/6] Generating R.java..."
$AAPT package -f -m \
    -J "$BUILD_DIR/gen" \
    -M "$SRC_DIR/AndroidManifest.xml" \
    -S "$SRC_DIR/res" \
    -I "$ANDROID_JAR"

echo "[2/6] Compiling Java sources..."
find "$SRC_DIR/java" -name "*.java" > "$BUILD_DIR/sources.txt"
echo "$BUILD_DIR/gen/com/fivelidz/cpumonitor/R.java" >> "$BUILD_DIR/sources.txt"

javac -source 17 -target 17 \
    -bootclasspath "$ANDROID_JAR" \
    -classpath "$ANDROID_JAR" \
    -d "$BUILD_DIR/classes" \
    @"$BUILD_DIR/sources.txt"

echo "[3/6] Creating DEX..."
$D8 --output "$BUILD_DIR" \
    --lib "$ANDROID_JAR" \
    $(find "$BUILD_DIR/classes" -name "*.class")

echo "[4/6] Packaging APK..."
$AAPT package -f \
    -M "$SRC_DIR/AndroidManifest.xml" \
    -S "$SRC_DIR/res" \
    -I "$ANDROID_JAR" \
    -F "$BUILD_DIR/cpu-monitor-unsigned.apk"

# Add DEX to APK
cd "$BUILD_DIR"
zip -u cpu-monitor-unsigned.apk classes.dex

echo "[5/6] Aligning APK..."
$ZIPALIGN -f 4 "$BUILD_DIR/cpu-monitor-unsigned.apk" "$BUILD_DIR/cpu-monitor-aligned.apk"

echo "[6/6] Signing APK..."
# Create debug keystore if not exists
KEYSTORE="$PROJECT_DIR/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkey -v \
        -keystore "$KEYSTORE" \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=Debug,O=Android,C=US"
fi

$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out "$BUILD_DIR/cpu-monitor.apk" \
    "$BUILD_DIR/cpu-monitor-aligned.apk"

# Copy to downloads folder as requested
DOWNLOADS="$HOME/storage/downloads"
if [ ! -d "$DOWNLOADS" ]; then
    DOWNLOADS="$HOME/workspace/builds"
fi

mkdir -p "$DOWNLOADS"
cp "$BUILD_DIR/cpu-monitor.apk" "$DOWNLOADS/cpu-monitor.apk"

echo ""
echo "=== BUILD COMPLETE ==="
echo "APK: $DOWNLOADS/cpu-monitor.apk"
echo ""
echo "The app includes a HOME SCREEN WIDGET that will:"
echo "✓ Show real-time CPU and memory usage"
echo "✓ Alert you to suspicious background processes"
echo "✓ Update every 2 seconds automatically"
echo ""
echo "To install: pm install $DOWNLOADS/cpu-monitor.apk"
echo "Or use: termux-open $DOWNLOADS/cpu-monitor.apk"
echo ""
echo "After installing, long-press your home screen and add the CPU Monitor widget!"