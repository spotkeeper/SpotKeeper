package com.anrapps.spotkeeper.util;

import android.os.Environment;

import com.anrapps.spotkeeper.entity.Track;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    //sdcard/SpotKeeper/artist/album/track.aac
    private static final String FILE_PATH_FORMAT = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/SpotKeeper/%s/%s/%s.aac";

    public static File getFileForTrack(final Track track) {
        final String artistName = track.artistName;
        final String albumName = track.albumName;
        final String trackName = track.name;
        return new File(String.format(FILE_PATH_FORMAT,
                artistName.replaceAll("/", "-"),
                albumName.replaceAll("/", "-"),
                trackName.replaceAll("/", "-")));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void mkDirsAndFiles(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileForTrack(final Track track) {
        File trackFile = getFileForTrack(track);
        //noinspection ResultOfMethodCallIgnored
        trackFile.delete();
    }

    public static boolean trackFileExists(final Track track) {
        return getFileForTrack(track).exists();
    }
}
