using System.IO;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace Athkar;

public record Surah(int id, string name, string latin, string type, List<string> verses);
public record Dhikr(string id, string title, string arabic, int count,
    [property: JsonPropertyName("when")] string whenText,
    string sourceKind, string sourceRef, string source, string meaning,
    string? fadl, List<string> categories, bool locked);
public record Category(string id, string name, string blurb,
    [property: JsonPropertyName("when")] string whenText);
public record City(string name, double lat, double lng);
public record Kaaba(double lat, double lng);
public record QiblaFile(Kaaba kaaba, List<City> cities);
public record Shelf(string id, string name, string blurb);
public record BookInfo(string id, string shelf, string title, string author, string blurb, string file, string extra);
public record Catalog(List<Shelf> shelves, List<BookInfo> books);
public record HadithItem(int n, string text);
public record Chapter(string id, string title, string? body, List<HadithItem>? items);
public record Book(string id, string shelf, string title, string author, string blurb, List<Chapter> chapters);

public static class Store
{
    static readonly JsonSerializerOptions Opt = new() { PropertyNameCaseInsensitive = true };
    public static List<Surah> Quran { get; private set; } = [];
    public static List<Dhikr> Adhkar { get; private set; } = [];
    public static List<Category> Categories { get; private set; } = [];
    public static Catalog Catalog { get; private set; } = new([], []);
    public static QiblaFile Qibla { get; private set; } = new(new(21.422487, 39.826206), []);
    static List<Book> Aqidah = [];
    static List<Book> Fiqh = [];
    static readonly Dictionary<string, Book> Cache = [];

    static string Root => Path.Combine(AppContext.BaseDirectory, "data");

    public static void Load()
    {
        Quran = Read<List<Surah>>("quran.json");
        Adhkar = Read<List<Dhikr>>("adhkar.json");
        Categories = Read<List<Category>>("categories.json");
        Catalog = Read<Catalog>("catalog.json");
        Qibla = Read<QiblaFile>("qibla.json");
        Aqidah = Read<List<Book>>("aqidah.json");
        Fiqh = Read<List<Book>>("fiqh.json");
    }

    public static Book Book(BookInfo info)
    {
        if (Cache.TryGetValue(info.id, out var c)) return c;
        Book b = info.file switch
        {
            "aqidah.json" => Aqidah.First(x => x.id == info.id),
            "fiqh.json" => Fiqh.First(x => x.id == info.id),
            _ => Read<Book>(info.file.Replace('/', Path.DirectorySeparatorChar)),
        };
        Cache[info.id] = b;
        return b;
    }

    static T Read<T>(string rel)
    {
        var path = Path.Combine(Root, rel);
        return JsonSerializer.Deserialize<T>(File.ReadAllText(path), Opt)!;
    }
}

public static class QiblaMath
{
    public const double KaabaLat = 21.422487;
    public const double KaabaLng = 39.826206;

    public static double Bearing(double lat, double lng)
    {
        var p1 = lat * Math.PI / 180;
        var p2 = KaabaLat * Math.PI / 180;
        var dl = (KaabaLng - lng) * Math.PI / 180;
        var y = Math.Sin(dl) * Math.Cos(p2);
        var x = Math.Cos(p1) * Math.Sin(p2) - Math.Sin(p1) * Math.Cos(p2) * Math.Cos(dl);
        return Norm360(Math.Atan2(y, x) * 180 / Math.PI);
    }

    public static double DistanceKm(double lat, double lng)
    {
        const double r = 6371.0088;
        var p1 = lat * Math.PI / 180;
        var p2 = KaabaLat * Math.PI / 180;
        var dp = (KaabaLat - lat) * Math.PI / 180;
        var dl = (KaabaLng - lng) * Math.PI / 180;
        var a = Math.Sin(dp / 2) * Math.Sin(dp / 2) + Math.Cos(p1) * Math.Cos(p2) * Math.Sin(dl / 2) * Math.Sin(dl / 2);
        return 2 * r * Math.Asin(Math.Min(1, Math.Sqrt(a)));
    }

    public static double Norm360(double n)
    {
        var m = n % 360;
        return m < 0 ? m + 360 : m;
    }
}
