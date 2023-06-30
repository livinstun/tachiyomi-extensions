package eu.kanade.tachiyomi.extension.en.isekaiscantop

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Page
import okhttp3.CacheControl
import okhttp3.Request
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Locale

class IsekaiScanTop : Madara(
    "IsekaiScan.top (unoriginal)",
    "https://isekaiscan.top",
    "en",
    SimpleDateFormat("MMM dd, HH:mm", Locale.US),
) {
    override val useNewChapterEndpointWithMangaID = true

    override val dateWithoutYear = true

    override fun popularMangaRequest(page: Int): Request {
        return GET(
            url = "$baseUrl/popular-manga?page=$page",
            headers = headers,
            cache = CacheControl.FORCE_NETWORK,
        )
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET(
            url = "$baseUrl/latest-manga?page=$page",
            headers = headers,
            cache = CacheControl.FORCE_NETWORK,
        )
    }

    override fun pageListParse(document: Document): List<Page> {
        val stringArray = document.select("p#arraydata").text().split(",").toTypedArray()
        return stringArray.mapIndexed { index, url ->
            Page(
                index,
                document.location(),
                url,
            )
        }
    }

    override fun searchPage(page: Int): String = "search?page=$page"

    override fun searchMangaNextPageSelector(): String? = "ul.pagination li:last-child a"
}
