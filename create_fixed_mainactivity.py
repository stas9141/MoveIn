#!/usr/bin/env python3

def create_fixed_mainactivity():
    # Read the clean file from git
    with open('/tmp/clean_mainactivity.kt', 'r') as f:
        content = f.read()
    
    print("🔧 Creating new MainActivity.kt with all fixes...")
    
    # 1. Add MyApartment to shouldShowBottomNavigation
    print("  ✅ Adding MyApartment to shouldShowBottomNavigation...")
    content = content.replace(
        '        Screen.Settings,\n        is Screen.TaskDetail,',
        '        Screen.Settings,\n        Screen.MyApartment,\n        is Screen.TaskDetail,'
    )
    
    # 2. Add MyApartment screen handler after ReportConfiguration
    print("  ✅ Adding MyApartment screen handler...")
    myapartment_handler = '''
            Screen.MyApartment -> {
                MyApartmentScreen(
                    userData = appState.userData,
                    onBackClick = {
                        appState.navigateTo(Screen.Settings)
                    },
                    onEditClick = {
                        // Handle edit
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }'''
    
    # Find the ReportConfiguration handler and add MyApartment after it
    import re
    pattern = r'(Screen\.ReportConfiguration -> \{[^}]+\}\s*)\n\s*\}'
    replacement = r'\1' + myapartment_handler + '\n        }'
    content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    # 3. Add missing parameters to SettingsScreen calls
    print("  ✅ Adding missing parameters to SettingsScreen calls...")
    
    # Parameters to add
    settings_params = '''                    onMyApartmentClick = {
                        appState.navigateTo(Screen.MyApartment)
                    },
                    onCreateAccountClick = {
                        appState.navigateTo(Screen.SignUp)
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
                    },
'''
    
    # Find SettingsScreen calls and add parameters before modifier
    pattern = r'(onLogoutAllDevices = \{[^}]+\}\s*,\s*)(modifier = Modifier\.padding\(innerPadding\))'
    replacement = r'\1' + settings_params + r'\2'
    content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    # Write the new content
    with open('app/src/main/java/com/example/movein/MainActivity.kt', 'w') as f:
        f.write(content)
    
    print("🎉 New MainActivity.kt created successfully!")
    print("  - MyApartment added to shouldShowBottomNavigation")
    print("  - MyApartment screen handler added")
    print("  - Missing parameters added to SettingsScreen calls")
    print("  - File is ready to build!")

if __name__ == "__main__":
    create_fixed_mainactivity()


