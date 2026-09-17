using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Shapes;

namespace Athkar;

public partial class MainWindow : Window
{
    string _page = "home";
    double _font = 1;

    public MainWindow()
    {
        InitializeComponent();
        Store.Load();
        ShowHome();
    }

    void OnNav(object sender, RoutedEventArgs e)
    {
        _page = (string)((Button)sender).Tag;
        switch (_page)
        {
            case "home": ShowHome(); break;
            case "quran": ShowQuran(); break;
            case "library": ShowLibrary(); break;
            case "adhkar": ShowAdhkar(); break;
            case "qibla": ShowQibla(); break;
        }
    }

    TextBlock T(string text, double size, Brush? color = null, FontFamily? font = null)
    {
        return new TextBlock
        {
            Text = text,
            FontSize = size,
            Foreground = color ?? (Brush)FindResource("Ink"),
            FontFamily = font ?? new FontFamily("Traditional Arabic, Segoe UI"),
            TextWrapping = TextWrapping.Wrap,
            Margin = new Thickness(0, 0, 0, 8),
        };
    }

    Button Row(string title, string sub, RoutedEventHandler click)
    {
        var b = new Button { HorizontalContentAlignment = HorizontalAlignment.Stretch, Margin = new Thickness(0, 4, 0, 4) };
        var sp = new StackPanel();
        sp.Children.Add(T(title, 18));
        sp.Children.Add(T(sub, 13, (Brush)FindResource("Muted")));
        b.Content = sp;
        b.Click += click;
        return b;
    }

    void Set(UIElement el) => Body.Content = el;

