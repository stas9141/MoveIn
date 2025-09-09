import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = MoveInViewModel()
    
    var body: some View {
        NavigationView {
            VStack {
                if viewModel.isLoading {
                    ProgressView("Loading...")
                } else {
                    DashboardView(viewModel: viewModel)
                }
            }
            .navigationTitle("MoveIn")
        }
        .onAppear {
            viewModel.loadData()
        }
    }
}

struct DashboardView: View {
    @ObservedObject var viewModel: MoveInViewModel
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                // Welcome Section
                if let userData = viewModel.userData {
                    WelcomeCard(userData: userData)
                }
                
                // Quick Stats
                StatsView(viewModel: viewModel)
                
                // Defects Section
                DefectsSectionView(viewModel: viewModel)
                
                // Tasks Section
                TasksSectionView(viewModel: viewModel)
            }
            .padding()
        }
    }
}

struct WelcomeCard: View {
    let userData: UserData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Welcome to MoveIn!")
                .font(.title2)
                .fontWeight(.bold)
            
            Text("Your apartment has \(userData.rooms) rooms, \(userData.bathrooms) bathrooms, and \(userData.parking) parking spaces.")
                .font(.body)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

struct StatsView: View {
    @ObservedObject var viewModel: MoveInViewModel
    
    var body: some View {
        HStack(spacing: 16) {
            StatCard(title: "Defects", value: "\(viewModel.defects.count)", color: .red)
            StatCard(title: "Tasks", value: "\(viewModel.totalTasks)", color: .blue)
            StatCard(title: "Completed", value: "\(viewModel.completedTasks)", color: .green)
        }
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack {
            Text(value)
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(color)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}

struct DefectsSectionView: View {
    @ObservedObject var viewModel: MoveInViewModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Defects")
                    .font(.headline)
                Spacer()
                Button("Add Defect") {
                    // Add defect action
                }
                .buttonStyle(.bordered)
            }
            
            if viewModel.defects.isEmpty {
                Text("No defects found")
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
            } else {
                ForEach(viewModel.defects, id: \.id) { defect in
                    DefectCard(defect: defect)
                }
            }
        }
    }
}

struct DefectCard: View {
    let defect: Defect
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(defect.location)
                    .font(.headline)
                Spacer()
                StatusBadge(status: defect.status)
            }
            
            Text(defect.description)
                .font(.body)
                .foregroundColor(.secondary)
                .lineLimit(2)
            
            HStack {
                Text(defect.category.name)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(.systemGray5))
                    .cornerRadius(4)
                
                Spacer()
                
                if !defect.subTasks.isEmpty {
                    Text("\(defect.subTasks.count) tasks")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(8)
        .shadow(color: .black.opacity(0.1), radius: 2, x: 0, y: 1)
    }
}

struct StatusBadge: View {
    let status: DefectStatus
    
    var body: some View {
        Text(status.name)
            .font(.caption)
            .fontWeight(.medium)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(statusColor.opacity(0.2))
            .foregroundColor(statusColor)
            .cornerRadius(4)
    }
    
    private var statusColor: Color {
        switch status {
        case .open:
            return .red
        case .inProgress:
            return .orange
        case .closed:
            return .green
        }
    }
}

struct TasksSectionView: View {
    @ObservedObject var viewModel: MoveInViewModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Tasks")
                .font(.headline)
            
            if viewModel.checklistData == nil {
                Text("Complete onboarding to see your tasks")
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
            } else {
                Text("Your personalized checklist will appear here")
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
            }
        }
    }
}

// MARK: - ViewModel
class MoveInViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var userData: UserData?
    @Published var checklistData: ChecklistData?
    @Published var defects: [Defect] = []
    
    private let defectRepository = DefectRepository()
    private let checklistRepository = ChecklistRepository()
    
    var totalTasks: Int {
        guard let data = checklistData else { return 0 }
        return data.firstWeek.count + data.firstMonth.count + data.firstYear.count
    }
    
    var completedTasks: Int {
        guard let data = checklistData else { return 0 }
        let allTasks = data.firstWeek + data.firstMonth + data.firstYear
        return allTasks.filter { $0.isCompleted }.count
    }
    
    func loadData() {
        isLoading = true
        
        // Load defects
        defects = defectRepository.loadDefects()
        
        // For demo purposes, create sample data
        if userData == nil {
            userData = UserData(
                rooms: 4,
                selectedRoomNames: ["Salon", "Kitchen", "Master Bedroom", "Mamad"],
                bathrooms: 2,
                parking: 1,
                warehouse: true,
                balconies: 2
            )
        }
        
        if checklistData == nil {
            checklistRepository.initializeUserData(userData!)
            checklistData = checklistRepository.checklistData.value
        }
        
        isLoading = false
    }
    
    func addDefect(_ defect: Defect) {
        defectRepository.addDefect(defect)
        defects = defectRepository.defects.value
    }
    
    func updateDefect(_ defect: Defect) {
        defectRepository.updateDefect(defect)
        defects = defectRepository.defects.value
    }
}

#Preview {
    ContentView()
}

