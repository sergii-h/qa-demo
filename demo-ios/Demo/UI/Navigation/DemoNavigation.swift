import SwiftUI

enum DemoRoute: Hashable {
    case create
    case edit(String)
    case detail(String)
}

struct DemoNavigation: View {
    let repository: TaskRepositoryProtocol

    @State private var path: [DemoRoute]
    @State private var listRefreshTrigger = 0

    init(repository: TaskRepositoryProtocol, initialPath: [DemoRoute] = []) {
        self.repository = repository
        _path = State(initialValue: initialPath)
    }

    var body: some View {
        NavigationStack(path: $path) {
            TaskListView(
                repository: repository,
                refreshTrigger: listRefreshTrigger,
                onCreateTask: { path.append(.create) },
                onEditTask: { taskId in path.append(.edit(taskId)) },
                onTaskInfo: { taskId in path.append(.detail(taskId)) }
            )
            .navigationDestination(for: DemoRoute.self) { route in
                switch route {
                case .create:
                    TaskFormView(
                        repository: repository,
                        mode: .create,
                        taskId: nil,
                        onSaved: {
                            path.removeLast()
                            listRefreshTrigger += 1
                        }
                    )
                case .edit(let taskId):
                    TaskFormView(
                        repository: repository,
                        mode: .edit,
                        taskId: taskId,
                        onSaved: {
                            path.removeLast()
                            listRefreshTrigger += 1
                        }
                    )
                case .detail(let taskId):
                    TaskDetailView(repository: repository, taskId: taskId)
                }
            }
        }
    }
}