    void ShowHome()
    {
        var sp = new StackPanel();
        var bism = T("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 22);
        bism.TextAlignment = TextAlignment.Center;
        var title = T("أذكار اليوم", 32);
        title.TextAlignment = TextAlignment.Center;
        sp.Children.Add(bism);
        sp.Children.Add(title);
        sp.Children.Add(Row("المصحف الشريف", "١١٤ سورة — حفص عن عاصم", (_, _) => ShowQuran()));
        sp.Children.Add(Row("الأذكار", "صباح ومساء وصلاة ونوم", (_, _) => ShowAdhkar()));
        sp.Children.Add(Row("المكتبة", "عقيدة وفقه وحديث", (_, _) => ShowLibrary()));
        sp.Children.Add(Row("القبلة", "بوصلة دون إنترنت", (_, _) => ShowQibla()));
        sp.Children.Add(T("حجم الخط", 13, (Brush)FindResource("Muted")));
        var sl = new Slider { Minimum = 0.85, Maximum = 1.6, Value = _font, Width = 280, HorizontalAlignment = HorizontalAlignment.Left };
        sl.ValueChanged += (_, e) => _font = e.NewValue;
        sp.Children.Add(sl);
        Set(sp);
    }

    void ShowQuran(Surah? open = null)
    {
        var sp = new StackPanel();
        if (open == null)
        {
            sp.Children.Add(T("المصحف الشريف", 22));
            foreach (var s in Store.Quran)
            {
                var local = s;
                sp.Children.Add(Row($"{s.id}. {s.name}",
                    (s.type == "m" ? "مكية" : "مدنية") + " · " + s.verses.Count,
                    (_, _) => ShowQuran(local)));
            }
        }
        else
        {
            sp.Children.Add(Back(() => ShowQuran()));
            sp.Children.Add(T(open.name, 22));
            for (var i = 0; i < open.verses.Count; i++)
                sp.Children.Add(T($"{open.verses[i]} ﴿{i + 1}﴾", 22 * _font));
        }
        Set(sp);
    }

    void ShowAdhkar(Category? cat = null, Dhikr? d = null, int left = 0)
    {
        var sp = new StackPanel();
        if (d != null)
        {
            sp.Children.Add(Back(() => ShowAdhkar(cat)));
            sp.Children.Add(T(d.title, 20));
            sp.Children.Add(T(d.arabic, 22 * _font));
            sp.Children.Add(T(d.meaning, 14, (Brush)FindResource("Muted")));
            sp.Children.Add(T($"{d.source} — {d.sourceRef}", 12, (Brush)FindResource("Muted")));
            var remain = left;
            var btn = new Button
            {
                Content = $"المتبقي {remain} / {d.count}",
                Background = (Brush)FindResource("Accent"),
                Foreground = Brushes.White,
                Padding = new Thickness(12),
                Margin = new Thickness(0, 16, 0, 0),
            };
            btn.Click += (_, _) =>
            {
                if (remain > 0) remain--;
                btn.Content = $"المتبقي {remain} / {d.count}";
            };
            sp.Children.Add(btn);
        }
        else if (cat != null)
        {
            sp.Children.Add(Back(() => ShowAdhkar()));
            sp.Children.Add(T(cat.name, 22));
            foreach (var item in Store.Adhkar.Where(x => x.categories.Contains(cat.id)))
            {
                var local = item;
                sp.Children.Add(Row(item.title, item.arabic, (_, _) => ShowAdhkar(cat, local, local.count)));
            }
        }
        else
        {
            sp.Children.Add(T("الأذكار", 22));
            foreach (var c in Store.Categories)
            {
                var local = c;
                sp.Children.Add(Row(c.name, c.blurb, (_, _) => ShowAdhkar(local)));
            }
        }
        Set(sp);
    }

    void ShowLibrary(string? shelf = null, Book? book = null, Chapter? ch = null)
    {
        var sp = new StackPanel();
        if (ch != null)
        {
            sp.Children.Add(Back(() => ShowLibrary(shelf, book)));
            sp.Children.Add(T(ch.title, 20));
            if (!string.IsNullOrWhiteSpace(ch.body))
                sp.Children.Add(T(ch.body, 18 * _font));
            foreach (var h in ch.items ?? [])
                sp.Children.Add(T($"{h.n}. {h.text}", 17 * _font));
        }
        else if (book != null)
        {
            sp.Children.Add(Back(() => ShowLibrary(shelf)));
            sp.Children.Add(T(book.title, 22));
            sp.Children.Add(T(book.author, 13, (Brush)FindResource("Muted")));
            foreach (var c in book.chapters)
            {
                var local = c;
                sp.Children.Add(Row(c.title, "", (_, _) => ShowLibrary(shelf, book, local)));
            }
        }
        else if (shelf != null)
        {
            sp.Children.Add(Back(() => ShowLibrary()));
            var sh = Store.Catalog.shelves.First(s => s.id == shelf);
            sp.Children.Add(T(sh.name, 22));
            foreach (var b in Store.Catalog.books.Where(x => x.shelf == shelf))
            {
                var local = b;
                sp.Children.Add(Row(b.title, $"{b.author} · {b.extra}", (_, _) => ShowLibrary(shelf, Store.Book(local))));
            }
        }
        else
        {
            sp.Children.Add(T("المكتبة", 22));
            foreach (var s in Store.Catalog.shelves)
            {
                var local = s;
                sp.Children.Add(Row(s.name, s.blurb, (_, _) => ShowLibrary(local.id)));
            }
        }
        Set(sp);
    }

    void ShowQibla()
    {
        var city = Store.Qibla.cities.FirstOrDefault() ?? new City("الجيزة", 30.0131, 31.2089);
        var sp = new StackPanel { HorizontalAlignment = HorizontalAlignment.Center };
        sp.Children.Add(T("القبلة", 22));
        var angle = QiblaMath.Bearing(city.lat, city.lng);
        var km = QiblaMath.DistanceKm(city.lat, city.lng);
        var canvas = new Canvas { Width = 240, Height = 240, Margin = new Thickness(0, 12, 0, 12) };
        canvas.Children.Add(new Ellipse { Width = 240, Height = 240, Stroke = (Brush)FindResource("Ink"), StrokeThickness = 3 });
        var needle = new Rectangle
        {
            Width = 10,
            Height = 90,
            Fill = (Brush)FindResource("Accent"),
            RenderTransformOrigin = new Point(0.5, 1),
            RenderTransform = new RotateTransform(angle),
        };
        Canvas.SetLeft(needle, 115);
        Canvas.SetTop(needle, 30);
        canvas.Children.Add(needle);
        sp.Children.Add(canvas);
        var deg = T($"{(int)angle}°", 36);
        deg.TextAlignment = TextAlignment.Center;
        sp.Children.Add(deg);
        var dist = T($"{(int)km} كم إلى الكعبة — {city.name}", 14, (Brush)FindResource("Muted"));
        dist.TextAlignment = TextAlignment.Center;
        sp.Children.Add(dist);
        var box = new ComboBox { Width = 220, Margin = new Thickness(0, 12, 0, 0), ItemsSource = Store.Qibla.cities.Select(c => c.name).ToList(), SelectedIndex = 0 };
        box.SelectionChanged += (_, _) =>
        {
            if (box.SelectedItem is string name)
            {
                var c = Store.Qibla.cities.First(x => x.name == name);
                ShowQiblaCity(c);
            }
        };
        sp.Children.Add(box);
        Set(sp);
    }

    void ShowQiblaCity(City city)
    {
        _page = "qibla";
        var sp = new StackPanel { HorizontalAlignment = HorizontalAlignment.Center };
        sp.Children.Add(T("القبلة", 22));
        var angle = QiblaMath.Bearing(city.lat, city.lng);
        var km = QiblaMath.DistanceKm(city.lat, city.lng);
        var deg = T($"{(int)angle}°", 36);
        deg.TextAlignment = TextAlignment.Center;
        sp.Children.Add(deg);
        var dist = T($"{(int)km} كم إلى الكعبة — {city.name}", 14, (Brush)FindResource("Muted"));
        dist.TextAlignment = TextAlignment.Center;
        sp.Children.Add(dist);
        var box = new ComboBox { Width = 220, Margin = new Thickness(0, 12, 0, 0), ItemsSource = Store.Qibla.cities.Select(c => c.name).ToList(), SelectedItem = city.name };
        box.SelectionChanged += (_, _) =>
        {
            if (box.SelectedItem is string name)
                ShowQiblaCity(Store.Qibla.cities.First(x => x.name == name));
        };
        sp.Children.Add(box);
        Set(sp);
    }

    Button Back(Action go)
    {
        var b = new Button { Content = "رجوع", HorizontalAlignment = HorizontalAlignment.Right, Foreground = (Brush)FindResource("Accent") };
        b.Click += (_, _) => go();
        return b;
    }
}
