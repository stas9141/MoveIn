# MoveIn - Your New Home Companion

A comprehensive Android app designed to help users organize their apartment move-in process with personalized checklists.

## Features

### 🏠 Welcome & Onboarding
- **Welcome Screen**: Beautiful introduction with app logo and description
- **Apartment Details Form**: Collect user's apartment specifications
  - Number of rooms (3-6)
  - Number of bathrooms (1-3)
  - Number of parking spaces (1-2)
  - Warehouse availability (yes/no)

### 📋 Personalized Checklists
The app generates customized checklists based on the user's apartment details:

#### First Week Tasks
- Change all locks
- Test all smoke detectors
- Take initial meter readings
- Locate circuit breaker and water shut-off
- Clean all appliances
- Check for leaks
- Test heating and cooling systems
- **Personalized tasks**: Additional bathroom inspections, warehouse organization, parking space verification

#### First Month Tasks
- Update address with important services
- Register with local services
- Meet your neighbors
- Explore the neighborhood
- Check internet and phone reception
- Document any issues
- Plan emergency contacts

#### First Year Tasks
- Review lease renewal
- Annual deep cleaning
- Check for maintenance needs
- Update emergency kit
- Review insurance coverage
- Plan for next move

### 🎯 Task Management
- **Dashboard**: Overview of all tasks with completion status
- **Task Details**: Detailed view with description, notes, and sub-tasks
- **Progress Tracking**: Visual progress indicators for each time period
- **Notes & Sub-tasks**: Add personal notes and break down tasks into smaller steps

## Technical Implementation

### Architecture
- **Jetpack Compose**: Modern UI framework for Android
- **Material Design 3**: Latest design system
- **State Management**: Custom AppState class for managing app state
- **Navigation**: Simple screen-based navigation

### Data Models
- `UserData`: Stores apartment specifications
- `ChecklistItem`: Individual task with title, description, category, and completion status
- `SubTask`: Smaller tasks within main tasks
- `ChecklistData`: Organized collection of tasks by time period

### Key Components
1. **WelcomeScreen**: App introduction and onboarding
2. **ApartmentDetailsScreen**: Form for apartment specifications
3. **DashboardScreen**: Main hub with task lists and progress
4. **TaskDetailScreen**: Detailed task view with notes and sub-tasks

## Getting Started

1. Open the project in Android Studio
2. Sync Gradle dependencies
3. Run the app on an Android device or emulator
4. Follow the onboarding flow to set up your apartment details
5. Start using your personalized checklists!

## Personalization Logic

The app intelligently adds tasks based on your apartment details:
- **Bathrooms**: Adds inspection tasks for each bathroom
- **Warehouse**: Includes organization tasks if you have warehouse space
- **Parking**: Adds parking space verification tasks
- **Rooms**: Considers room count for task customization

## Future Enhancements

- Data persistence with Room database
- Cloud sync for backup and sharing
- Push notifications for task reminders
- Photo documentation for completed tasks
- Export functionality for completed checklists
- Multiple apartment support
- Community tips and recommendations

---

Built with ❤️ using Jetpack Compose and Material Design 3
