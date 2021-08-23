package eu.kanade.tachiyomi.extension.en.loadingartist

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import uy.kohesive.injekt.injectLazy

class LoadingArtist : HttpSource() {
    override val name = "Loading Artist"

    override val baseUrl = "https://loadingartist.com"

    override val lang = "en"

    override val supportsLatest = false

    private val json: Json by injectLazy()

    private val comicList: MutableList<SManga> = mutableListOf()

    @Serializable
    private data class Comic(
        val url: String,
        val img: String = "",
        val title: String,
        val date: String = "",
        val section: String,
        val keywords: List<String> = listOf()
    )

    // Popular Section (list of comic archives by year)

    // Retrieves the entire comic archive
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/search.json")

    override fun popularMangaParse(response: Response): MangasPage {
        comicList.clear()
        json.parseToJsonElement(response.body!!.string()).jsonObject.forEach {
            val item = json.decodeFromJsonElement<Comic>(it.value)
            if (listOf("comic", "art", "game").any { type -> item.section == type }) {
                comicList.add(
                    SManga.create().apply {
                        setUrlWithoutDomain(item.url)
                        title = item.title
                        thumbnail_url = if (item.img.isEmpty()) null else "$baseUrl${item.img}"
                        artist = "Loading Artist"
                        author = artist
                        description = if (item.date.isEmpty()) null else "Date Published: ${item.date}"
                        genre = item.keywords.joinToString(", ")
                        status = SManga.COMPLETED
                    }
                )
            }
        }
        return MangasPage(comicList, false)
    }

    // Search

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        return Observable.just(
            MangasPage(comicList.filter { it.title.contains(query, true) }, false)
        )
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = throw Exception("Not used")
    override fun searchMangaParse(response: Response): MangasPage = throw Exception("Not used")

    // Details

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return Observable.just(manga)
    }

    override fun mangaDetailsParse(response: Response): SManga = throw Exception("Not used")

    // Chapters

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return Observable.just(
            listOf(
                SChapter.create().apply {
                    setUrlWithoutDomain(manga.url)
                    name = manga.title
                }
            )
        )
    }

    override fun chapterListParse(response: Response): List<SChapter> = throw Exception("Not used")

    // Pages

    override fun pageListParse(response: Response): List<Page> {
        val imageUrl = response.asJsoup().selectFirst("div.main-image-container img")
            .attr("abs:src")
        return listOf(Page(0, response.request.url.toString(), imageUrl))
    }

    override fun imageUrlParse(response: Response): String = throw Exception("Not used")

    override fun latestUpdatesRequest(page: Int): Request = throw Exception("Not used")
    override fun latestUpdatesParse(response: Response): MangasPage = throw Exception("Not used")
}