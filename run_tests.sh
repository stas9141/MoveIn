#!/bin/bash

# MoveIn App Test Runner
# This script runs different types of tests for the MoveIn Android app

set -e  # Exit on any error

echo "🏠 MoveIn App Test Runner"
echo "=========================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is available
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_warning "Please install Java 11+ to run tests"
        print_status "Visit: https://adoptium.net/ for Java installation"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 11 ]; then
        print_error "Java version $JAVA_VERSION is too old. Java 11+ is required."
        exit 1
    fi
    
    print_success "Java $JAVA_VERSION found"
}

# Check if Gradle wrapper exists
check_gradle() {
    if [ ! -f "./gradlew" ]; then
        print_error "Gradle wrapper not found. Please run this script from the project root."
        exit 1
    fi
    
    print_success "Gradle wrapper found"
}

# Run unit tests
run_unit_tests() {
    print_status "Running unit tests..."
    
    if ./gradlew test; then
        print_success "Unit tests passed!"
    else
        print_error "Unit tests failed!"
        return 1
    fi
}

# Run component tests
run_component_tests() {
    print_status "Running component tests..."
    print_warning "Make sure you have an Android device or emulator running"
    
    if ./gradlew connectedAndroidTest; then
        print_success "Component tests passed!"
    else
        print_error "Component tests failed!"
        print_warning "This might be due to no device/emulator being available"
        return 1
    fi
}

# Run all tests
run_all_tests() {
    print_status "Running all tests..."
    
    if ./gradlew check; then
        print_success "All tests passed!"
    else
        print_error "Some tests failed!"
        return 1
    fi
}

# Clean and rebuild
clean_and_rebuild() {
    print_status "Cleaning and rebuilding project..."
    
    ./gradlew clean
    ./gradlew build
    
    print_success "Project cleaned and rebuilt successfully!"
}

# Show test coverage
show_coverage() {
    print_status "Generating test coverage report..."
    
    if ./gradlew jacocoTestReport; then
        print_success "Coverage report generated!"
        print_status "Check: app/build/reports/jacoco/test/html/index.html"
    else
        print_error "Failed to generate coverage report!"
        return 1
    fi
}

# Main function
main() {
    case "${1:-all}" in
        "unit")
            check_java
            check_gradle
            run_unit_tests
            ;;
        "component")
            check_java
            check_gradle
            run_component_tests
            ;;
        "all")
            check_java
            check_gradle
            run_all_tests
            ;;
        "clean")
            check_gradle
            clean_and_rebuild
            ;;
        "coverage")
            check_java
            check_gradle
            show_coverage
            ;;
        "help"|"-h"|"--help")
            echo "Usage: $0 [unit|component|all|clean|coverage|help]"
            echo ""
            echo "Commands:"
            echo "  unit      - Run unit tests only"
            echo "  component - Run component tests only"
            echo "  all       - Run all tests (default)"
            echo "  clean     - Clean and rebuild project"
            echo "  coverage  - Generate test coverage report"
            echo "  help      - Show this help message"
            ;;
        *)
            print_error "Unknown command: $1"
            echo "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
