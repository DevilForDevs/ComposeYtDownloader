package praser

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

val client = OkHttpClient()
suspend fun sendYoutubeSearchRequest(
    query: String,
    continuation: String,
    parmas: String
): JSONObject{
    val jsonBody = JSONObject().apply {
        if (continuation.isNotEmpty()) {
            put("continuation", continuation)
        } else {
            put("query", query)
            put("params", parmas)
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
    val totalVideos= JSONArray()
    val totalResult= JSONObject()
    val estimatedResult=response_json.getString("estimatedResults")
    totalResult.put("estimatedResult",estimatedResult)
    if (response_json.has("contents")){
        val contentsArray = response_json
            .deepGet(
                "contents",
                "twoColumnSearchResultsRenderer",
                "primaryContents",
                "sectionListRenderer",
                "contents", 0,
                "itemSectionRenderer",
                "contents"
            ) as? JSONArray
        if (contentsArray!=null){
            for (su in 0..<contentsArray.length()) {
                val item= contentsArray.getJSONObject(su)
                if (item.has("videoRenderer")){
                    totalVideos.put(createVideoTree(item.getJSONObject("videoRenderer")))
                }
                if (item.has("reelShelfRenderer")){
                    val shorts=item.getJSONObject("reelShelfRenderer").getJSONArray("items")
                    for (vu in 0..<shorts.length()) {
                        totalVideos.put(extractShortsInfo(shorts.getJSONObject(vu)))
                    }
                }
            }
            val continuationToken = response_json
                .deepGet(
                    "contents",
                    "twoColumnSearchResultsRenderer",
                    "primaryContents",
                    "sectionListRenderer",
                    "contents", 1,
                    "continuationItemRenderer",
                    "continuationEndpoint",
                    "continuationCommand",
                    "token"
                ) as? String
            totalResult.put("continuation",continuationToken)
            totalResult.put("videos",totalVideos)


        }
    }
    if (response_json.has("onResponseReceivedCommands")){
        println("coninuation items")
        val contentsArray = response_json.deepGet(
            "onResponseReceivedCommands", 0,
            "appendContinuationItemsAction",
            "continuationItems", 0,
            "itemSectionRenderer",
            "contents"
        ) as? JSONArray
        if (contentsArray!=null){
            for (su in 0..<contentsArray.length()) {
                val item= contentsArray.getJSONObject(su)
                if (item.has("videoRenderer")){
                    val  vt=createVideoTree(item.getJSONObject("videoRenderer"))

                    if (vt !in totalVideos){
                        totalVideos.put(vt)
                    }
                }
                if (item.has("reelShelfRenderer")){
                    val shorts=item.getJSONObject("reelShelfRenderer").getJSONArray("items")
                    for (vu in 0..<shorts.length()) {
                        val mk=extractShortsInfo(shorts.getJSONObject(vu))
                        if (mk!in totalVideos){
                            totalVideos.put(mk)
                        }
                    }
                }
            }
        }
        val continuationToken = response_json
            .deepGet(
                "onResponseReceivedCommands", 0,
                "appendContinuationItemsAction",
                "continuationItems",1,
                "continuationItemRenderer",
                "continuationEndpoint",
                "continuationCommand",
                "token"
            ) as? String
        totalResult.put("continuation",continuationToken)
        totalResult.put("videos",totalVideos)




    }
    return totalResult



}
fun extractShortsInfo(json: JSONObject): JSONObject{
    val root = json.getJSONObject("shortsLockupViewModel")

    val videoId = root
        .getJSONObject("inlinePlayerData")
        .getJSONObject("onVisible")
        .getJSONObject("innertubeCommand")
        .getJSONObject("watchEndpoint")
        .getString("videoId")

    val title = root
        .getJSONObject("overlayMetadata")
        .getJSONObject("primaryText")
        .getString("content")
    val result= JSONObject()
    result.put("videoId",videoId)
    result.put("title",title)
    result.put("duration","short")
    return result
}
fun JSONObject.deepGet(vararg keys: Any): Any? {
    var current: Any? = this
    for (key in keys) {
        current = when (current) {
            is JSONObject -> current.opt(key as String)
            is JSONArray -> current.opt(key as Int)
            else -> return null
        }
    }
    return current
}


fun createVideoTree(videoRenderer: JSONObject): JSONObject {
    val videoId = videoRenderer.getString("videoId")


    val title = videoRenderer
        .getJSONObject("title")
        .getJSONArray("runs")
        .getJSONObject(0)
        .getString("text")

    val result = JSONObject()
    result.put("videoId", videoId)
    if (videoRenderer.has("lengthText")){
        val duration = videoRenderer
            .getJSONObject("lengthText")
            .getString("simpleText")
        result.put("duration", duration)
    }else{
        result.put("duration", "Unknown")
    }

    result.put("title", title)

    return result
}