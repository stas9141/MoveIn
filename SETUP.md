# Setup Guide for MoveIn App

## Java Runtime Issue Resolution

The app requires Java to build. Here are the steps to resolve the Java runtime issue:

### Option 1: Install Java via Homebrew (Recommended for macOS)
```bash
# Install Homebrew if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java
brew install openjdk@17

# Set JAVA_HOME
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc
```

### Option 2: Install Java via SDKMAN
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java
sdk install java 17.0.9-tem

# Set as default
sdk default java 17.0.9-tem
```

### Option 3: Download from Oracle
1. Visit https://www.oracle.com/java/technologies/downloads/
2. Download Java 17 for macOS
3. Install the package
4. Set JAVA_HOME environment variable

## Verify Java Installation
```bash
java -version
javac -version
echo $JAVA_HOME
```

## Build the App
Once Java is installed, you can build the app:

```bash
# Clean and build
./gradlew clean
./gradlew assembleDebug

# Or run directly on connected device/emulator
./gradlew installDebug
```

## Alternative: Use Android Studio
1. Open the project in Android Studio
2. Let Android Studio handle the Java setup automatically
3. Click "Run" to build and install the app

## App Features
Once the app is running, you'll have access to:

✅ **Welcome Screen** - Beautiful introduction with home emoji logo
✅ **Apartment Details** - Form to input apartment specifications
✅ **Personalized Checklists** - Generated based on your apartment details
✅ **Task Management** - Mark tasks complete, add notes, create sub-tasks
✅ **Progress Tracking** - Visual indicators for completion status

## Troubleshooting

### If you still get Java errors:
1. Make sure JAVA_HOME is set correctly
2. Restart your terminal
3. Try using Android Studio instead of command line

### If build fails:
1. Run `./gradlew clean`
2. Delete `.gradle` folder in project root
3. Run `./gradlew assembleDebug` again

### If app crashes:
1. Check logcat in Android Studio
2. Make sure all dependencies are synced
3. Clean and rebuild the project

## Project Structure
```
app/src/main/java/com/example/movein/
├── MainActivity.kt              # Main app entry point
├── AppState.kt                  # State management
├── data/
│   ├── Models.kt               # Data classes
│   └── ChecklistData.kt        # Checklist generation logic
├── navigation/
│   └── Navigation.kt           # Screen definitions
└── ui/screens/
    ├── WelcomeScreen.kt        # Welcome screen
    ├── ApartmentDetailsScreen.kt # Apartment form
    ├── DashboardScreen.kt      # Main dashboard
    └── TaskDetailScreen.kt     # Task details
```

The app is fully functional and ready to use once Java is properly configured!
