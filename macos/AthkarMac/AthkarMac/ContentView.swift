import SwiftUI
import CoreLocation
import CoreMotion

private let paper = Color(red: 0.965, green: 0.941, blue: 0.894)
private let ink = Color(red: 0.173, green: 0.149, blue: 0.118)
private let muted = Color(red: 0.478, green: 0.443, blue: 0.392)
private let accent = Color(red: 0.604, green: 0.420, blue: 0.227)

struct ContentView: View {
    @EnvironmentObject var store: AppStore
    @State private var splash = true
    var body: some View {
        ZStack {
            TabView {
                HomeView().tabItem { Text("الرئيسية") }
                QuranView().tabItem { Text("المصحف") }
                LibraryView().tabItem { Text("المكتبة") }
                AdhkarView().tabItem { Text("الأذكار") }
                QiblaView().tabItem { Text("القبلة") }
            }
            .tint(accent)
            .toolbarBackground(.ultraThinMaterial, for: .tabBar)
            .background(paper.ignoresSafeArea())
            if splash {
                VStack(spacing: 12) {
                    Text("أذكار اليوم")
                        .font(.system(size: 30, design: .serif))
                        .foregroundStyle(ink)
                        .scaleEffect(splash ? 1 : 1.06)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(paper.ignoresSafeArea())
                .transition(.opacity)
            }
        }
        .animation(.easeOut(duration: 0.4), value: splash)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.72) {
                splash = false
            }
        }
    }
}

struct HomeView: View {
    @EnvironmentObject var store: AppStore
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 10) {
                    Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")
                        .font(.system(size: 22, design: .serif))
                    Text("أذكار اليوم")
                        .font(.system(size: 32, design: .serif))
                        .padding(.bottom, 16)
                    row("المصحف الشريف", "١١٤ سورة — حفص عن عاصم")
                    row("الأذكار", "صباح ومساء وصلاة ونوم")
                    row("المكتبة", "عقيدة وفقه وحديث")
                    row("القبلة", "بوصلة دون إنترنت")
                    Text("حجم الخط").foregroundStyle(muted).padding(.top, 12)
                    Slider(value: $store.fontScale, in: 0.85...1.6)
                }
                .padding(20)
                .foregroundStyle(ink)
                .frame(maxWidth: .infinity)
            }
            .background(paper)
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    func row(_ t: String, _ s: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(t).font(.system(size: 18, design: .serif))
            Text(s).font(.footnote).foregroundStyle(muted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 8)
    }
}

