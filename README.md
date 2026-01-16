# CPU Monitor for Android

A lightweight Android app with home screen widget for real-time CPU and memory monitoring, designed to detect and manage resource-hungry background processes.

## Features

### Home Screen Widget
- **Real-time monitoring** - Updates every 2 seconds
- **Color-coded alerts** - Green (normal), Yellow (warning), Red (critical)
- **CPU usage percentage** - Shows current system CPU load
- **Memory usage** - Displays RAM consumption
- **Suspicious process detection** - Alerts for high-CPU background apps

### Main App
- **Process list** - View all suspicious background processes
- **Tap to kill** - Instantly terminate resource-hungry apps
- **Kill all** - One-tap to clean all suspicious processes
- **Detailed stats** - PID, CPU time, and process names

## Installation

### Download APK
[Download latest APK](https://github.com/fivelidz/cpu-monitor/releases)

### Install from source
```bash
# Clone repository
git clone https://github.com/fivelidz/cpu-monitor.git
cd cpu-monitor

# Build with Gradle
export JAVA_HOME=/path/to/java-17
gradle assembleDebug

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Building on Termux

This app was developed entirely on Android using Termux:

```bash
# Install requirements
pkg install openjdk-17 gradle git

# Build
gradle assembleDebug
```

## Usage

1. Install the APK
2. Open the app to grant necessary permissions
3. Long-press home screen → Widgets → CPU Monitor
4. Drag widget to desired location

## Permissions

- `GET_TASKS` - Read running processes
- `KILL_BACKGROUND_PROCESSES` - Terminate apps
- `PACKAGE_USAGE_STATS` - Access detailed stats
- `FOREGROUND_SERVICE` - Continuous monitoring
- `POST_NOTIFICATIONS` - Alert notifications

## Architecture

```
app/
├── src/main/java/com/fivelidz/cpumonitor/
│   ├── MainActivity.java         # Process list UI
│   ├── CpuMonitorService.java   # Background monitoring
│   └── CpuWidgetProvider.java   # Widget updates
├── src/main/res/
│   ├── layout/
│   │   ├── activity_main.xml    # Main UI
│   │   └── cpu_widget.xml       # Widget UI
│   └── xml/
│       └── cpu_widget_info.xml  # Widget config
└── build.gradle                 # Build config
```

## Detection Algorithm

The app identifies suspicious processes based on:
- High CPU time in background state
- Processes consuming >10000 CPU ticks
- Background importance level

## Screenshots

### Widget
- Compact 3x1 widget showing:
  - CPU: XX% (color-coded)
  - MEM: XX%
  - Alert status

### Main App
- Clean list interface
- Refresh button
- Kill all button
- Tap individual processes to terminate

## Development

Built with:
- Java 17
- Android SDK 34
- Minimum SDK 21 (Android 5.0+)
- No external dependencies

## Why This App?

Created after discovering a stuck `npm install` process consuming 100% CPU on my phone. This app would have immediately detected and alerted me to the issue, saving battery life and improving performance.

## License

MIT License - Feel free to use and modify

## Author

**fivelidz**
GitHub: [https://github.com/fivelidz](https://github.com/fivelidz)

---

*Built entirely on Android via Termux - because why use a PC when your phone is the computer?*