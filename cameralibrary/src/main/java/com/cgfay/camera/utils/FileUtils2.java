package com.cgfay.camera.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.cgfay.cameralibrary.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class FileUtils2 {
    public static String ApplyFormat(long value) {
        NavigableMap<Long, String> suffixes = new TreeMap<>();
        suffixes.put(Long.valueOf(1000), "K+");
        suffixes.put(Long.valueOf(1000000), "M+");
        suffixes.put(Long.valueOf(1000000000), "B+");
        if (value == Long.MIN_VALUE) {
            return ApplyFormat(-9223372036854775807L);
        }
        if (value < 0) {
            return "-" + ApplyFormat(-value);
        }
        if (value < 1000) {
            return Long.toString(value);
        }
        Entry<Long, String> e = suffixes.floorEntry(Long.valueOf(value));
        String suffix = (String) e.getValue();
        long truncated = value / (((Long) e.getKey()).longValue() / 10);
        return (truncated > 100 ? 1 : (truncated == 100 ? 0 : -1)) < 0 && ((((double) truncated) / 10.0d) > ((double) (truncated / 10)) ? 1 : ((((double) truncated) / 10.0d) == ((double) (truncated / 10)) ? 0 : -1)) != 0 ? (((double) truncated) / 10.0d) + suffix : (truncated / 10) + suffix;
    }

    public static final String makeShortTimeString(Context context, long secs) {
        long hours = secs / 3600;
        long secs2 = secs % 3600;
        return String.format(context.getResources().getString(hours == 0 ? R.string.app_name : R.string.app_name), new Object[]{Long.valueOf(hours), Long.valueOf(secs2 / 60), Long.valueOf(secs2 % 60)});
    }

    public static void deleteFile(File f) {
        if (f.exists()) {
            if (f.isDirectory()) {

                File[] files = f.listFiles();
                if (files != null && files.length > 0) {
                    for (File deleteFile : files) {
                        deleteFile(deleteFile);
                    }
                }
            }
            f.delete();
        }
    }
    public static String getTrimmedVideoDir(Context context) {
        String dirPath = getTempHiddenDirectoryPath(context) + "/trimmedVideo";
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dirPath;
    }
    public static String getSaveEditThumbnailDir(Context context) {
        File folderDir = new File(getTempHiddenDirectoryPath(context) + File.separator + "small_video" + File.separator + "thumb");
        if (folderDir == null) {
            folderDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "videoeditor" + File.separator + "picture");
        }
        if (folderDir.exists() || folderDir.mkdirs()) {
        }
        return folderDir.getAbsolutePath();
    }
    public static String saveImageToSDForEdit(Bitmap bmp, String dirPath, String fileName) {
        if (bmp == null) {
            return "";
        }
        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
          //  ThrowableExtension.printStackTrace(e);
        }
        return file.getAbsolutePath();
    }
    public static String getTempAudioRecordPath(Context context) {
        return getTempHiddenDirectoryPath(context) + "/temp_audio.mp3";
    }
    public static String getMutedvideoPath(Context context) {
        return getTempHiddenDirectoryPath(context) + "/temp_mute_video.mp4";
    }
    public static String getExtractedAudioPath(Context context) {
        return getTempHiddenDirectoryPath(context) + "/temp_extract_audio.mp3";
    }
    public static String getCroppedVideoPath(Context context) {
        return getTempHiddenDirectoryPath(context) + "/temp_video_cropped.mp4";
    }

    public static String getTempCropAudioPath(Context context, int extra) {
        return getTempHiddenDirectoryPath(context) + "/temp_audio_cropped_" + extra + ".mp3";
    }

    public static String getExtensionFromPath(String oldPath) {
        File file = new File(oldPath);
        return file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
    }

    public static long getDurationFromFilePath(Context context, String audio_path) {
        return VideoUtils.getDuration(audio_path) / 1000;
    }

    public static String getCameraRecordFilePath(Context context, boolean withoutMusic) {
        if (withoutMusic) {
            return getTempHiddenDirectoryPath(context) + "/record_camera.mp4";
        }
        return getTempHiddenDirectoryPath(context) + "/record_camera_added_music.mp4";
    }

    public static String getCameraRecordFilePath(Context context, int numOfSegment) {

        String root = Environment.getExternalStorageDirectory().toString();
        File mFile = new File(root + "/" + context.getString(R.string.app_name) + "/.Segment");

      //  File mFile = new File(getTempHiddenDirectoryPath(context) + File.separator + "/.Segment");
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
        return mFile.getAbsolutePath() + "/Segment_" + numOfSegment + ".mp4";
    }
    public static void getCameraRecordFilePathDelete(Context context) {

        String root = Environment.getExternalStorageDirectory().toString();
        File mFile = new File(root + "/" + context.getString(R.string.app_name) + "/.Segment");

      //  File mFile = new File(getTempHiddenDirectoryPath(context) + File.separator + "/.Segment");
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
        if (mFile.isDirectory())
        {
            String[] children = mFile.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(mFile, children[i]).delete();
            }
        }
       // return mFile.getAbsolutePath() + "/Segment_" + numOfSegment + ".mp4";
    }

    public static String getCameraSegmentFolder(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File mFile = new File(root + "/" + context.getString(R.string.app_name) + "/.Segment");

       // File mFile = new File(getTempHiddenDirectoryPath(context) + File.separator + "/.Segment");
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
        return mFile.getAbsolutePath();
    }

    public static String getColorEffectDirPath(Context context) {
        File file = new File(GetResDirectory(context) + "/Color_Effect");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public static String getDownloadZipPath(Context context) {
        return getEmojiStickerDir(context) + "/EMOJIS/EMOJIS";
    }

    public static String getMagicEffectDir(Context mContext, String name) {
        File file = new File(getMagicEffectRootDir(mContext) + "/" + name);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public static String getMagicEffectRootDir(Context mContext) {
        File file = new File(GetResDirectory(mContext) + "/Magic_Effec");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public static boolean isImageFile(String str) {
        String mimeType = URLConnection.guessContentTypeFromName(str);
        boolean isImg = mimeType != null && mimeType.startsWith("image");
        if (isImg) {
            return isImg;
        }
        if (str.endsWith(".GIF") || str.endsWith(".jpg") || str.endsWith(".JPG") || str.endsWith(".jpeg") || str.endsWith(".JPEG") || str.endsWith(".png") || str.endsWith(".PNG") || str.endsWith(".gif")) {
            return true;
        }
        return isImg;
    }

    public static String getStickerDir(Context mContext, String name) {
        File file = new File(getStickerRootDir(mContext) + "/" + name);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public static String getStickerRootDir(Context mContext) {
        File file = new File(GetResDirectory(mContext) + "/Sticker");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public static String getStickerVideoPath(Context context) {
        return getAppMediaStorageDirectoryPath(context) + "/editing_sticker_video.mp4";
    }

    public static String getGIFStickerDir(Context mContext) {
        return getStickerRootDir(mContext) + "/GIFS/GIFS";
    }

    public static String getEmojiStickerDir(Context mContext) {
        return getAppMediaStorageDirectoryPath(mContext) + "/EMOJIS/EMOJIS";
    }

    public static String getVideoCoverPath(Context context) {
        return getTempHiddenDirectoryPath(context) + "/video_cover.jpg";
    }

    public static String getFinalAudio(Context mContext) {
        return getTempHiddenDirectoryPath(mContext) + "/final_audio.mp3";
    }

    public static String getDownloadingMusicPath(Context mContext) {
        return getTempHiddenDirectoryPath(mContext) + "/downloaded_audio.mp3";
    }
    public static String getManagedSpeedVideoPath(Context mContext) {
        return getTempHiddenDirectoryPath(mContext) + "/vid_speed_video.mp4";
    }

    public static String getTempRecordingPath(Context context) {
        return getTempHiddenDirectoryPath(context) + "/editing_record" + System.currentTimeMillis() + ".mp4";
    }

    public static String getAppMediaStorageDirectoryPath(Context context) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/" + context.getText(R.string.app_name);
        File dirfile = new File(path);
        if (!dirfile.exists()) {
            dirfile.mkdir();
        }
        return path;
    }

    public static String getFinalVideoSavePath(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/" + context.getString(R.string.app_name) + "/UserVideos");
        if (!file.exists()) {
            file.mkdir();
        }
        String path = file.getAbsolutePath() + "/final_video_app.mp4";
        File finalFile = new File(path);
        if (finalFile.exists()) {
           // finalFile.delete();
        }
        return path;
    }
    public static String getFinalUploadVideo(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/" + context.getString(R.string.app_name) + "/Upload");
        if (!file.exists()) {
            file.mkdir();
        }
        String path = file.getAbsolutePath() + "/final_video_app.mp4";
        File finalFile = new File(path);
        if (finalFile.exists()) {
           // finalFile.delete();
        }
        return path;
    }

    public static String getEmojiSticker(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/" + context.getString(R.string.app_name) + "/Emoji");
        if (!file.exists()) {
            file.mkdir();
        }
        return String.valueOf(file);
    }


    public static void deleteWholefile(Context context)
    {



        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/" + context.getString(R.string.app_name) + "/UserVideos");
        if (file.isDirectory())
        {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(file, children[i]).delete();
            }
        }

        File dir = new File(Environment.getExternalStorageDirectory()+context.getString(R.string.app_name));
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }

    public static String GetResDirectory(Context context) {
        File file = new File(getAppHidenDir(context) + "/RES");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static String getAppHidenDir(Context context) {


        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/" + context.getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
        //return context.getFilesDir().getAbsolutePath();
    }

    public static String getTempHiddenDirectoryPath(Context context) {
        String path = getAppHidenDir(context) + "/tmp";
        File dirfile = new File(path);
        if (!dirfile.exists()) {
            dirfile.mkdir();
        }
        return path;
    }

    public static String getDownloadVideoSavePath(Activity activity, boolean withVidName, boolean isTemporaryStore) {
        File file;
        if (isTemporaryStore) {
            file = new File(getTempHiddenDirectoryPath(activity));
        } else {
            file = new File(getAppMediaStorageDirectoryPath(activity));
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!withVidName) {
            return file.getAbsolutePath();
        }
        if (isTemporaryStore) {
            return file.getAbsolutePath() + "/" + activity.getString(R.string.app_name) + "_" + System.currentTimeMillis() + "_temp.mp4";
        }
        return file.getAbsolutePath() + "/" + activity.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".mp4";
    }

    public static String GetVideoName(Activity activity, String id) {
        String[] fileName = id.split("-");

        int time = (int) (System.currentTimeMillis());
        Timestamp tsTemp = new Timestamp(time);
        String ts =  tsTemp.toString();

        return activity.getString(R.string.app_name) + "_" + id + ".mp4";
    }
}
