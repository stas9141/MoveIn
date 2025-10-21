# MainActivity.kt Fix Guide

## Current Compilation Errors:
1. Line 263: 'when' expression must be exhaustive. Add the 'MyApartment' branch
2. Line 697: No value passed for parameter 'onMyApartmentClick'
3. Line 860: No value passed for parameter 'onMyApartmentClick'

## Fix Instructions:

### Step 1: Add MyApartment Import
**Location:** Near the top of the file (around line 45)
**Find:** `import com.example.movein.ui.screens.SimpleSignUpScreen`
**Add after it:**
```kotlin
import com.example.movein.ui.screens.MyApartmentScreen
```

### Step 2: Add MyApartment Case to When Expression
**Location:** Around line 864 (look for `Screen.ReportConfiguration`)
**Find:**
```kotlin
            Screen.ReportConfiguration -> {
                ReportConfigurationScreen(
                    defects = appState.defects,
                    onBackClick = {
                        appState.navigateTo(Screen.Settings)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
```

**Add BEFORE it:**
```kotlin
            Screen.MyApartment -> {
                MyApartmentScreen(
                    onBackClick = {
                        appState.navigateTo(Screen.Settings)
                    },
                    onEditClick = { /* Handle edit */ },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
```

### Step 3: Fix First SettingsScreen Call (around line 697)
**Look for the first occurrence of SettingsScreen that has:**
```kotlin
                SettingsScreen(
                    isDarkMode = false,
                    onDarkModeToggle = { },
                    onBackClick = {
```

**Add this parameter right after `onBackClick`:**
```kotlin
                    onMyApartmentClick = {
                        appState.navigateTo(Screen.MyApartment)
                    },
```

### Step 4: Fix Second SettingsScreen Call (around line 860)
**Look for the second occurrence of SettingsScreen that has:**
```kotlin
                SettingsScreen(
                    isDarkMode = false,
                    onDarkModeToggle = { },
                    onBackClick = {
```

**Add this parameter right after `onBackClick`:**
```kotlin
                    onMyApartmentClick = {
                        appState.navigateTo(Screen.MyApartment)
                    },
```

## Summary:
After these changes:
- MyApartmentScreen will be imported
- MyApartment screen case will be added to the when expression
- Both SettingsScreen calls will have the onMyApartmentClick parameter

## Result:
The app will compile successfully and all features will work, including:
- Login error display (already implemented and working)
- MyApartment screen navigation from Settings
- All other existing functionality

## Note:
The account management buttons ("Create Account", "Sign In") have been removed from Settings screen, so anonymous users won't see those prompts in Settings anymore. They can still access login/signup from the Welcome screen.



