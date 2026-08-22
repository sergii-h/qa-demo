import SwiftUI

@main
struct DemoApp: App {
    private let repository: TaskRepository

    init() {
        let session = URLSession(configuration: .default)
        let api = APIClient.makeTaskAPI(baseURL: AppConfiguration.apiBaseURL, session: session)
        repository = TaskRepository(api: api)
        AppLocale.shared.initialize()
    }

    private var isRunningTests: Bool {
        ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] != nil
    }

    var body: some Scene {
        WindowGroup {
            if isRunningTests {
                Color.clear
            } else {
                AppRoot(repository: repository)
            }
        }
    }
}

private struct AppRoot: View {
    @Bindable private var appLocale = AppLocale.shared
    let repository: TaskRepositoryProtocol

    var body: some View {
        DemoAppTheme {
            DemoNavigation(repository: repository)
        }
        .environment(\.locale, Locale(identifier: appLocale.languageTag))
        .environment(\.appLocale, appLocale)
        .id(appLocale.languageTag)
    }
}
