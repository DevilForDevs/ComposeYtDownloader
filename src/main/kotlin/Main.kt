import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import streams.WebMMuxer
import java.awt.FileDialog
import java.awt.Frame
import java.io.*
import java.net.URI
import java.net.URL
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import dashedmuxer.DashedParser
import dashedmuxer.DashedWriter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import praser.sendYoutubeSearchRequest
import java.awt.EventQueue
import java.nio.file.Paths

data class NavItem(val name: String, val icon: ImageVector)

@Composable
fun SideNavigation(selected: String, onSelect: (String) -> Unit) {
    val items = listOf(
        NavItem("Downloads", Icons.Default.ArrowDropDown),
        NavItem("Home", Icons.Default.Home),
        NavItem("Settings", Icons.Default.Settings)
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Color.Black), // Optional: background color
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(24.dp))
        items.forEach { item ->
            val isSelected = item.name == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .clickable { onSelect(item.name) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Color(0xFFC11310) else Color(0xFF1D161D))
                )

                Spacer(Modifier.width(10.dp))

                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = if (isSelected) Color(0xFFC11310) else Color.White
                )
            }
        }
    }
}
/*https://www.youtube.com/watch?v=7QONnvGtxZU*/


@Composable
fun ContentArea(selected: String) {
    val downloadItems = remember { mutableStateListOf<DownloadItem>() }
    var searchItems = remember { mutableStateListOf<JSONObject>() }
    var continuationToken=remember { mutableStateOf("") }
    var enableGrid by remember { mutableStateOf(false) }
    val numberOfResults = remember { mutableStateOf("") }
    when (selected) {
        "Home" -> HomeScreen(
            downloadItems = downloadItems,
            searchItems = searchItems,
            continuationToken = continuationToken,
            numberOfResults = numberOfResults,
            enableGrid =enableGrid
        ){
            enableGrid=it
        }
        "Downloads" -> ProfileScreen(downloadItems)
        "Settings" -> SettingsScreen()
    }
}
@Composable
fun UrlInputField(
    text: String,
    onTextChange: (String) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF45292E)),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (text.isEmpty()) {
                        Text(
                            "Enter URL",
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

private fun validUrl(urlS:String): Boolean {
    return try {
        URL(urlS)
        true
    } catch (e: Exception) {
        false
    }
}
fun saveResultToDisk(result: JSONObject, filename: String = "search_result.json") {
    val file = File(filename)
    file.writeText(result.toString(4))  // Pretty-print with indentation
}



@Composable
fun HomeScreen(downloadItems: SnapshotStateList<DownloadItem>,searchItems: SnapshotStateList<JSONObject>,continuationToken: MutableState<String>,numberOfResults: MutableState<String>,enableGrid: Boolean,onArrangementChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D161D))
            .padding(horizontal = 16.dp)
    ) {
        var text by remember { mutableStateOf("") }
        var shownItems by remember { mutableStateOf("") }
        var showDialog by remember { mutableStateOf(false) }
        var longVideos by remember { mutableStateOf(false) }
        var selectedFilePath by remember { mutableStateOf<String?>(null) }
        val scrapper = Interact()
        val scope = rememberCoroutineScope()
        var responsejson = remember { mutableStateOf(JSONObject()) }

        //animation related vars
        val offsetY = remember { Animatable(30f) }
        val alpha = remember { Animatable(0f) }
        var triggeredHalfway by remember { mutableStateOf(false) }



        fun doSomething() {
            scope.launch {
                withContext(Dispatchers.IO) {
                    if (validUrl(text)){
                        val videoId = scrapper.videoId(text)
                        videoId?.let {
                            val result = scrapper.getStreamingData(it)
                            responsejson.value = result
                            text = ""
                            showDialog = true
                        }
                    }else{
                        val appDataDir = File(System.getProperty("user.home"), "ComposeDownloader/thumbnails")
                        if (appDataDir.exists()) {
                            appDataDir.listFiles()?.forEach { it.delete() }
                        }
                        continuationToken.value=""
                        searchItems.clear()
                        val result= sendYoutubeSearchRequest(text,"", parmas = if (longVideos)"EgIYAg%3D%3D" else "8AEB")
                        numberOfResults.value=result.getString("estimatedResult")
                        if (result.has("continuation")){
                            continuationToken.value=result.getString("continuation")
                        }
                        val videos=result.getJSONArray("videos")
                        for (i in 0 until videos.length()) {
                            val video = videos.getJSONObject(i)
                            searchItems.add(video)
                        }

                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            delay(1000)
            launch {
                // Observe offsetY using snapshotFlow
                snapshotFlow { offsetY.value }
                    .collect { value ->
                        if (value <= 15f && !triggeredHalfway) {
                            triggeredHalfway = true
                        }
                    }
            }

            // Start animations
            launch { offsetY.animateTo(0f, tween(800)) }
            launch { alpha.animateTo(1f, tween(800)) }
        }
        /* .offset { IntOffset(0, offsetY.value.toInt()) }
                .graphicsLayer { this.alpha = alpha.value },*/


        // Top Row: Input + Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF45292E))
                    .padding(horizontal = 12.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            doSomething()
                            true
                        } else false
                    }
                    ,
                contentAlignment = Alignment.CenterStart
            ) {
                UrlInputField(
                    text = text,
                    onTextChange = { text = it }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    val file = chooseTxtFile()
                    selectedFilePath = file?.absolutePath
                },
                modifier = Modifier.height(40.dp)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF45292E),
                    contentColor = Color.White
                )
            ) {
                Text("Use Text File")
            }
            Button(
                onClick = {
                    doSomething()
                },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF45292E),
                    contentColor = Color.White
                )
            ) {
                Text("Download")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (enableGrid) "" else "Show Grid",
                color = Color.White,
                fontSize = 12.sp
            )
            Switch(
                checked = enableGrid,
                onCheckedChange = {
                    onArrangementChange(it)
                                  },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF45292E)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text =shownItems,
                color = Color.White,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            var expanded by remember { mutableStateOf(false) }

            IconButton(
                onClick = { expanded = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        longVideos = true
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Long")
                            if (longVideos) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.Green
                                )
                            }
                        }
                    }

                    DropdownMenuItem(onClick = {
                        expanded = false
                        longVideos = false
                    }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Short")
                            if (!longVideos) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.Green
                                )
                            }
                        }
                    }

                    DropdownMenuItem(onClick = {
                        expanded = false
                        println("About clicked")
                    }) {
                        Text("About")
                    }

                }
            }
        }

        if (selectedFilePath!=null){
            println(selectedFilePath)
            val f= File(selectedFilePath)
            val lines=f.readLines()
            for (line in lines){
                val cleaned = line.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
                if (cleaned.contains("dribbble.com")){
                   /* val ddownloadItem=DownloadItem()
                    ddownloadItem.suffix="Initiating...."
                    ddownloadItem.inRam.value=0*/


                }
            }
            val batchSize = 11
            lines.chunked(batchSize).forEach { batch ->
                runBlocking {
                    batch.map { url ->
                        async(Dispatchers.IO) {
                            val cleaned =url.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
                            if (cleaned.contains("dribbble.com")){
                                val ddownloadItem=DownloadItem()
                                ddownloadItem.suffix="Initiating...."
                                ddownloadItem.inRam.value=0
                                val userHome = System.getProperty("user.home")
                                val downloadsFolder = Paths.get(userHome, "Downloads").toString()+"/Dribblesfiles"
                                val file= File(downloadsFolder)
                                if (!file.exists()){
                                    file.mkdir()
                                }
                                val regex = Regex("userupload/(\\d+)")
                                val match = regex.find(cleaned)
                                val uniqueId = match?.groupValues?.get(1) ?: "unknown"

                                val originalName = File(URI(cleaned).path).name
                                val fileName = "${uniqueId}_$originalName"
                                ddownloadItem.fileName.value=fileName
                                ddownloadItem.videoId="ddr.png"
                                val outputPath = File(downloadsFolder, fileName)
                                if (outputPath.exists()){
                                    outputPath.delete()
                                }else{

                                }


                            }
                        }
                    }.awaitAll()
                }
            }
        }

        if (showDialog) {
            askResolution(
                resp = responsejson.value,
                onDismiss = { showDialog = false },
                selectedItag = {
                    val playerResponse = responsejson.value.getJSONObject("playerResponse")
                    val mitem=DownloadItem()
                    mitem.videoId=playerResponse.getJSONObject("videoDetails").getString("videoId")
                    downloadItems.add(mitem)
                    scope.launch(Dispatchers.IO){
                        downloadItem(playerResponse,mitem,it)
                    }

                }
            )
        }
        val requestMutex = remember { Mutex() }

        suspend fun loadMore() {
            requestMutex.withLock {
                if (continuationToken.value != "") {
                    val result = sendYoutubeSearchRequest(text, continuationToken.value,parmas = if (longVideos)"EgIYAg%3D%3D" else "8AEB"
                    )

                    if (result.has("continuation")) {
                        continuationToken.value = result.getString("continuation")
                    } else {
                        continuationToken.value = ""
                    }

                    val videos = result.getJSONArray("videos")
                    val newVideos = mutableListOf<JSONObject>()
                    for (i in 0 until videos.length()) {
                        val video = videos.getJSONObject(i)
                        val newVideoId = video.optString("videoId", null)

                        if (newVideoId != null && searchItems.none { it.optString("videoId") == newVideoId }) {
                            newVideos.add(video)
                        }
                    }

                    EventQueue.invokeLater {
                        searchItems.addAll(newVideos)
                    }
                }
            }
        }

        if (enableGrid) {
            val gridState = rememberLazyGridState()

            LaunchedEffect(gridState, searchItems) {
                snapshotFlow { gridState.layoutInfo }
                    .collect { layoutInfo ->
                        val visibleIndices = layoutInfo.visibleItemsInfo.map { it.index }
                        val visibleItemCount = visibleIndices.size
                        val totalLoaded = searchItems.size

                        shownItems = "$totalLoaded/${numberOfResults.value}"

                        val lastVisible = visibleIndices.lastOrNull()
                        if (lastVisible != null && lastVisible >= totalLoaded - 1) {
                            scope.launch(Dispatchers.IO) { loadMore() }
                        }
                    }
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(searchItems.size) { index ->
                    GridItemView(searchItems[index],responsejson){
                        showDialog=it
                    }
                }
            }
            //update shownItem which is remember string

        } else {
            val listState = rememberLazyListState()

            LaunchedEffect(listState, searchItems) {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.index } }
                    .collect { visibleIndices ->
                        val visibleItemCount = visibleIndices.size
                        val totalLoaded = searchItems.size

                        shownItems = "$totalLoaded/${numberOfResults.value}"

                        val lastVisible = visibleIndices.lastOrNull()
                        if (lastVisible != null && lastVisible >= totalLoaded - 1) {
                            scope.launch(Dispatchers.IO) { loadMore() }
                        }
                    }
            }


            LazyColumn(state = listState) {
                itemsIndexed(searchItems) { _, jsonObject ->
                    ColumnItem(jsonObject,responsejson){
                        showDialog=it
                    }
                }
            }
            //update shownItem which is remember string
        }


    }
}

