package com.athkar.alyawm

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Surah(
    val id: Int,
    val name: String,
    val latin: String,
    val type: String,
    val verses: List<String>,
)

data class Dhikr(
    val id: String,
    val title: String,
    val arabic: String,
    val count: Int,
    @SerializedName("when") val whenText: String,
    val sourceKind: String,
    val sourceRef: String,
    val source: String,
    val meaning: String,
    val fadl: String? = null,
    val categories: List<String>,
    val locked: Boolean,
)

data class Category(
    val id: String,
    val name: String,
    val blurb: String,
    @SerializedName("when") val whenText: String,
)

data class City(val name: String, val lat: Double, val lng: Double)

data class QiblaFile(val kaaba: CityKaaba, val cities: List<City>)
data class CityKaaba(val lat: Double, val lng: Double)

data class Catalog(val shelves: List<Shelf>, val books: List<BookInfo>)
data class Shelf(val id: String, val name: String, val blurb: String)
data class BookInfo(
    val id: String,
    val shelf: String,
    val title: String,
    val author: String,
    val blurb: String,
    val file: String,
    val extra: String,
)

data class Book(
    val id: String,
    val shelf: String,
    val title: String,
    val author: String,
    val blurb: String,
    val chapters: List<Chapter>,
)

data class Chapter(
    val id: String,
    val title: String,
    val body: String? = null,
    val items: List<HadithItem>? = null,
)

data class HadithItem(val n: Int, val text: String)

object Store {
    private val gson = Gson()
    lateinit var quran: List<Surah>
        private set
    lateinit var adhkar: List<Dhikr>
        private set
    lateinit var categories: List<Category>
        private set
    lateinit var catalog: Catalog
        private set
    lateinit var qibla: QiblaFile
        private set
    private var aqidah: List<Book> = emptyList()
    private var fiqh: List<Book> = emptyList()
    private val bookCache = mutableMapOf<String, Book>()

    fun load(ctx: Context) {
        quran = gson.fromJson(asset(ctx, "quran.json"), object : TypeToken<List<Surah>>() {}.type)
        adhkar = gson.fromJson(asset(ctx, "adhkar.json"), object : TypeToken<List<Dhikr>>() {}.type)
        categories = gson.fromJson(asset(ctx, "categories.json"), object : TypeToken<List<Category>>() {}.type)
        catalog = gson.fromJson(asset(ctx, "catalog.json"), Catalog::class.java)
        qibla = gson.fromJson(asset(ctx, "qibla.json"), QiblaFile::class.java)
        aqidah = gson.fromJson(asset(ctx, "aqidah.json"), object : TypeToken<List<Book>>() {}.type)
        fiqh = gson.fromJson(asset(ctx, "fiqh.json"), object : TypeToken<List<Book>>() {}.type)
    }

    fun book(ctx: Context, info: BookInfo): Book {
        bookCache[info.id]?.let { return it }
        if (info.file == "aqidah.json") {
            return aqidah.first { it.id == info.id }.also { bookCache[info.id] = it }
        }
        if (info.file == "fiqh.json") {
            return fiqh.first { it.id == info.id }.also { bookCache[info.id] = it }
        }
        val b: Book = gson.fromJson(asset(ctx, info.file), Book::class.java)
        bookCache[info.id] = b
        return b
    }

    private fun asset(ctx: Context, name: String): String =
        ctx.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
