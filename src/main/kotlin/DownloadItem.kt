import androidx.compose.runtime.mutableStateOf

class DownloadItem{
    var fileName=mutableStateOf("Initializing...")
    var isDownloading= mutableStateOf(true)
    var percentage=mutableStateOf(1f)
    var inRam=mutableStateOf(0L)
    var onWeb=mutableStateOf(0L)
    val onDisk=mutableStateOf(0L)
    var isAudio=false
    var audioUrl=""
    var url=""
    var isWebm=false
    var downloadProgress=mutableStateOf("Progress...")
    var videoId=""
    var suffix=""
    var totalBytes=0L
    var audioLength=0L

}