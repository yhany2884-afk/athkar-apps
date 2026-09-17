import SwiftUI

@main
struct AthkarMacApp: App {
    @StateObject private var store = AppStore()
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(store)
                .environment(\.layoutDirection, .rightToLeft)
                .frame(minWidth: 820, minHeight: 560)
        }
        .windowStyle(.automatic)
        .defaultSize(width: 980, height: 680)
        .commands { CommandGroup(replacing: .newItem) {} }
    }
}