@Composable
fun ColumnItem(item: JSONObject,streamingData: MutableState<JSONObject>,onDialogStateChange: (Boolean) -> Unit) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val videoId = item.optString("videoId", "")
    val title = item.optString("title", "")
    val duration = item.optString("duration", "")
    val scope = rememberCoroutineScope()
    val scrapper=Interact()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(videoId) {
        if (videoId.isBlank()) return@LaunchedEffect

        val dir = File(System.getProperty("user.home"), "ComposeDownloader/thumbnails")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "$videoId.jpg")

        if (file.exists()) {
            imageBitmap = FileInputStream(file).use { loadImageBitmap(it) }
            return@LaunchedEffect
        }

        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        try {
            withContext(Dispatchers.IO) {
                URL(thumbnailUrl).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            imageBitmap = FileInputStream(file).use { loadImageBitmap(it) }
        } catch (e: Exception) {
            println("Failed to load image: ${e.message}")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                showDialog=true
            }
    ) {
        Box(
            modifier = Modifier
                .size(height = 120.dp, width = 150.dp)
                .background(Color.DarkGray)
        ) {
            imageBitmap?.let {
                Image(
                    painter = BitmapPainter(it),
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (duration.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = duration,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
        )
    }
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    imageBitmap?.let {
                        Box(
                            modifier = Modifier
                                .height(200.dp)
                                .width(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = BitmapPainter(it),
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = duration,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val result = scrapper.getStreamingData(item.getString("videoId"))
                                        streamingData.value = result
                                        onDialogStateChange(true)
                                        showDialog=false
                                    }

                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                            ) {
                                Text("Download", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                            ) {
                                Text("Close", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}


fun chooseTxtFile(): File? {
    val fileDialog = FileDialog(null as Frame?, "Choose a .txt File", FileDialog.LOAD)
    fileDialog.isVisible = true
    return if (fileDialog.file != null) {
        val path = File(fileDialog.directory, fileDialog.file)
        if (path.extension.equals("txt", ignoreCase = true)) path else null
    } else {
        null
    }
}
fun generateOutputPathDribble(downloadUrl: String, outputFolder: String): String {
    val regex = Regex("userupload/(\\d+)")
    val match = regex.find(downloadUrl)
    val uniqueId = match?.groupValues?.get(1) ?: "unknown"

    val originalName = File(URI(downloadUrl).path).name
    val fileName = "${uniqueId}_$originalName"

    val outputPath = File(outputFolder, fileName).path
    return outputPath
}
fun downloadItem(json: JSONObject,downloadItem: DownloadItem,itag: Int){
    val dir = File(System.getProperty("user.home"), "ComposeDownloader/tempFiles")
    if (!dir.exists()) {
        dir.mkdirs() // creates parent folders if necessary
    }
    var audioSize = 0L
    var videoSize = 0L
    var audioUrl = ""
    var videoUrl = ""
    var vp9 = false
    var resolution="Audio"

    val adaptiveFormats = json.getJSONObject("streamingData").getJSONArray("adaptiveFormats")
    val scrapper=Interact()
    val title=scrapper.txt2filename(json.getJSONObject("videoDetails").getString("title"))

    for (index in 0 until adaptiveFormats.length()) {
        val fmt = adaptiveFormats.getJSONObject(index)
        val itagValue = fmt.getInt("itag")
        val mimeType = fmt.optString("mimeType", "")
        val url = fmt.optString("url", "")
        val contentLength = fmt.optString("contentLength", "0").toLongOrNull() ?: 0L

        // If user selected this itag
        if (itagValue == itag) {
            if (mimeType.contains("video")) {
                videoUrl = url
                videoSize = contentLength
                vp9 = mimeType.contains("webm")
                resolution=fmt.getString("qualityLabel")
            } else if (mimeType.contains("audio")) {
                audioUrl = url
                audioSize = contentLength
                resolution=fmt.getString("qualityLabel")
            }
        }

        // Auto-pick best audio based on video codec if video selected
        if (videoUrl.isNotEmpty() && mimeType.contains("audio")) {
            if ((vp9 && itagValue == 251) || (!vp9 && itagValue == 140)) {
                audioUrl = url
                audioSize = contentLength
            }
        }
    }
    val userHome = System.getProperty("user.home")
    val downloadsFolder = Paths.get(userHome, "Downloads").toString()

    if (videoUrl.isEmpty()) {
        downloadItem.suffix="Downloading Audio"
        downloadItem.inRam.value=0
        downloadItem.onDisk.value=0
        downloadItem.onWeb.value=0
        downloadItem.percentage.value=0f
        downloadItem.fileName.value="$title($resolution).mp3"
        val audioTempFile = File(downloadsFolder, downloadItem.fileName.value)
        if (audioTempFile.exists()){
            downloadItem.onWeb.value=audioSize
            downloadItem.onDisk.value=audioTempFile.length()
            downloadAs9MB(audioUrl,downloadItem, FileOutputStream(audioTempFile,true))
        }else{
            downloadItem.onWeb.value=audioSize
            downloadAs9MB(audioUrl,downloadItem, FileOutputStream(audioTempFile))
        }
    } else {
        downloadItem.fileName.value="$title($resolution).mp4"
        val finalVideoFile = File(downloadsFolder, downloadItem.fileName.value)

        val videoTempFile = File(dir, "${downloadItem.videoId}($itag).mp4")
        val audioTempFile = File(dir, "${downloadItem.videoId}($itag).mp3")
        if (videoTempFile.exists()){
            downloadItem.suffix="Downloading Video"
           downloadItem.onDisk.value=videoTempFile.length()
           val fos= FileOutputStream(videoTempFile,true)
           downloadItem.onWeb.value=videoSize
            downloadItem.totalBytes=videoSize
           downloadAs9MB(videoUrl,downloadItem,fos)
            downloadItem.totalBytes=audioSize
           downloadItem.suffix="Downloading Audio"
           downloadItem.inRam.value=0
           downloadItem.onDisk.value=0
           downloadItem.onWeb.value=0
           downloadItem.percentage.value=0f
           if (audioTempFile.exists()){
               downloadItem.onWeb.value=audioSize
               downloadItem.onDisk.value=audioTempFile.length()
               downloadAs9MB(audioUrl,downloadItem, FileOutputStream(audioTempFile,true))
           }else{
               downloadItem.onWeb.value=audioSize
               downloadAs9MB(audioUrl,downloadItem, FileOutputStream(audioTempFile))
           }
       }else{
            downloadItem.suffix="Downloading Video"
           val fos= FileOutputStream(videoTempFile)
            downloadItem.onWeb.value=videoSize
            downloadItem.totalBytes=videoSize
            downloadAs9MB(videoUrl,downloadItem,fos)
           /* djDownloader(videoUrl,fos,0L,videoSize){progress,percent,speed->
                val output = String.format("\r%-20s %3d%% %-10s", progress, percent, speed)
                print(output)

            }*/
            downloadItem.suffix="Downloading Audio"
            downloadItem.totalBytes=audioSize
           downloadItem.inRam.value=0
           downloadItem.onDisk.value=0
           downloadItem.onWeb.value=0
           downloadItem.percentage.value=0f
           if (audioTempFile.exists()){
               downloadItem.onWeb.value=audioSize
               downloadItem.onDisk.value=audioTempFile.length()
               downloadAs9MB(audioUrl,downloadItem, FileOutputStream(audioTempFile,true))
           }else{
               downloadItem.onWeb.value=audioSize
               downloadAs9MB(audioUrl,downloadItem, FileOutputStream(audioTempFile))
               /*djDownloader(audioUrl,FileOutputStream(audioTempFile),0L,audioSize){progress,percent,speed->
                   val output = String.format("\r%-20s %3d%% %-5s", progress, percent, speed)
                   print(output)

               }*/
           }
           if (vp9){
               val muxer=WebMMuxer()
               val appTempDir = File(System.getProperty("user.home"), "ComposeDownloader/tempFiles")
               val tempFile = File(appTempDir, "temp.temp")
               downloadItem.downloadProgress.value="Merging..."
               println(tempFile.absoluteFile.toString())
               val isMuxed=muxer.setup(videoTempFile.absoluteFile.toString(),audioTempFile.absolutePath,finalVideoFile.absoluteFile.toString(),tempFile.absoluteFile.toString())
               if (isMuxed==0){
                   downloadItem.downloadProgress.value="Finished ${convertBytes(finalVideoFile.length())}"
                   println("deleting")
                   videoTempFile.delete()
                   audioTempFile.delete()
               }

           }else{
               val videoSource= DashedParser(videoTempFile)
               val audioSource= DashedParser(audioTempFile)
               downloadItem.downloadProgress.value="Parsing"
               videoSource.parse()
               audioSource.parse()

               val writter= DashedWriter(finalVideoFile,listOf(videoSource,audioSource), progress = {
                   downloadItem.downloadProgress.value=it
                   if (it=="Finished"){
                       videoSource.reader.close()
                       audioSource.reader.close()
                       videoTempFile.delete()
                       audioTempFile.delete()
                       downloadItem.downloadProgress.value="Finished ${convertBytes(finalVideoFile.length())}"
                   }
               })
               writter.buildNonFMp4()

           }

       }


    }




}

fun djDownloader( url: String,
                  fos: FileOutputStream,
                  onDisk: Long,
                  totalBytes: Long,
                  progress: (dbyt: String, percent: Int, speed: String) -> Unit){
    val chunkSize = 9437184L  // 9MB
    val start = onDisk
    val end = minOf(start + chunkSize - 1, totalBytes - 1)

    val request = Request.Builder()
        .url(url)
        .addHeader("Range", "bytes=$start-$end")
        .build()

    val response = dclient.newCall(request).execute()

    if (response.code == 206) {
        response.body.byteStream().use { inputStream ->
            val buffer = ByteArray(1024)
            var bytesRead: Int

            var downloadedInChunk = 0L
            var speedBytes = 0L
            var lastTime = System.currentTimeMillis()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fos.write(buffer, 0, bytesRead)
                downloadedInChunk += bytesRead
                speedBytes += bytesRead

                val currentDownloaded = onDisk + downloadedInChunk
                val percent = ((currentDownloaded * 100) / totalBytes).toInt()
                val now = System.currentTimeMillis()

                if (now - lastTime >= 1000) {
                    val speedText = convertSpeed(speedBytes)
                    val pg = "${convertBytes(currentDownloaded)}/${convertBytes(totalBytes)}"
                    progress(pg, percent, speedText)
                    speedBytes = 0
                    lastTime = now
                }
            }

            // Final update after chunk
            val finalDownloaded = onDisk + downloadedInChunk
            val pg = "${convertBytes(finalDownloaded)}/${convertBytes(totalBytes)}"
            val percent = ((finalDownloaded * 100) / totalBytes).toInt()
            progress(pg, percent, convertSpeed(speedBytes))

            if (finalDownloaded < totalBytes) {
                djDownloader(url, fos, finalDownloaded, totalBytes, progress)
            }
        }
    } else {
        println("HTTP error: Expected 206 Partial Content, got ${response.code}")
    }

}
fun convertSpeed(bytesPerSec: Long): String {
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        bytesPerSec >= gigabyte -> "${(bytesPerSec / gigabyte).roundToInt()} GB/s"
        bytesPerSec >= megabyte -> "${(bytesPerSec / megabyte).roundToInt()} MB/s"
        bytesPerSec >= kilobyte -> "${(bytesPerSec / kilobyte).roundToInt()} KB/s"
        else -> "$bytesPerSec B/s"
    }
}
@Composable
fun GridItemView(item: JSONObject,streamingData: MutableState<JSONObject>,onDialogStateChange: (Boolean) -> Unit) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val videoId = item.getString("videoId")
    val title = item.getString("title")
    val duration = item.getString("duration")
    val scope = rememberCoroutineScope()
    val scrapper=Interact()

    var showDialog by remember { mutableStateOf(false) }

    // Load or download image
    LaunchedEffect(videoId) {
        val appDataDir = File(System.getProperty("user.home"), "ComposeDownloader/thumbnails")
        if (!appDataDir.exists()) appDataDir.mkdirs()

        val file = File(appDataDir, "$videoId.jpg")
        if (file.exists()) {
            imageBitmap = FileInputStream(file).use { loadImageBitmap(it) }
            return@LaunchedEffect
        }

        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        try {
            withContext(Dispatchers.IO) {
                URL(thumbnailUrl).openStream().use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
            }
            if (file.exists()) {
                imageBitmap = FileInputStream(file).use { loadImageBitmap(it) }
            }
        } catch (e: Exception) {
            println("Failed to download image: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color.DarkGray)
            .clickable { showDialog = true }
    ) {
        imageBitmap?.let {
            Image(
                painter = BitmapPainter(it),
                contentDescription = "Grid Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(4.dp)
        ) {
            Text(
                text = duration,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    // ✅ Dialog for details
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    imageBitmap?.let {
                        Box(
                            modifier = Modifier
                                .height(200.dp)
                                .width(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = BitmapPainter(it),
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = duration,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val result = scrapper.getStreamingData(item.getString("videoId"))
                                        streamingData.value = result
                                        onDialogStateChange(true)
                                        showDialog=false
                                    }

                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                            ) {
                                Text("Download", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                            ) {
                                Text("Close", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun individualItem(item: DownloadItem){
    var imageBitmap by remember(item.videoId) { mutableStateOf<ImageBitmap?>(null) }

    // Load image when the item appears
    LaunchedEffect(item.videoId) {
        if (item.videoId=="ddr.png"){
            val file = File("src/main/resources/${item.videoId}")
            if (file.exists()) {
                imageBitmap = FileInputStream(file).use {
                    loadImageBitmap(it)
                }
            }
        }else{
            val appDataDir = File(System.getProperty("user.home"), "ComposeDownloader/thumbnails")
            val file = File(appDataDir, "${item.videoId}.jpg")
            if (file.exists()) {
                imageBitmap = FileInputStream(file).use {
                    loadImageBitmap(it)
                }
            }
        }

    }
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription =item.fileName.value,
                modifier = Modifier
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = item.fileName.value, color = Color.White)
            Text(text = item.downloadProgress.value, color = Color.White)
            Row {
                Button(
                    modifier = Modifier
                        .padding(end = 10.dp),
                    onClick = {
                        item.isDownloading.value=false
                    }
                ) {
                    Text("Cancel")
                }
                Button(
                    modifier = Modifier
                        .padding(end = 10.dp),
                    onClick = {
                       println("opening")
                    }
                ) {
                    Text("Open")
                }
                LinearProgressIndicator(
                    progress = item.percentage.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    color = Color.White,
                    backgroundColor = Color.Gray
                )
            }

        }
    }

}
@Composable
fun askFormat(){

}
fun convertBytes(sizeInBytes: Long): String {
    val kilobyte = 1024
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        sizeInBytes >= gigabyte -> String.format("%.2f GB", sizeInBytes.toDouble() / gigabyte)
        sizeInBytes >= megabyte -> String.format("%.2f MB", sizeInBytes.toDouble() / megabyte)
        sizeInBytes >= kilobyte -> String.format("%.2f KB", sizeInBytes.toDouble() / kilobyte)
        else -> "$sizeInBytes Bytes"
    }
}

val dclient = OkHttpClient.Builder()
    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
    .build()

fun downloadAs9MB(url: String, downloadItem: DownloadItem, fos: FileOutputStream) {
    val chunkSize = 9437184L // 9MB
    val start = downloadItem.onDisk.value + downloadItem.inRam.value
    val end = minOf(start + chunkSize - 1, downloadItem.totalBytes - 1)

    val request = Request.Builder()
        .url(url)
        .addHeader("Range", "bytes=$start-$end")
        .build()

    try {
        val response = dclient.newCall(request).execute()
        if (response.code == 206 || response.code == 200) {
            val inputStream = response.body?.byteStream() ?: throw IOException("Empty response body")
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (!downloadItem.isDownloading.value) return
                fos.write(buffer, 0, bytesRead)
                downloadItem.inRam.value += bytesRead

                val totalDownloaded = downloadItem.onDisk.value + downloadItem.inRam.value
                downloadItem.percentage.value = ((totalDownloaded.toFloat() / downloadItem.totalBytes.toFloat()) * 100).toInt().toFloat() / 100f
                downloadItem.downloadProgress.value =
                    "Progress ${convertBytes(totalDownloaded)}/${convertBytes(downloadItem.totalBytes)}  ${downloadItem.percentage.value}%  ${downloadItem.suffix}"
            }

            if (downloadItem.onDisk.value + downloadItem.inRam.value == downloadItem.totalBytes) {
                return // Done
            } else {
                downloadAs9MB(url, downloadItem, fos) // Continue next chunk
            }

        } else {
            downloadItem.suffix = "HTTP ${response.code} Error"
        }

    } catch (e: java.net.SocketTimeoutException) {
        downloadItem.suffix = "Retrying after timeout..."
        downloadAs9MB(url, downloadItem, fos) // Retry
    } catch (e: IOException) {
        downloadItem.suffix = "IO Error: ${e.message}"
    } catch (e: Exception) {
        downloadItem.suffix = "Error: ${e.message}"
    }
}

@Composable
fun askResolution(resp: JSONObject, onDismiss: () -> Unit,selectedItag:(slectedItag: Int)-> Unit) {
    val playerResponse = resp.getJSONObject("playerResponse")
    if (playerResponse.has("streamingData")){
        val title = playerResponse.getJSONObject("videoDetails").getString("title")
        val videoId = playerResponse.getJSONObject("videoDetails").getString("videoId")

        var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        var selectedType by remember { mutableStateOf("video") }
        val adaptiveFormats=playerResponse.getJSONObject("streamingData").getJSONArray("adaptiveFormats")
        val scrapper = Interact()

        LaunchedEffect(videoId) {
            val dir = File(System.getProperty("user.home"), "ComposeDownloader/thumbnails")
            if (!dir.exists()) {
                dir.mkdirs() // creates parent folders too
            }
            val file = File(dir,"$videoId.jpg")
            scrapper.downloadImage("https://img.youtube.com/vi/$videoId/hqdefault.jpg", file)
            val decoded = withContext(Dispatchers.IO) {
                FileInputStream(file).use { loadImageBitmap(it) }
            }
            bitmap = decoded
        }

        Dialog(onDismissRequest = onDismiss
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1D161D),
                modifier = Modifier
                    .height(500.dp)
                    .clip(RoundedCornerShape(16.dp)) // removes corner artifacts
                    .background(Color(0xFF1D161D))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .background(Color(0xFF1D161D))
                ) {

                    // Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bitmap?.let {
                            Image(
                                bitmap = it,
                                contentDescription = title,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Duration: 5:42", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Format selection (video/audio)
                    Text(
                        "Select Format",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("video" to "Video", "audio" to "Audio").forEach { (type, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White,
                                        unselectedColor = Color.LightGray
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, color = Color.White)
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                    ){
                        items(adaptiveFormats.length()){index->
                            val fmt = adaptiveFormats.getJSONObject(index)
                            if (fmt.getString("mimeType").contains(selectedType)){
                                val itag = fmt.optInt("itag")
                                val mimeType = fmt.optString("mimeType")
                                if (mimeType.contains("video")){
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                onDismiss()
                                                selectedItag(itag)
                                            }

                                    ){
                                        Image(
                                            modifier = Modifier
                                                .background(Color.White),
                                            bitmap = useResource(
                                                "edit.png",
                                                block = { loadImageBitmap(it) }
                                            ),
                                            contentDescription = "music"

                                        )
                                        val fmtText="${fmt.getString("qualityLabel")} $mimeType ${scrapper.formatSpeed(fmt.getString("contentLength").toLong())}"
                                        Text(
                                            text =fmtText,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                            , color = Color.White
                                        )
                                    }
                                }else{
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                selectedItag(itag)
                                            }

                                    ){
                                        Image(
                                            modifier = Modifier
                                                .background(Color.White),
                                            bitmap = useResource(
                                                "musiedit.png",
                                                block = { loadImageBitmap(it) }
                                            ),
                                            contentDescription = "music"

                                        )
                                        var fmtText="$itag $mimeType ${scrapper.formatSpeed(fmt.getString("contentLength").toLong())}"
                                        if (itag==140||itag==251){
                                            fmtText="$itag $mimeType ${scrapper.formatSpeed(fmt.getString("contentLength").toLong())} \uD83D\uDC4D"
                                        }
                                        Text(
                                            text =fmtText,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                            , color = Color.White
                                        )
                                    }
                                }
                            }

                        }

                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF45292E),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Close")
                        }
                    }

                }
            }
        }
    }else{
        var showDialog by remember { mutableStateOf(false) }
        if (showDialog){
            AlertDialog(
                onDismissRequest ={
                    showDialog=false
                },
                title = { Text("Formats Not Found") },
                text = { Text("Failed To get Formats") },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
@Composable
fun MessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


@Composable
fun ProfileScreen(items: SnapshotStateList<DownloadItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D161D))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "My Downloads",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(items) { index, item ->
                individualItem(item)
            }
        }
    }
}


@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Adjust your Settings here.", fontSize = 20.sp, color = Color.Blue)
    }
}


@Composable

fun App() {
    var selectedScreen by remember { mutableStateOf("Home") }

    MaterialTheme {
        Row(
            modifier= Modifier
                .fillMaxSize()
                .background(Color(0xFF1D161D)
                )
        ) {
            SideNavigation(selected = selectedScreen, onSelect = { selectedScreen = it })
            Surface(modifier = Modifier.weight(1f)) {
                ContentArea(selected = selectedScreen)
            }
        }
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }



}
