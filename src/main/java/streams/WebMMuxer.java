package streams;

import streams.WebMReader.TrackKind;
import streams.WebMReader.WebMTrack;
import streams.io.SharpStream;

import java.io.File;
import java.io.IOException;


/**
 * @author kapodamy
 */
 public class WebMMuxer  {
     public int setup(String videoPath, String audioPath, String merged, String tempfile) throws IOException {
         File videoFile = new File(videoPath);
         File audioFile = new File(audioPath);
         File outputFile = new File(merged);
         File tempFile = new File(tempfile);
         ChunkFileInputStream[] sources = new ChunkFileInputStream[2];

         ProgressReport progress = pos -> System.out.println("Written: Video" + pos + " bytes");
         ProgressReport progress1 = pos -> System.out.println("Written: Audio" + pos + " bytes");

         sources[0] = new ChunkFileInputStream(new FileStream(videoFile), 0, videoFile.length(), progress);
         sources[1] = new ChunkFileInputStream(new FileStream(audioFile), 0, audioFile.length(), progress1);
         CircularFileWriter.OffsetChecker checker = () -> -1; // no limit
         SharpStream outputStream = new FileStream(outputFile);

         CircularFileWriter writer = new CircularFileWriter(outputStream, tempFile, checker);
         process(writer,sources);
         return 0;

     }
    public int process(SharpStream out, SharpStream... sources) throws IOException {
        WebMWriter muxer = new WebMWriter(sources);
        muxer.parseSources();

        int[] indexes = new int[sources.length];
        for (int i = 0; i < sources.length; i++) {
            WebMTrack[] tracks = muxer.getTracksFromSource(i);
            for (int j = 0; j < tracks.length; j++) {
                if (tracks[j].kind == TrackKind.Audio) {
                    indexes[i] = j;
                    i = sources.length;
                    break;
                }
            }
        }
        muxer.selectTracks(indexes);
        muxer.build(out);
        return 0;


    }

}
