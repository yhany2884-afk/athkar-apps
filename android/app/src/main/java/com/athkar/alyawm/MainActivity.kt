package com.athkar.alyawm

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val Paper = Color(0xFFF6F0E4)
private val Ink = Color(0xFF2C261E)
private val Muted = Color(0xFF7A7164)
private val Accent = Color(0xFF9A6B3A)
private val Line = Color(0x332C261E)

class MainActivity : ComponentActivity() {
    private val askLocation = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.load(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            askLocation.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                App()
            }
        }
    }
}

private enum class Tab { Home, Quran, Library, Adhkar, Qibla }

@Composable
private fun App() {
    var tab by remember { mutableStateOf(Tab.Home) }
    var fontScale by remember { mutableFloatStateOf(1f) }
    var splash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(720)
        splash = false
    }
    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    Tab.Home -> HomeScreen(
                        onOpen = { tab = it },
                        fontScale = fontScale,
                        onFont = { fontScale = it },
                    )
                    Tab.Quran -> QuranScreen(fontScale)
                    Tab.Library -> LibraryScreen(fontScale)
                    Tab.Adhkar -> AdhkarScreen(fontScale)
                    Tab.Qibla -> QiblaScreen()
                }
            }
            NavigationBar(
                containerColor = Color(0x99FFF9F0),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                val items = listOf(
                    Tab.Home to "الرئيسية",
                    Tab.Quran to "المصحف",
                    Tab.Library to "المكتبة",
                    Tab.Adhkar to "الأذكار",
                    Tab.Qibla to "القبلة",
                )
                items.forEach { (t, label) ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { },
                        label = {
                            Text(
                                label,
                                fontSize = 12.sp,
                                color = if (tab == t) Accent else Muted,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0x33C4A35A),
                        ),
                    )
                }
            }
        }
        if (splash) {
            Box(
                Modifier.fillMaxSize().background(Paper),
                contentAlignment = Alignment.Center,
            ) {
                Text("أذكار اليوم", fontSize = 30.sp, fontFamily = FontFamily.Serif, color = Ink)
            }
        }
    }
}

@Composable
private fun HomeScreen(onOpen: (Tab) -> Unit, fontScale: Float, onFont: (Float) -> Unit) {
    val rows = listOf(
        Triple("المصحف الشريف", "١١٤ سورة — حفص عن عاصم", Tab.Quran),
        Triple("أذكار الصباح والمساء", "حصن اليوم من الفجر إلى العشاء", Tab.Adhkar),
        Triple("المكتبة", "عقيدة وفقه وحديث", Tab.Library),
        Triple("القبلة", "بوصلة دون إنترنت", Tab.Qibla),
    )
    LazyColumn(contentPadding = PaddingValues(20.dp)) {
        item {
            Text(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "أذكار اليوم",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                color = Ink,
            )
            Spacer(Modifier.height(24.dp))
        }
        itemsIndexed(rows) { i, row ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(row.third) }
                    .padding(vertical = 12.dp),
            ) {
                Text("${i + 1}  ${row.first}", fontSize = 18.sp, color = Ink, fontFamily = FontFamily.Serif)
                Text(row.second, fontSize = 13.sp, color = Muted)
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("حجم الخط", color = Muted, fontSize = 13.sp)
            Slider(value = fontScale, onValueChange = onFont, valueRange = 0.85f..1.6f)
        }
    }
}

