import Foundation
import Combine

struct Surah: Codable, Identifiable {
    let id: Int
    let name: String
    let latin: String
    let type: String
    let verses: [String]
}

struct Dhikr: Codable, Identifiable {
    let id: String
    let title: String
    let arabic: String
    let count: Int
    let `when`: String
    let sourceKind: String
    let sourceRef: String
    let source: String
    let meaning: String
    let fadl: String?
    let categories: [String]
    let locked: Bool
}

struct Category: Codable, Identifiable {
    let id: String
    let name: String
    let blurb: String
    let `when`: String
}

struct City: Codable, Identifiable {
    var id: String { name }
    let name: String
    let lat: Double
    let lng: Double
}

struct QiblaFile: Codable {
    let kaaba: Kaaba
    let cities: [City]
    struct Kaaba: Codable { let lat: Double; let lng: Double }
}

struct Catalog: Codable {
    let shelves: [Shelf]
    let books: [BookInfo]
}

struct Shelf: Codable, Identifiable {
    let id: String
    let name: String
    let blurb: String
}

struct BookInfo: Codable, Identifiable {
    let id: String
    let shelf: String
    let title: String
    let author: String
    let blurb: String
    let file: String
    let extra: String
}

struct Book: Codable, Identifiable {
    let id: String
    let shelf: String
    let title: String
    let author: String
    let blurb: String
    let chapters: [Chapter]
}

struct Chapter: Codable, Identifiable {
    let id: String
    let title: String
    let body: String?
    let items: [HadithItem]?
}

struct HadithItem: Codable {
    let n: Int
    let text: String
}

final class AppStore: ObservableObject {
    @Published var quran: [Surah] = []
    @Published var adhkar: [Dhikr] = []
    @Published var categories: [Category] = []
    @Published var catalog = Catalog(shelves: [], books: [])
    @Published var qibla = QiblaFile(kaaba: .init(lat: 21.422487, lng: 39.826206), cities: [])
    @Published var fontScale: Double = 1
    private var aqidah: [Book] = []
    private var fiqh: [Book] = []
    private var cache: [String: Book] = [:]

    init() { load() }

    func load() {
        quran = decode("quran", in: "data")
        adhkar = decode("adhkar", in: "data")
        categories = decode("categories", in: "data")
        catalog = decode("catalog", in: "data")
        qibla = decode("qibla", in: "data")
        aqidah = decode("aqidah", in: "data")
        fiqh = decode("fiqh", in: "data")
    }

    func book(_ info: BookInfo) -> Book {
        if let c = cache[info.id] { return c }
        let b: Book
        if info.file == "aqidah.json" {
            b = aqidah.first { $0.id == info.id }!
        } else if info.file == "fiqh.json" {
            b = fiqh.first { $0.id == info.id }!
        } else {
            let name = (info.file as NSString).lastPathComponent.replacingOccurrences(of: ".json", with: "")
            b = decode(name, in: "data/library")
        }
        cache[info.id] = b
        return b
    }

    private func decode<T: Decodable>(_ name: String, in sub: String) -> T {
        let url = Bundle.main.url(forResource: name, withExtension: "json", subdirectory: sub)
            ?? Bundle.main.url(forResource: name, withExtension: "json")
        guard let url else { fatalError("missing \(sub)/\(name).json") }
        return try! JSONDecoder().decode(T.self, from: Data(contentsOf: url))
    }
}

enum QiblaMath {
    static let kaabaLat = 21.422487
    static let kaabaLng = 39.826206

    static func bearing(lat: Double, lng: Double) -> Double {
        let p1 = lat * .pi / 180
        let p2 = kaabaLat * .pi / 180
        let dl = (kaabaLng - lng) * .pi / 180
        let y = sin(dl) * cos(p2)
        let x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return norm360(atan2(y, x) * 180 / .pi)
    }

    static func distanceKm(lat: Double, lng: Double) -> Double {
        let r = 6371.0088
        let p1 = lat * .pi / 180
        let p2 = kaabaLat * .pi / 180
        let dp = (kaabaLat - lat) * .pi / 180
        let dl = (kaabaLng - lng) * .pi / 180
        let a = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
        return 2 * r * asin(min(1, sqrt(a)))
    }

    static func norm360(_ n: Double) -> Double {
        let m = n.truncatingRemainder(dividingBy: 360)
        return m < 0 ? m + 360 : m
    }
}