struct QuranView: View {
    @EnvironmentObject var store: AppStore
    var body: some View {
        NavigationStack {
            List(store.quran) { s in
                NavigationLink {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 10) {
                            ForEach(Array(s.verses.enumerated()), id: \.offset) { i, ayah in
                                Text("\(ayah) ﴿\(i + 1)﴾")
                                    .font(.system(size: 22 * store.fontScale, design: .serif))
                                    .lineSpacing(8)
                            }
                        }
                        .padding(20)
                    }
                    .background(paper)
                    .navigationTitle(s.name)
                } label: {
                    HStack {
                        Text("\(s.id). \(s.name)")
                        Spacer()
                        Text("\(s.verses.count)")
                            .foregroundStyle(muted)
                            .font(.footnote)
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(paper)
            .navigationTitle("المصحف الشريف")
        }
    }
}

struct AdhkarView: View {
    @EnvironmentObject var store: AppStore
    var body: some View {
        NavigationStack {
            List(store.categories) { c in
                NavigationLink {
                    List(store.adhkar.filter { $0.categories.contains(c.id) }) { d in
                        NavigationLink {
                            DhikrDetail(d: d)
                        } label: {
                            VStack(alignment: .leading) {
                                Text(d.title).font(.system(size: 17, design: .serif))
                                Text(d.arabic).lineLimit(2).foregroundStyle(muted).font(.footnote)
                            }
                        }
                    }
                    .scrollContentBackground(.hidden)
                    .background(paper)
                    .navigationTitle(c.name)
                } label: {
                    VStack(alignment: .leading) {
                        Text(c.name).font(.system(size: 18, design: .serif))
                        Text(c.blurb).font(.footnote).foregroundStyle(muted)
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(paper)
            .navigationTitle("الأذكار")
        }
    }
}

struct DhikrDetail: View {
    let d: Dhikr
    @EnvironmentObject var store: AppStore
    @State private var left = 0
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(d.arabic)
                    .font(.system(size: 22 * store.fontScale, design: .serif))
                    .lineSpacing(8)
                Text(d.meaning).foregroundStyle(muted)
                Text("\(d.source) — \(d.sourceRef)").font(.footnote).foregroundStyle(muted)
                Button {
                    if left > 0 { left -= 1 }
                } label: {
                    Text("المتبقي \(left) / \(d.count)")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(accent)
                        .foregroundStyle(.white)
                }
            }
            .padding(20)
        }
        .background(paper)
        .navigationTitle(d.title)
        .onAppear { left = d.count }
    }
}

struct LibraryView: View {
    @EnvironmentObject var store: AppStore
    var body: some View {
        NavigationStack {
            List(store.catalog.shelves) { s in
                NavigationLink {
                    List(store.catalog.books.filter { $0.shelf == s.id }) { b in
                        NavigationLink {
                            BookView(info: b)
                        } label: {
                            VStack(alignment: .leading) {
                                Text(b.title).font(.system(size: 17, design: .serif))
                                Text("\(b.author) · \(b.extra)").font(.footnote).foregroundStyle(muted)
                            }
                        }
                    }
                    .scrollContentBackground(.hidden)
                    .background(paper)
                    .navigationTitle(s.name)
                } label: {
                    VStack(alignment: .leading) {
                        Text(s.name).font(.system(size: 18, design: .serif))
                        Text(s.blurb).font(.footnote).foregroundStyle(muted)
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(paper)
            .navigationTitle("المكتبة")
        }
    }
}

struct BookView: View {
    let info: BookInfo
    @EnvironmentObject var store: AppStore
    var body: some View {
        let book = store.book(info)
        List(book.chapters) { ch in
            NavigationLink {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 14) {
                        if let body = ch.body, !body.isEmpty {
                            Text(body).font(.system(size: 18 * store.fontScale, design: .serif))
                        }
                        ForEach(ch.items ?? [], id: \.n) { h in
                            Text("\(h.n). \(h.text)")
                                .font(.system(size: 17 * store.fontScale, design: .serif))
                                .lineSpacing(6)
                        }
                    }
                    .padding(20)
                }
                .background(paper)
                .navigationTitle(ch.title)
            } label: {
                Text(ch.title)
            }
        }
        .scrollContentBackground(.hidden)
        .background(paper)
        .navigationTitle(book.title)
    }
}

final class Locator: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var coord: CLLocationCoordinate2D?
    private let mgr = CLLocationManager()
    override init() {
        super.init()
        mgr.delegate = self
        mgr.requestWhenInUseAuthorization()
        mgr.startUpdatingLocation()
    }
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        coord = locations.last?.coordinate
    }
}

final class Heading: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var degrees: Double = 0
    private let mgr = CLLocationManager()
    override init() {
        super.init()
        mgr.delegate = self
        if CLLocationManager.headingAvailable() {
            mgr.startUpdatingHeading()
        }
    }
    func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        degrees = newHeading.trueHeading >= 0 ? newHeading.trueHeading : newHeading.magneticHeading
    }
}

struct QiblaView: View {
    @EnvironmentObject var store: AppStore
    @StateObject private var loc = Locator()
    @StateObject private var heading = Heading()
    @State private var cityIndex = 0
    var body: some View {
        let city = store.qibla.cities.isEmpty ? City(name: "الجيزة", lat: 30.0131, lng: 31.2089)
            : store.qibla.cities[min(cityIndex, store.qibla.cities.count - 1)]
        let lat = loc.coord?.latitude ?? city.lat
        let lng = loc.coord?.longitude ?? city.lng
        let q = QiblaMath.bearing(lat: lat, lng: lng)
        let km = QiblaMath.distanceKm(lat: lat, lng: lng)
        let rot = QiblaMath.norm360(q - heading.degrees)
        VStack(spacing: 12) {
            Text("القبلة").font(.system(size: 22, design: .serif))
            CompassNeedle(angle: rot)
                .frame(width: 240, height: 240)
            Text("\(Int(q))°").font(.system(size: 36, design: .serif))
            Text("\(Int(km)) كم إلى الكعبة").foregroundStyle(muted)
            if loc.coord == nil {
                Picker("المدينة", selection: $cityIndex) {
                    ForEach(Array(store.qibla.cities.enumerated()), id: \.offset) { i, c in
                        Text(c.name).tag(i)
                    }
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(paper)
        .foregroundStyle(ink)
    }
}

struct CompassNeedle: View {
    let angle: Double
    var body: some View {
        ZStack {
            Circle().stroke(ink, lineWidth: 3)
            Capsule()
                .fill(accent)
                .frame(width: 10, height: 90)
                .offset(y: -45)
                .rotationEffect(.degrees(angle))
            Circle().fill(accent).frame(width: 16, height: 16)
        }
    }
}