@Composable
private fun QuranScreen(fontScale: Float) {
    var open by remember { mutableStateOf<Surah?>(null) }
    val surah = open
    if (surah == null) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item { Header("المصحف الشريف") }
            items(Store.quran, key = { it.id }) { s ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { open = s }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${s.id}. ${s.name}", fontSize = 18.sp, color = Ink, fontFamily = FontFamily.Serif)
                    Text(
                        if (s.type == "m") "مكية · ${s.verses.size}" else "مدنية · ${s.verses.size}",
                        color = Muted,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { open = null }) { Text("رجوع", color = Accent) }
                Text(
                    surah.name,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    color = Ink,
                )
            }
            LazyColumn(contentPadding = PaddingValues(20.dp)) {
                itemsIndexed(surah.verses) { i, ayah ->
                    Text(
                        "$ayah ﴿${i + 1}﴾",
                        fontSize = (22 * fontScale).sp,
                        lineHeight = (38 * fontScale).sp,
                        fontFamily = FontFamily.Serif,
                        color = Ink,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdhkarScreen(fontScale: Float) {
    var cat by remember { mutableStateOf<Category?>(null) }
    var open by remember { mutableStateOf<Dhikr?>(null) }
    var remaining by remember { mutableIntStateOf(0) }
    when {
        open != null -> {
            val d = open!!
            Column(Modifier.padding(20.dp).fillMaxSize()) {
                TextButton(onClick = { open = null }) { Text("رجوع", color = Accent) }
                Text(d.title, fontSize = 20.sp, fontFamily = FontFamily.Serif, color = Ink)
                Spacer(Modifier.height(12.dp))
                Text(
                    d.arabic,
                    fontSize = (22 * fontScale).sp,
                    lineHeight = (36 * fontScale).sp,
                    fontFamily = FontFamily.Serif,
                    color = Ink,
                )
                Spacer(Modifier.height(12.dp))
                Text(d.meaning, color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("${d.source} — ${d.sourceRef}", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Accent)
                        .clickable {
                            if (remaining > 0) remaining -= 1
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("المتبقي $remaining / ${d.count}", color = Color.White, fontSize = 18.sp)
                }
            }
        }
        cat != null -> {
            val list = Store.adhkar.filter { cat!!.id in it.categories }
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    TextButton(onClick = { cat = null }) { Text("رجوع", color = Accent) }
                    Header(cat!!.name)
                }
                items(list, key = { it.id }) { d ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                open = d
                                remaining = d.count
                            }
                            .padding(vertical = 10.dp),
                    ) {
                        Text(d.title, fontSize = 17.sp, color = Ink, fontFamily = FontFamily.Serif)
                        Text(d.arabic, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }
        else -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item { Header("الأذكار") }
                items(Store.categories, key = { it.id }) { c ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { cat = c }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(c.name, fontSize = 18.sp, color = Ink, fontFamily = FontFamily.Serif)
                        Text(c.blurb, fontSize = 13.sp, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(fontScale: Float) {
    val ctx = LocalContext.current
    var shelf by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<BookInfo?>(null) }
    var book by remember { mutableStateOf<Book?>(null) }
    var chapter by remember { mutableStateOf<Chapter?>(null) }
    when {
        chapter != null -> {
            val ch = chapter!!
            Column(Modifier.fillMaxSize()) {
                TextButton(onClick = { chapter = null }) { Text("رجوع", color = Accent) }
                Text(ch.title, Modifier.padding(horizontal = 16.dp), fontSize = 18.sp, color = Ink)
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    if (!ch.body.isNullOrBlank()) {
                        item { Text(ch.body!!, fontSize = (18 * fontScale).sp, color = Ink, fontFamily = FontFamily.Serif) }
                    }
                    items(ch.items.orEmpty()) { h ->
                        Text(
                            "${h.n}. ${h.text}",
                            fontSize = (17 * fontScale).sp,
                            lineHeight = (30 * fontScale).sp,
                            color = Ink,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.padding(bottom = 14.dp),
                        )
                    }
                }
            }
        }
        book != null -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    TextButton(onClick = { book = null; info = null }) { Text("رجوع", color = Accent) }
                    Header(book!!.title)
                    Text(book!!.author, color = Muted, fontSize = 13.sp)
                }
                items(book!!.chapters, key = { it.id }) { ch ->
                    Text(
                        ch.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chapter = ch }
                            .padding(vertical = 10.dp),
                        fontSize = 16.sp,
                        color = Ink,
                    )
                }
            }
        }
        shelf != null -> {
            val list = Store.catalog.books.filter { it.shelf == shelf }
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    TextButton(onClick = { shelf = null }) { Text("رجوع", color = Accent) }
                    Header(Store.catalog.shelves.first { it.id == shelf }.name)
                }
                items(list, key = { it.id }) { b ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                info = b
                                book = Store.book(ctx, b)
                            }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(b.title, fontSize = 18.sp, color = Ink, fontFamily = FontFamily.Serif)
                        Text("${b.author} · ${b.extra}", fontSize = 12.sp, color = Muted)
                    }
                }
            }
        }
        else -> {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item { Header("المكتبة") }
                items(Store.catalog.shelves, key = { it.id }) { s ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { shelf = s.id }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(s.name, fontSize = 18.sp, color = Ink, fontFamily = FontFamily.Serif)
                        Text(s.blurb, fontSize = 13.sp, color = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun QiblaScreen() {
    val ctx = LocalContext.current
    var city by remember { mutableStateOf(Store.qibla.cities.first()) }
    var heading by remember { mutableFloatStateOf(0f) }
    val loc = remember {
        runCatching {
            val lm = ctx.getSystemService(LocationManager::class.java)
            val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            if (fine) {
                lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null
        }.getOrNull()
    }
    val lat = loc?.latitude ?: city.lat
    val lng = loc?.longitude ?: city.lng
    val qibla = QiblaMath.bearing(lat, lng)
    val km = QiblaMath.distanceKm(lat, lng)
    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(SensorManager::class.java)
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        val listener = object : SensorEventListener {
            private val r = FloatArray(9)
            private val o = FloatArray(3)
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(r, e.values)
                    SensorManager.getOrientation(r, o)
                    heading = Math.toDegrees(o[0].toDouble()).toFloat()
                } else {
                    heading = e.values[0]
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sensor?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        onDispose { sm.unregisterListener(listener) }
    }
    val rotate = QiblaMath.norm360(qibla - heading)
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header("القبلة")
        Canvas(Modifier.size(260.dp).padding(12.dp)) {
            val c = Offset(size.width / 2, size.height / 2)
            val rad = min(size.width, size.height) / 2 - 8
            drawCircle(Ink, rad, c, style = Stroke(width = 3f))
            val ang = Math.toRadians((rotate - 90).toDouble())
            val tip = Offset(
                c.x + (rad * 0.82 * cos(ang)).toFloat(),
                c.y + (rad * 0.82 * sin(ang)).toFloat(),
            )
            drawLine(Accent, c, tip, strokeWidth = 10f, cap = StrokeCap.Round)
            drawCircle(Accent, 10f, c)
        }
        Text("${qibla.toInt()}°", fontSize = 36.sp, color = Ink, fontFamily = FontFamily.Serif)
        Text("${km.toInt()} كم إلى الكعبة", color = Muted)
        Spacer(Modifier.height(12.dp))
        if (loc == null) {
            Text("اختر المدينة", color = Muted, fontSize = 13.sp)
            LazyColumn(Modifier.height(220.dp)) {
                items(Store.qibla.cities) { c ->
                    Text(
                        c.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { city = c }
                            .padding(8.dp),
                        color = if (c.name == city.name) Accent else Ink,
                    )
                }
            }
        } else {
            Text("من موقع الجهاز", color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun Header(title: String) {
    Text(
        title,
        fontSize = 22.sp,
        fontFamily = FontFamily.Serif,
        color = Ink,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
