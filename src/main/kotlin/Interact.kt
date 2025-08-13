
import RandomStringGenerator.generateContentPlaybackNonce
import RandomStringGenerator.generateTParameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.regex.Pattern

class Interact {
    data class RequestVariant(
        val data: JSONObject,
        val query: Map<String, String>,
        val headers: Map<String, String>
    )
    fun downloadImage(urlString: String, outputFile: File): Boolean {
        return try {
            val url = URL(urlString)
            url.openStream().use { input: InputStream ->
                Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private val variants = listOf(
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "ANDROID")
                        put("clientVersion", "19.07.35")
                        put("androidSdkVersion", 30)
                        put("hl", "en")
                        put("timeZone", "UTC")
                    })
                })
                put("playbackContext", JSONObject().apply {
                    put("contentPlaybackContext", JSONObject().apply {
                        put("html5Preference", "HTML5_PREF_WANTS")

                    })
                })
            },
            headers =mapOf(
                "Content-Type" to "application/json",
                "X-YouTube-Client-Name" to "3",  // 3 = ANDROID
                "X-YouTube-Client-Version" to "19.07.35",
                "Origin" to "https://www.youtube.com",
                "User-Agent" to "com.google.android.youtube/19.07.35 (Linux; U; Android 11) gzip"
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"),
            ),

        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20250613.00.00")
                        put("hl", "en")
                    })
                })
            },
            headers = mapOf(
                "Content-Type" to "application/json",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
                "Origin" to "https://www.youtube.com",
                "X-Youtube-Client-Name" to "1",
                "X-Youtube-Client-Version" to "2.20250613.00.00"
            ),
            query = mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")

        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "ANDROID_EMBEDDED_PLAYER",
                        "clientVersion" to "17.31.35",
                        "androidSdkVersion" to 30,
                        "userAgent" to "com.google.android.youtube/17.31.35 (Linux; U; Android 11) gzip",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    ),
                    "thirdParty" to mapOf("embedUrl" to "https://www.youtube.com/")
                )))
            },
            headers =mapOf(
                "X-YouTube-Client-Name" to "55",
                "X-YouTube-Client-Version" to "17.31.35",
                "Origin" to "https://www.youtube.com",
                "content-type" to "application/json"
            ),
            query =mapOf("key" to "AIzaSyCjc_pVEDi4qsv5MtC2dMXzpIaDoRFLsxw")
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                        "clientVersion" to "2.0",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    ),
                    "thirdParty" to mapOf("embedUrl" to "https://www.youtube.com/")
                )))
            },
            headers =mapOf(
                "X-YouTube-Client-Name" to "85",
                "X-YouTube-Client-Version" to "2.0",
                "Origin" to "https://www.youtube.com",
                "content-type" to "application/json"
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        )
        ,RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "WEB_CREATOR",
                        "clientVersion" to "1.20220726.00.00",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    )
                )))
            },
            headers =mapOf(
                "X-YouTube-Client-Name" to "62",
                "X-YouTube-Client-Version" to "1.20220726.00.00",
                "Origin" to "https://www.youtube.com",
                "content-type" to "application/json",
                "User-Agent" to "Mozilla/5.0"
            ),
            query = mapOf("key" to "AIzaSyBUPetSUmoZL-OhlxA7wSac5XinrygCqMo")
        ),RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "MWEB",
                        "clientVersion" to "2.20220801.00.00",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    ),
                    "thirdParty" to mapOf("embedUrl" to "https://www.youtube.com/")
                )))
            },
            headers =mapOf(
                "X-YouTube-Client-Name" to "2",
                "X-YouTube-Client-Version" to "2.20220801.00.00",
                "Origin" to "https://www.youtube.com",
                "content-type" to "application/json",
            ),
            query = mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "ANDROID_CREATOR",
                        "clientVersion" to "22.30.100",
                        "androidSdkVersion" to 30,
                        "userAgent" to "com.google.android.apps.youtube.creator/22.30.100 (Linux; U; Android 11) gzip",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    )
                )))
            },
            headers =mapOf(
                "X-YouTube-Client-Name" to "14",
                "X-YouTube-Client-Version" to "22.30.100",
                "content-type" to "application/json",
                "Origin" to "https://www.youtube.com",
            ),
            query =mapOf("key" to "AIzaSyD_qjV8zaaUMehtLkrKFgVeSX_Iqbtyws8")
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "IOS_CREATOR",
                        "clientVersion" to "22.33.101",
                        "deviceModel" to "iPhone14,3",
                        "userAgent" to "com.google.ios.ytcreator/22.33.101 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    )
                )))
            },
            headers = mapOf(
                "X-YouTube-Client-Name" to "15",
                "X-YouTube-Client-Version" to "22.33.101",
                "userAgent" to "com.google.ios.ytcreator/22.33.101 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)",
                "content-type" to "application/json",
                "Origin" to "https://www.youtube.com",
            ),
            query =mapOf("key" to "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8")
        ),RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "IOS_MESSAGES_EXTENSION",
                        "clientVersion" to "17.33.2",
                        "deviceModel" to "iPhone14,3",
                        "userAgent" to "com.google.ios.youtube/17.33.2 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    ),
                    "thirdParty" to mapOf("embedUrl" to "https://www.youtube.com/")
                )))
            },
            headers = mapOf(
                "X-YouTube-Client-Name" to "66",
                "X-YouTube-Client-Version" to "17.33.2",
                "userAgent" to "com.google.ios.youtube/17.33.2 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)",
                "content-type" to "application/json",
                "Origin" to "https://www.youtube.com",
            ),
            query = mapOf("key" to "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8")
        ),RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "IOS",
                        "clientVersion" to "19.45.4",
                        "deviceMake" to "Apple",
                        "deviceModel" to "iPhone16,2",
                        "userAgent" to "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X;)",
                        "osName" to "iPhone",
                        "osVersion" to "18.1.0.22B83",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    )
                )))
            },
            headers = mapOf(
                "X-YouTube-Client-Name" to "5",
                "X-YouTube-Client-Version" to "19.45.4",
                "userAgent" to "com.google.ios.youtube/19.45.4 (iPhone16,2; U; CPU iOS 18_1_0 like Mac OS X;)",
                "content-type" to "application/json",
                "Origin" to "https://www.youtube.com",
                "X-Goog-Visitor-Id" to "CgtNY2N4RFlyYTFrNCjE-q68BjIKCgJJThIEGgAgaA%3D%3D"
            ),
            query =mapOf("key" to "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8")
        ), RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "WEB_REMIX",
                        "clientVersion" to "1.20220727.01.00"
                    )
                )))
            },
            headers =mapOf(
                "Origin" to "https://www.youtube.com",
                "Content-Type" to "application/json",
                "User-Agent" to "Mozilla/5.0",
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        ),RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "ANDROID_MUSIC",
                        "clientVersion" to "7.11.50",
                        "androidSdkVersion" to 30
                    )
                )))
            },
            headers =mapOf(
                "Content-Type" to "application/json",
                "User-Agent" to "com.google.android.apps.youtube.music/",
                "Origin" to "https://www.youtube.com",
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        ), RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "IOS_MUSIC",
                        "clientVersion" to "5.21",
                        "deviceModel" to "iPhone14,3"
                    )
                )))
            },
            headers =mapOf(
                "Content-Type" to "application/json",
                "User-Agent" to "com.google.ios.youtubemusic/",
                "Origin" to "https://www.youtube.com",
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"),
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "WEB",
                        "clientVersion" to "2.20240726.00.00",
                        "userAgent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.5 Safari/605.1.15,gzip(gfe)",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    )
                )))

            },
            headers = mapOf(
                "Content-Type" to "application/json",
                "Origin" to "https://www.youtube.com",
                "X-YouTube-Client-Name" to "1",
                "clientVersion" to "2.20240726.00.00",
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"),
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "WEB_EMBEDDED_PLAYER",
                        "clientVersion" to "1.20241009.01.00",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    )
                )))

            },
            headers = mapOf(
                "Origin" to "https://www.youtube.com",
                "X-YouTube-Client-Name" to "56",
                "X-YouTube-Client-Version" to "1.20241009.01.00",
                "Content-Type" to "application/json",
                "userAgent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36,gzip(gfe)"
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"),
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "ANDROID_VR",/*not uses descipher*/
                        "clientVersion" to "1.60.19",
                        "deviceMake" to "Oculus",
                        "deviceModel" to "Quest 3",
                        "androidSdkVersion" to 32,
                        "osName" to "Android",
                        "osVersion" to "12L",
                        "userAgent" to "com.google.android.apps.youtube.vr.oculus/1.57.29 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
                    )
                )))
            },
            headers =mapOf(
                "Content-Type" to "application/json",
                "userAgent" to "com.google.android.apps.youtube.vr.oculus/1.57.29 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip",
                "Origin" to "https://www.youtube.com",
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        ),
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "TVHTML5",
                        "clientVersion" to "7.20250122.15.00",
                        "hl" to "en-GB",
                        "gl" to "GB",
                        "deviceMake" to "Sony",
                        "deviceModel" to "PlayStation 4",
                        "clientScreen" to "WATCH",
                        "osName" to "PlayStation 4",
                        "platform" to "GAME_CONSOLE",
                        "utcOffsetMinutes" to 0
                    ),
                    "request" to mapOf(
                        "internalExperimentFlags" to emptyList<Any>(),
                        "useSsl" to true
                    ),
                    "user" to mapOf(
                        "lockedSafetyMode" to false
                    )
                )))
            },
            headers = mapOf(
                "X-YouTube-Client-Name" to "7",
                "X-YouTube-Client-Version" to "7.20250122.15.00",
                "Origin" to "https://www.youtube.com",
                "Content-Type" to "application/json"
            ),
            query = mapOf(
                "key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
            )
        )
        ,
        RequestVariant(
            data = JSONObject().apply {
                put("context", JSONObject(mapOf(
                    "client" to mapOf(
                        "clientName" to "MEDIA_CONNECT_FRONTEND",
                        "clientVersion" to "0.1",
                        "hl" to "en",
                        "timeZone" to "UTC",
                        "utcOffsetMinutes" to 0
                    ),
                )))
            },
            headers =mapOf(
                "X-YouTube-Client-Name" to "97",
                "Origin" to "https://www.youtube.com",
                "content-type" to "application/json"
            ),
            query =mapOf("key" to "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        )
    )



    private fun encodeParams(params: Map<String, Any>): String {
        return params.entries.joinToString("&") { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value.toString(), "UTF-8")}" }
    }
    suspend fun getStreamingData(videoId: String): JSONObject = withContext(Dispatchers.IO) {
        val requestResponse = JSONObject()
        val cpn = generateContentPlaybackNonce()
        val tp = generateTParameter()
        val visitorData = getVisitorId()
        val request = androidPlayerResponse(cpn, visitorData, videoId, tp)
        val client = OkHttpClient()

        try {
            val response = client.newCall(request).execute()
            if (response.code == 200) {
                val responseString = response.body?.string()
                JSONObject(responseString)
            } else {
                requestResponse.put("error", "Returning fail: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            requestResponse.put("error", e.message ?: "Unknown error")
        }
    }
    private fun getTitle(source: JSONObject): String? {
        return source.getJSONArray("runs").getJSONObject(0).getString("text")

    }
    private fun getDuration(source: JSONObject): String? {
        return source.getJSONArray("runs").getJSONObject(0).getString("text")
    }
    private fun getContinuation(source: JSONObject): String? {
       return source.getJSONObject("continuationEndpoint").getJSONObject("continuationCommand").getString("token")

    }
    fun videoId(url: String): String? {
        val regex = """^.*(?:(?:youtu\.be\/|v\/|vi\/|u\/\w\/|embed\/|shorts\/|live\/)|(?:(?:watch)?\?v(?:i)?=|\&v(?:i)?=))([^#\&\?]*).*""".toRegex()
        val matchResult = regex.find(url)
        if (matchResult != null) {
            val videoId = matchResult.groupValues[1]
            return videoId
        }
        return null
    }
    fun getThumbnail(source: JSONObject): String? {
        val thumbs=source.getJSONArray("thumbnails")
        if (thumbs.length()>2){
            return thumbs.getJSONObject(1).getString("url")
        }
        return source.getJSONArray("thumbnails").getJSONObject(0).getString("url")
    }
    fun txt2filename(txt: String): String {
        val specialCharacters = listOf(
            "@", "#", "$", "*", "&", "<", ">", "/", "\\b", "|", "?", "CON", "PRN", "AUX", "NUL",
            "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT0",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", ":", "\"", "'"
        )

        var normalString = txt
        for (sc in specialCharacters) {
            normalString = normalString.replace(sc, "")
        }

        return normalString
    }
    fun  getshelfRenderer(source: JSONObject): MutableList<JSONObject> {
        val itemsVideo = mutableListOf<JSONObject>()
        val vitems=source.getJSONObject("shelfRenderer").getJSONObject("content")
        if (vitems.has("verticalListRenderer")){
            println("verticalListRenderer")
            val ivds=vitems.getJSONObject("verticalListRenderer").getJSONArray("items")
            for (z in 0 until ivds.length()) {
                val ite=JSONObject()
                val rl=ivds.getJSONObject(z)
                ite.put("videoId",rl.getJSONObject("videoRenderer").getString("videoId"))
                ite.put("title",txt2filename(rl.getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text")))
                if (rl.getJSONObject("videoRenderer").has("lengthText")){
                    ite.put("duration",rl.getJSONObject("videoRenderer").getJSONObject("lengthText").get("simpleText").toString())
                }else{
                    ite.put("duration","Unknown")
                }
                ite.put("thumbnail",rl.getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url"))
                itemsVideo.add(ite)

            }
        }
        if (vitems.has("horizontalListRenderer")){
            println("horizontalListRenderer")
            val hzi=vitems.getJSONObject("horizontalListRenderer").getJSONArray("items")
            for (l in 0 until hzi.length()) {
                val ite=JSONObject()
                val rl=hzi.getJSONObject(l)
                if (rl.has("gridVideoRenderer")){
                    ite.put("videoId",rl.getJSONObject("gridVideoRenderer").getString("videoId"))
                    ite.put("title",txt2filename(rl.getJSONObject("gridVideoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text")))
                    if (rl.getJSONObject("gridVideoRenderer").has("lengthText")){
                        ite.put("duration",rl.getJSONObject("gridVideoRenderer").getJSONObject("lengthText").get("simpleText").toString())
                    }else{
                        ite.put("duration","Unknown")
                    }
                    ite.put("thumbnail",rl.getJSONObject("gridVideoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url"))
                    itemsVideo.add(ite)
                }
            }
        }
         return itemsVideo


    }
    fun getreelShelfRenderer(source: JSONObject): MutableList<JSONObject> {
        val itemsVideo = mutableListOf<JSONObject>()
        val reels = source.getJSONObject("reelShelfRenderer").getJSONArray("items")

        for (reel in 0 until reels.length()) {
            val rl = reels.getJSONObject(reel)

            val ite = JSONObject()

            when {
                rl.has("reelItemRenderer") -> {
                    val reelItem = rl.getJSONObject("reelItemRenderer")
                    ite.put("videoId", reelItem.getString("videoId"))

                    try {
                        ite.put("title", txt2filename(reelItem.getJSONObject("headline").getString("simpleText")))
                    } catch (e: Exception) {
                        val runs = reelItem.getJSONObject("headline").getJSONArray("runs")
                        ite.put("title", txt2filename(runs.getJSONObject(0).getString("text")))
                    }

                    val thumb = reelItem.getJSONObject("thumbnail")
                        .getJSONArray("thumbnails").getJSONObject(0).getString("url")
                    ite.put("thumbnail", thumb)
                    ite.put("duration", "Shorts")
                    itemsVideo.add(ite)
                }

                rl.has("shortsLockupViewModel") -> {
                    val lockup = rl.getJSONObject("shortsLockupViewModel")
                   /* val vid=lockup.getJSONObject("inlinePlayerData").getJSONObject("onVisible")
                       .getJSONObject("innertubeCommand")
                       .getJSONObject("watchEndpoint")
                       .getString("videoId")
                    ite.put("videoId",vid)*/


                    val title = try {
                        lockup.getJSONObject("overlayMetadata")
                            .getJSONObject("primaryText")
                            .getString("content")
                    } catch (e: Exception) {
                        "unknown"
                    }
                    ite.put("title", txt2filename(title))

                    val thumb = lockup.getJSONObject("thumbnail")
                        .getJSONArray("sources")
                        .getJSONObject(0)
                        .getString("url")
                    ite.put("thumbnail", thumb)
                    ite.put("duration", "Shorts")
                    itemsVideo.add(ite)
                }
            }
        }

        return itemsVideo
    }
    fun elementRenderer(source: JSONObject): JSONObject {
        val jsonObject=JSONObject()
        if (source.getJSONObject("elementRenderer").getJSONObject("newElement").getJSONObject("type").getJSONObject("componentType").getJSONObject("model").has("compactVideoModel")){
            val videoDetails=source.getJSONObject("elementRenderer").getJSONObject("newElement").getJSONObject("type").getJSONObject("componentType").getJSONObject("model").getJSONObject("compactVideoModel").getJSONObject("compactVideoData").getJSONObject("videoData")
            val thumb=videoDetails.getJSONObject("thumbnail").getJSONObject("image").getJSONArray("sources").getJSONObject(0).getString("url")
            jsonObject.put("title",videoDetails.getJSONObject("metadata").getString("title"))
            jsonObject.put("duration",videoDetails.getJSONObject("thumbnail").get("timestampText"))
            jsonObject.put("thumbnail",thumb)
            jsonObject.put("videoId",videoId(videoDetails.getString("dragAndDropUrl")))
        }else{
            println(source.getJSONObject("elementRenderer").getJSONObject("newElement").getJSONObject("type").getJSONObject("componentType").getJSONObject("model"))
        }
        return jsonObject

    }
    private fun compactVideoRenderer(source: JSONObject): JSONObject {
        val itemTo=JSONObject()
        val js=source.getJSONObject("compactVideoRenderer")
        itemTo.put("title",getTitle(js.getJSONObject("title")))
        itemTo.put("duration",getDuration(js.getJSONObject("lengthText")))
        itemTo.put("thumbnail",getThumbnail(js.getJSONObject("thumbnail")))
        itemTo.put("videoId",js.getString("videoId"))
        return itemTo
    }
     fun search(term:String,continuation:String): JSONObject? {
        val allItems=JSONObject()
        val videosCollected = JSONArray()
        var nextContinuation:String?=null
        var sugggestion=  JSONArray()
        val jsonResponse = sendYoutubeSearchRequest(term,continuation)
        if (jsonResponse.has("onResponseReceivedCommands")){
            if (jsonResponse.getJSONArray("onResponseReceivedCommands").getJSONObject(0).has("appendContinuationItemsAction")){
                val sections=jsonResponse.getJSONArray("onResponseReceivedCommands").getJSONObject(0).getJSONObject("appendContinuationItemsAction").getJSONArray("continuationItems")
                val collections=sections.getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents")
                for (su in 0..<collections.length()) {
                    val s = collections.getJSONObject(su)
                    if (s.has("videoRenderer")){
                        val ite=JSONObject()
                        ite.put("videoId",s.getJSONObject("videoRenderer").getString("videoId"))
                        ite.put("title",txt2filename(s.getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text")))
                        if (s.getJSONObject("videoRenderer").has("lengthText")){
                            ite.put("duration",s.getJSONObject("videoRenderer").getJSONObject("lengthText").get("simpleText").toString())
                        }else{
                            ite.put("duration","Unknown")
                        }
                        ite.put("thumbnail",s.getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url"))
                        videosCollected.put(ite)


                    }
                    if (s.has("reelShelfRenderer")){
                        val videos=getreelShelfRenderer(s)
                        for (vip in videos){
                            videosCollected.put(vip)
                        }
                    }
                    if (s.has("shelfRenderer")){
                        val videos=getshelfRenderer(s)
                        for (vip in videos){
                            videosCollected.put(vip)
                        }
                    }

                }
                if (sections.length()>1){
                    nextContinuation=sections.getJSONObject(1).getJSONObject("continuationItemRenderer").getJSONObject("continuationEndpoint").getJSONObject("continuationCommand").getString("token")
                }
            }

        }
        if (jsonResponse.has("contents")){
            println(jsonResponse.getJSONObject("contents").keySet())

            if (jsonResponse.getJSONObject("contents").has("sectionListRenderer")){
                val conts=jsonResponse.getJSONObject("contents").getJSONObject("sectionListRenderer").getJSONArray("contents")
                for (vu in 0..<conts.length()) {
                    val kitem=conts.getJSONObject(vu)
                    if (kitem.has("continuationItemRenderer")){
                        val cotni=getContinuation(kitem.getJSONObject("continuationItemRenderer"))
                        if (cotni!=null){
                            nextContinuation=cotni
                        }
                    }
                    if (kitem.has("itemSectionRenderer")){
                        if(kitem.getJSONObject("itemSectionRenderer").has("continuations")){
                            nextContinuation=kitem.getJSONObject("itemSectionRenderer").getJSONArray("continuations").getJSONObject(0).getJSONObject("nextContinuationData").getString("continuation")
                        }
                        val compactVideoRendere=jsonResponse.getJSONObject("contents").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents")
                        for (su in 0..<compactVideoRendere.length()) {
                            val itemTo=JSONObject()
                            if (compactVideoRendere.getJSONObject(su).has("compactVideoRenderer")){
                                val js=compactVideoRenderer(compactVideoRendere.getJSONObject(su))
                                videosCollected.put(js)
                            }
                            if (compactVideoRendere.getJSONObject(su).has("videoWithContextRenderer")){
                                val item=compactVideoRendere.getJSONObject(su).getJSONObject("videoWithContextRenderer")
                                if (item.has("lengthText")){
                                    itemTo.put("duration",getDuration(item.getJSONObject("lengthText")))
                                }else{
                                    itemTo.put("duration","Unknown")
                                }
                                itemTo.put("thumbnail",getThumbnail(item.getJSONObject("thumbnail")))
                                itemTo.put("videoId",item.getString("videoId"))
                                itemTo.put("title",getTitle(item.getJSONObject("headline")))
                                videosCollected.put(itemTo)
                            }
                            if (compactVideoRendere.getJSONObject(su).has("elementRenderer")){
                                if (compactVideoRendere.getJSONObject(su).getJSONObject("elementRenderer").getJSONObject("newElement").getJSONObject("type").has("componentType")){
                                    val item=elementRenderer(compactVideoRendere.getJSONObject(su))
                                    videosCollected.put(item)

                                }

                            }
                        }

                    }
                    if (kitem.has("elementRenderer")){
                        val item=elementRenderer((kitem))
                        videosCollected.put(item)
                    }
                    if (kitem.has("shelfRenderer")){
                        val items=kitem.getJSONObject("shelfRenderer").getJSONObject("content").getJSONObject("verticalListRenderer").getJSONArray("items")
                        for (su in 0..<items.length()) {
                            if (items.getJSONObject(su).has("elementRenderer")){
                                val item=elementRenderer(items.getJSONObject(su))
                                videosCollected.put(item)
                            }

                        }
                    }

                }
            }
            if(jsonResponse.getJSONObject("contents").has("twoColumnSearchResultsRenderer")){
                println("twocolumbsea")
                val sections=jsonResponse.getJSONObject("contents").getJSONObject("twoColumnSearchResultsRenderer").getJSONObject("primaryContents").getJSONObject("sectionListRenderer").getJSONArray("contents")
                val collections=sections.getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents")
                for (su in 0..<collections.length()) {
                    val s = collections.getJSONObject(su)
                    if (s.has("videoRenderer")){
                        val ite=JSONObject()
                        ite.put("videoId",s.getJSONObject("videoRenderer").getString("videoId"))
                        ite.put("title",txt2filename(s.getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text")))
                        if (s.getJSONObject("videoRenderer").has("lengthText")){
                            ite.put("duration",s.getJSONObject("videoRenderer").getJSONObject("lengthText").get("simpleText").toString())
                        }else{
                            ite.put("duration","Unknown")
                        }
                        ite.put("thumbnail",s.getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url"))
                        videosCollected.put(ite)

                    }
                    if (s.has("reelShelfRenderer")){
                        val videos=getreelShelfRenderer(s)
                        for (vip in videos){
                            videosCollected.put(vip)
                        }
                    }
                    if (s.has("shelfRenderer")){
                        val videos=getshelfRenderer(s)
                        for (vip in videos){
                            videosCollected.put(vip)
                        }
                    }

                }
                if (sections.length()>1){
                    /*    println(sections.getJSONObject(1).getJSONObject("continuationItemRenderer").getJSONObject("continuationEndpoint").getJSONObject("continuationCommand").getString("token"))*/
                    nextContinuation=sections.getJSONObject(1).getJSONObject("continuationItemRenderer").getJSONObject("continuationEndpoint").getJSONObject("continuationCommand").getString("token")
                }
            }

        }
        if (jsonResponse.has("continuationContents")){
            val items=jsonResponse.getJSONObject("continuationContents").getJSONObject("sectionListContinuation").getJSONArray("contents").getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents")
            for (su in 0..<items.length()) {
                val itemTo=JSONObject()
                if (items.getJSONObject(su).has("compactVideoRenderer")){
                    val js=compactVideoRenderer(items.getJSONObject(su))
                    videosCollected.put(js)
                }
                if (items.getJSONObject(su).has("elementRenderer")){
                    val item=elementRenderer(items.getJSONObject(su))
                    videosCollected.put(item)
                }
            }
            if(jsonResponse.getJSONObject("continuationContents").getJSONObject("sectionListContinuation").has("continuations")){
                nextContinuation=jsonResponse.getJSONObject("continuationContents").getJSONObject("sectionListContinuation").getJSONArray("continuations").getJSONObject(0).getJSONObject("reloadContinuationData").getString("continuation")
            }
        }
        if (videosCollected.length()!=0){
            allItems.put("videos",videosCollected)
            allItems.put("nextContinuation",nextContinuation)
            allItems.put("suggestion",sugggestion)
            return allItems
        }
        return null
    }
    fun playlistVideoRendrer(source: JSONArray): Pair<JSONArray, String?> {
        val allitems=JSONObject()
        val videos=JSONArray()
        var continuation:String?=null
        for (su in 0..<source.length()) {
            val item=source.getJSONObject(su)
            if (item.has("playlistVideoRenderer")){
                val ite=JSONObject()
                if (item.getJSONObject("playlistVideoRenderer").getJSONObject("lengthText").has("simpleText")){
                    ite.put("duration",item.getJSONObject("playlistVideoRenderer").getJSONObject("lengthText").get("simpleText"))
                }
                if (item.getJSONObject("playlistVideoRenderer").getJSONObject("lengthText").has("runs")){
                    ite.put("duration",item.getJSONObject("playlistVideoRenderer").getJSONObject("lengthText").getJSONArray("runs").getJSONObject(0).getString("text"))
                }
                ite.put("thumbnail",getThumbnail(item.getJSONObject("playlistVideoRenderer").getJSONObject("thumbnail")))
                ite.put("title",getTitle(item.getJSONObject("playlistVideoRenderer").getJSONObject("title")))
                ite.put("videoId",item.getJSONObject("playlistVideoRenderer").get("videoId"))
                videos.put(ite)
            }
            if (item.has("richItemRenderer")){
                val rl=item.getJSONObject("richItemRenderer").getJSONObject("content")
                if (rl.has("videoWithContextRenderer")){
                    val itemTo=JSONObject()
                    val itemT=rl.getJSONObject("videoWithContextRenderer")
                    if (item.has("lengthText")){
                        itemTo.put("duration",getDuration(itemT.getJSONObject("lengthText")))
                    }else{
                        itemTo.put("duration","Unknown")
                    }
                    itemTo.put("thumbnail",getThumbnail(itemT.getJSONObject("thumbnail")))
                    itemTo.put("videoId",itemT.getString("videoId"))
                    itemTo.put("title",getTitle(itemT.getJSONObject("headline")))
                    videos.put(itemTo)
                }
                if (rl.has("videoRenderer")){
                    val ite=JSONObject()
                    ite.put("videoId",rl.getJSONObject("videoRenderer").getString("videoId"))
                    ite.put("title",txt2filename(rl.getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text")))
                    if (rl.getJSONObject("videoRenderer").has("lengthText")){
                        ite.put("duration",rl.getJSONObject("videoRenderer").getJSONObject("lengthText").get("simpleText").toString())
                    }else{
                        ite.put("duration","Unknown")
                    }
                    ite.put("thumbnail",rl.getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url"))
                    videos.put(ite)
                }
            }

            if (item.has("continuationItemRenderer")){
               val conti=getContinuation(item.getJSONObject("continuationItemRenderer"))
                if (conti!=null){
                    continuation=conti
                }
            }
        }
        allitems.put("videos",videos)
        allitems.put("nextContinuation",continuation)
        return Pair(videos,continuation)
    }
    fun getChannelId(url: String): String? {
        try {
            val doc = Jsoup.connect(url).get()
            val scriptTags = doc.select("script")
            for (scriptTag in scriptTags) {
                val scriptContent = scriptTag.data().trim()
                if (scriptContent.startsWith("var ytInitialData")) {
                    val jsonString = scriptContent.substringAfter("{").substringBeforeLast("}")
                    val jsonObject = JSONObject("{$jsonString}")
                    val cid=jsonObject.getJSONObject("metadata").getJSONObject("channelMetadataRenderer").getString("externalId")
                    return cid
                }
            }

        } catch (e: Exception) {
            return null
        }
        return null

    }
    fun playlist(continuation: String?): JSONObject? {
        val allItems=JSONObject()
        var videosCollected = JSONArray()
        var nextContinuation:String?=null
        val indedxes= mutableListOf(1,4,1,7,8)
        for (index in indedxes){
            val variant=variants[index]
            val client = OkHttpClient()
            val baseApiUrl = "https://www.youtube.com/youtubei/v1/browse"
            val requestBody = variant.data
            if (continuation!=null){
                requestBody.put("continuation",continuation)
            }
            val urlWithQuery = StringBuilder(baseApiUrl)
            if (variant.query.isNotEmpty()) {
                urlWithQuery.append("?")
                variant.query.forEach { (key, value) ->
                    urlWithQuery.append("$key=$value&")
                }
                urlWithQuery.deleteCharAt(urlWithQuery.length - 1)
            }
            val request = Request.Builder()
                .url(urlWithQuery.toString())
                .apply {
                    variant.headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .post(requestBody.toString().toRequestBody())
                .build()
            val response = client.newCall(request).execute()
            response.body.use { responseBody ->
                val jsonResponse = JSONObject(responseBody.string())
                if (jsonResponse.has("onResponseReceivedActions")){
                    val sections=jsonResponse.getJSONArray("onResponseReceivedActions").getJSONObject(0).getJSONObject("appendContinuationItemsAction").getJSONArray("continuationItems")
                    val videos=playlistVideoRendrer(sections)
                    videosCollected=videos.first
                    nextContinuation=videos.second
                }
                if (jsonResponse.has("continuationContents")){
                    val cmpvi=jsonResponse.getJSONObject("continuationContents").getJSONObject("playlistVideoListContinuation")
                    val videos=playlistVideoRendrer(cmpvi.getJSONArray("contents"))
                    videosCollected=videos.first
                    nextContinuation=cmpvi.getJSONArray("continuations").getJSONObject(0).getJSONObject("nextContinuationData").getString("continuation")
                }

            }
            if (!response.isSuccessful){
                println("failed to get json")
            }
            if (videosCollected.length()!=0){
                allItems.put("videos",videosCollected)
                allItems.put("nextContinuation",nextContinuation)
                return allItems
            }
        }

        return null

    }
    fun extractPlaylistId(url: String): String? {
        val playlistRegex = """(?:[?&]list=)([a-zA-Z0-9_-]+)""".toRegex()
        val channelRegex = """(?:youtube\.com/(?:channel|user|c|@))([a-zA-Z0-9_-]+)""".toRegex()

        val playlistMatch = playlistRegex.find(url)?.groupValues?.get(1)
        val channelMatch = channelRegex.find(url)?.groupValues?.get(1)

        return playlistMatch ?: channelMatch
    }
    fun getPlayListItemsFromHtml(url: String): JSONObject? {
        val allItems=JSONObject()
        try {
            val doc = Jsoup.connect(url).get()
            val scriptTags = doc.select("script")
            for (scriptTag in scriptTags) {
                val scriptContent = scriptTag.data().trim()
                if (scriptContent.startsWith("var ytInitialData")) {
                    val jsonString = scriptContent.substringAfter("{").substringBeforeLast("}")
                    val jsonObject = JSONObject("{$jsonString}")
                    try {
                        val contAndItems=jsonObject.getJSONObject("contents").getJSONObject("twoColumnBrowseResultsRenderer").getJSONArray("tabs").getJSONObject(0).getJSONObject("tabRenderer").getJSONObject("content").getJSONObject("sectionListRenderer").getJSONArray("contents").getJSONObject(0).getJSONObject("itemSectionRenderer").getJSONArray("contents").getJSONObject(0)
                        if (contAndItems.has("playlistVideoListRenderer")){
                            val videos=playlistVideoRendrer(contAndItems.getJSONObject("playlistVideoListRenderer").getJSONArray("contents"))
                            if (videos.first.length()!=0){
                                allItems.put("videos",videos.first)
                                allItems.put("nextContinuation",videos.second)
                                return allItems
                            }
                        }
                    }catch (e:Exception){
                        return null
                    }

                }
            }

        } catch (e: IOException) {
            println("Error fetching the web page: ${e.message}")
            return null
        }
        return null

    }
    fun getFromChannelHtml(url: String): JSONObject? {
        val cm=getChannelId(url)
        val reUrl="https://www.youtube.com/channel/${cm}/videos"
        val allItems=JSONObject()
        val videosCollected = JSONArray()
        var nextContinuation:String?=null
        try {
            val doc = Jsoup.connect(reUrl).get()
            val scriptTags = doc.select("script")
            for (scriptTag in scriptTags) {
                val scriptContent = scriptTag.data().trim()
                if (scriptContent.startsWith("var ytInitialData")) {
                    val jsonString = scriptContent.substringAfter("{").substringBeforeLast("}")
                    val jsonObject = JSONObject("{$jsonString}")
                    val videos=jsonObject.getJSONObject("contents").getJSONObject("twoColumnBrowseResultsRenderer").getJSONArray("tabs").getJSONObject(1).getJSONObject("tabRenderer").getJSONObject("content").getJSONObject("richGridRenderer").getJSONArray("contents")
                    for (l in 0..<videos.length()) {
                        val v=videos.getJSONObject(l)
                        if (v.has("richItemRenderer")){
                            val ite=JSONObject()
                            val rl=v.getJSONObject("richItemRenderer").getJSONObject("content")
                            ite.put("videoId",rl.getJSONObject("videoRenderer").getString("videoId"))
                            ite.put("title",txt2filename(rl.getJSONObject("videoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text")))
                            if (rl.getJSONObject("videoRenderer").has("lengthText")){
                                ite.put("duration",rl.getJSONObject("videoRenderer").getJSONObject("lengthText").get("simpleText").toString())
                            }else{
                                ite.put("duration","Unknown")
                            }
                            ite.put("thumbnail",rl.getJSONObject("videoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url"))
                            videosCollected.put(ite)
                        }
                        if (v.has("continuationItemRenderer")){
                            nextContinuation=v.getJSONObject("continuationItemRenderer").getJSONObject("continuationEndpoint").getJSONObject("continuationCommand").getString("token")
                        }

                    }

                }
            }
            allItems.put("videos",videosCollected)
            allItems.put("nextContinuation",nextContinuation)
            return allItems

        } catch (e: IOException) {
            println("Error fetching the web page: ${e.message}")
            return null

        }


    }
    fun formatSpeed(speedT: Long): String {
        val speed=speedT.toDouble()
        return when {
            speed > 1e9 -> String.format("%.2f GB", speed / 1e9)
            speed > 1e6 -> String.format("%.2f MB", speed / 1e6)
            speed > 1e3 -> String.format("%.2f KB", speed / 1e3)
            else -> String.format("%.2f B", speed)
        }
    }
    fun hlsExtractor(videoId: String): JSONObject? {
        val client = OkHttpClient()

        // List of endpoints with their associated headers and data
        val endpoints = listOf(
            // First endpoint (WEB client)
            mapOf(
                "url" to "https://www.youtube.com/youtubei/v1/player",
                "data" to JSONObject().apply {
                    put("context", JSONObject().apply {
                        put("client", JSONObject().apply {
                            put("clientName", "WEB")
                            put("clientVersion", "2.20240726.00.00")
                            put("userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.5 Safari/605.1.15,gzip(gfe)")
                            put("hl", "en")
                            put("timeZone", "UTC")
                            put("utcOffsetMinutes", 0)
                        })
                    })
                    put("videoId", videoId)
                    put("playbackContext", JSONObject().apply {
                        put("contentPlaybackContext", JSONObject().apply {
                            put("html5Preference", "HTML5_PREF_WANTS")
                        })
                    })
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                },
                "headers" to mapOf(
                    "X-YouTube-Client-Name" to "1",
                    "X-YouTube-Client-Version" to "2.20240726.00.00",
                    "Origin" to "https://www.youtube.com",
                    "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.5 Safari/605.1.15,gzip(gfe)",
                    "Content-Type" to "application/json",

                    ),
                "params" to mapOf("prettyPrint" to "false")
            ),
            // Second endpoint (MEDIA_CONNECT_FRONTEND client)
            mapOf(
                "url" to "https://www.youtube.com/youtubei/v1/player",
                "data" to JSONObject().apply {
                    put("context", JSONObject().apply {
                        put("client", JSONObject().apply {
                            put("clientName", "MEDIA_CONNECT_FRONTEND")
                            put("clientVersion", "0.1")
                            put("hl", "en")
                            put("timeZone", "UTC")
                            put("utcOffsetMinutes", 0)
                        })
                    })
                    put("videoId", videoId)
                    put("playbackContext", JSONObject().apply {
                        put("contentPlaybackContext", JSONObject().apply {
                            put("html5Preference", "HTML5_PREF_WANTS")
                        })
                    })
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                },
                "headers" to mapOf(
                    "X-YouTube-Client-Name" to "95",
                    "X-YouTube-Client-Version" to "0.1",
                    "Origin" to "https://www.youtube.com",
                    "Content-Type" to "application/json",
                ),
                "params" to mapOf("prettyPrint" to "false")
            )
        )
        for (endpoint in endpoints) {
            try {
                val url = endpoint["url"] as String
                val data = endpoint["data"] as JSONObject
                println(data)
                val headers = endpoint["headers"] as Map<String, String>
                val params = endpoint["params"] as Map<String, String>

                val requestBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(), data.toString()
                )

                val request = Request.Builder()
                    .url(url)
                    .headers(headers.toHeaders())
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    return JSONObject(response.body.string())
                }
            } catch (e: Exception) {
                println("Request failed with error: ${e.localizedMessage}")
            }
        }


        return null
    }



}
fun instagram(vid: String): MutableList<JSONObject> {
    val items = mutableListOf<JSONObject>()
    val stm=getInstagramJson(vid)
    if (stm.toString().contains("edge_sidecar_to_children")){
        val edgeSidecarToChildren = stm?.getJSONObject("edge_sidecar_to_children")
        val edgesArray = edgeSidecarToChildren?.getJSONArray("edges")
        val edge= stm?.getJSONObject("edge_media_to_caption")
        val edges= edge?.getJSONArray("edges")
        if (edgesArray != null) {
            for (i in 0..<edgesArray.length()) {
                val item=JSONObject()
                val edgeObject = edgesArray.getJSONObject(i)
                /* if (edges != null) {
                     if(edges.length()!=0){
                         val ege1=edges.getJSONObject(0)
                         val nd=ege1.getJSONObject("node")
                         item.put("title","InstagramVideo($vid)$i")
                         *//* if (nd.toString().contains("text")){
                                                 item.put("title","InstagramVideo($vid)")
                                                 *//**//*item.put("title",nd.get("text"))*//**//*
                                                }*//**//*else{
                                                    item.put("title","InstagramVideo($vid)")
                                                }*//*
                        }
                    }*/
                val nodeObject = edgeObject.getJSONObject("node")
                item.put("title","InstagramVideo$i Id($vid)")
                /* if (nodeObject.toString().contains("accessibility_caption")){
                         if (nodeObject.get("accessibility_caption")!=null){
                             item.put("title","InstagramVideo($vid)")
                             *//*item.put("title",nodeObject.get("accessibility_caption"))*//*
                             *//*item.put("title",nodeObject.get("accessibility_caption"))*//*
                                }*//*else{
                                    item.put("title","InstagramVideo($vid)")
                                }*//*
                            }*/
                if (nodeObject.getBoolean("is_video")){
                    item.put("is_video",true)
                    item.put("video_url",nodeObject.get("video_url"))
                }else{
                    item.put("is_video",false)
                    item.put("display_url",nodeObject.get("display_url"))
                }
                items.add(item)


            }
        }
    }else{
        val item=JSONObject()
        val edge= stm?.getJSONObject("edge_media_to_caption")
        val edges= edge?.getJSONArray("edges")
        /* if(edges.length()!=0){
             val ege1=edges.getJSONObject(0)
             val nd=ege1.getJSONObject("node")
             if (nd.toString().contains("text")){
                 item.put("title",nd.get("text"))
                 println(nd.get("text"))
             }else{
                 item.put("title","InstagramVideo($vid)")
             }
         }*/
        if (stm != null) {
            if(stm.getBoolean("is_video")){
                println(stm.get("video_url"))
                item.put("is_video",true)
                item.put("video_url",stm.get("video_url"))
                item.put("title","InstagramVideoId($vid)")

            }else{
                println(stm.get("display_url"))
                item.put("is_video",false)
                item.put("display_url",stm.get("display_url"))
                item.put("title","InstagramVideoId($vid)")

            }
        }
        items.add(item)

    }
    return items

}
fun extractInstagramVideoId(url: String): String? {
    val pattern = "(?:https?:\\/\\/)?(?:www\\.)?instagram\\.com\\/?([a-zA-Z0-9\\.\\_\\-]+)?\\/([p]+)?([reel]+)?([tv]+)?([stories]+)?\\/([a-zA-Z0-9\\-\\_\\.]+)\\/?([0-9]+)?"
    val matcher = Pattern.compile(pattern).matcher(url)
    return if (matcher.find()) {
        matcher.group(6)
    } else {
        null
    }
}
fun getInstagramJson(vid: String): JSONObject? {
    val client = OkHttpClient()
    val INSTAGRAM_DOCUMENT_ID = "8845758582119845"
    val variables = JSONObject().apply {
        put("shortcode", vid)
        put("fetch_tagged_user_count", JSONObject.NULL)
        put("hoisted_comment_id", JSONObject.NULL)
        put("hoisted_reply_id", JSONObject.NULL)
    }
    val encodedVariables = URLEncoder.encode(variables.toString(), "UTF-8")
    val body = "variables=$encodedVariables&doc_id=$INSTAGRAM_DOCUMENT_ID".toRequestBody()


    val request = Request.Builder()
        .url("https://www.instagram.com/graphql/query/")
        .post(body)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build()
    val response=client.newCall(request).execute()
    val jsonResponse = response.body.string()
    val js=JSONObject(jsonResponse)
    val data=js.getJSONObject("data")
    return if (data.has("xdt_shortcode_media")){
        data.getJSONObject("xdt_shortcode_media")
    }else{
        null
    }
}
val client = OkHttpClient()

fun sendYoutubeSearchRequest(
    query: String,
    continuation: String
): JSONObject {
    val jsonBody = JSONObject().apply {
        if (continuation.isNotEmpty()) {
            put("continuation", continuation)
        } else {
            put("query", query)
            put("params", "8AEB")
        }

        put("context", JSONObject().apply {
            put("request", JSONObject().apply {
                put("internalExperimentFlags", JSONArray())
                put("useSsl", true)
            })
            put("client", JSONObject().apply {
                put("utcOffsetMinutes", 0)
                put("hl", "en-GB")
                put("gl", "IN")
                put("clientName", "WEB")
                put("originalUrl", "https://www.youtube.com")
                put("clientVersion", "2.20250613.00.00")
                put("platform", "DESKTOP")
            })
            put("user", JSONObject().apply {
                put("lockedSafetyMode", false)
            })
        })
    }

    val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("https://www.youtube.com/youtubei/v1/search?prettyPrint=false")
        .post(requestBody)
        .addHeader("Origin", "https://www.youtube.com")
        .addHeader("Referer", "https://www.youtube.com")
        .addHeader("X-YouTube-Client-Version", "2.20250613.00.00")
        .addHeader("X-YouTube-Client-Name", "1")
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept-Language", "en-GB, en;q=0.9")
        .build()

    val response=client.newCall(request).execute()
    val response_json= JSONObject(response.body.string())
    return response_json

}

fun getVisitorId(): String {
    val client = OkHttpClient()
    val url = "https://youtubei.googleapis.com/youtubei/v1/visitor_id?prettyPrint=false"

    // JSON Body
    val jsonBody = JSONObject(
        mapOf(
            "context" to mapOf(
                "request" to mapOf(
                    "internalExperimentFlags" to emptyList<Any>(),
                    "useSsl" to true
                ),
                "client" to mapOf(
                    "androidSdkVersion" to 35,
                    "utcOffsetMinutes" to 0,
                    "osVersion" to "15",
                    "hl" to "en-GB",
                    "clientName" to "ANDROID",
                    "gl" to "GB",
                    "clientScreen" to "WATCH",
                    "clientVersion" to "19.28.35",
                    "osName" to "Android",
                    "platform" to "MOBILE"
                ),
                "user" to mapOf(
                    "lockedSafetyMode" to false
                )
            )
        )
    )

    // Headers
    val headers = mapOf(
        "User-Agent" to "com.google.android.youtube/19.28.35 (Linux; U; Android 15; GB) gzip",
        "X-Goog-Api-Format-Version" to "2",
        "Content-Type" to "application/json",
        "Accept-Language" to "en-GB, en;q=0.9"
    )

    // Convert JSON to Request Body
    val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

    // Build Request
    val requestBuilder = Request.Builder()
        .url(url)
        .post(requestBody)

    // Add Headers
    headers.forEach { (key, value) ->
        requestBuilder.addHeader(key, value)
    }

    // Final Request
    val request = requestBuilder.build()
    val respons=client.newCall(request).execute()
    val responseString=respons.body?.string()
    val responseJson=JSONObject(responseString)
    return responseJson.getJSONObject("responseContext").getString("visitorData")

}
fun androidPlayerResponse(cpn:String,visitorData:String,videoId:String,t:String): Request {
    val url = "https://youtubei.googleapis.com/youtubei/v1/reel/reel_item_watch?prettyPrint=false&t=$t&id=$videoId&fields=playerResponse"

    // Create the JSON request body
    val jsonBody = JSONObject().apply {
        put("cpn", cpn)
        put("contentCheckOk", true)
        put("context", JSONObject().apply {
            put("request", JSONObject().apply {
                put("internalExperimentFlags", JSONArray())
                put("useSsl", true)
            })
            put("client", JSONObject().apply {
                put("androidSdkVersion", 35)
                put("utcOffsetMinutes", 0)
                put("osVersion", "15")
                put("hl", "en-GB")
                put("clientName", "ANDROID")
                put("gl", "GB")
                put("clientScreen", "WATCH")
                put("clientVersion", "19.28.35")
                put("osName", "Android")
                put("platform", "MOBILE")
                put("visitorData", visitorData)
            })
            put("user", JSONObject().apply {
                put("lockedSafetyMode", false)
            })
        })
        put("racyCheckOk", true)
        put("videoId", videoId)
        put("playerRequest", JSONObject().apply {
            put("videoId", videoId)
        })
        put("disablePlayerResponse", false)
    }

    // Define the request headers
    val headers = mapOf(
        "User-Agent" to "com.google.android.youtube/19.28.35 (Linux; U; Android 15; GB) gzip",
        "X-Goog-Api-Format-Version" to "2",
        "Content-Type" to "application/json",
        "Accept-Language" to "en-GB, en;q=0.9"
    )
    val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val requestBuilder = Request.Builder()
        .url(url)
        .post(requestBody)
    headers.forEach { (key, value) ->
        requestBuilder.addHeader(key, value)
    }
    val request = requestBuilder.build()
    return request

}


/*0 = WEB
1 = ANDROID_EMBEDDED_PLAYER
2 = TVHTML5_SIMPLY_EMBEDDED_PLAYER
3 = WEB_CREATOR
4 = MWEB
5 = ANDROID_CREATOR
6 = IOS_CREATOR
7 = IOS_MESSAGES_EXTENSION
8 = IOS
9 = WEB_REMIX
10 = ANDROID_MUSIC
11 = IOS_MUSIC
12 = WEB
13 = WEB_EMBEDDED_PLAYER
14 = ANDROID_VR
15 = TVHTML5
16 = MEDIA_CONNECT_FRONTEND
*/