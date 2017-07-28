package com.example.jin.hellofengmap.utils;

import android.content.Context;

import com.fengmap.android.data.FMDataManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description  文件操作类
 */
public class FileUtils {

    /**
     * 主题文件类型
     */
    public static final String FILE_TYPE_THEME = ".theme";

    /**
     * 地图文件类型
     */
    public static final String FILE_TYPE_MAP = ".fmap";

    /**
     * 默认地图
     */
    public static final String DEFAULT_MAP_ID = "10347";

    /**
     * 默认主题
     */
    public static final String DEFAULT_THEME_ID = "3007";

    /**
     * 通过主题id获取主题路径
     *
     * @param themeId 主题id
     * @return
     */
    public static String getThemePath(String themeId) {
        String themePath = FMDataManager.getFMThemeResourceDirectory() + themeId + File.separator + themeId + FILE_TYPE_THEME;
        return themePath;
    }

    /**
     * 通过地图id获取地图文件路径
     *
     * @param mapId 地图id
     * @return
     */
    public static String getMapPath(String mapId) {
        String mapPath = FMDataManager.getFMMapResourceDirectory() + mapId + File.separator + mapId + FILE_TYPE_MAP;
        return mapPath;
    }

    /**
     * 获取默认地图文件路径
     *
     * @param context 上下文
     * @return
     * @throws IOException
     */
    public static String getDefaultMapPath(Context context) {
        String srcFile = DEFAULT_MAP_ID + FILE_TYPE_MAP;
        String destFile = getMapPath(DEFAULT_MAP_ID);
        try {
            FileUtils.copyAssetsToSdcard(context, srcFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFile;
    }

    /**
     * 复制assets下文件到sdcard下文件
     *
     * @param context
     * @param srcFileName  复制源文件
     * @param destFileName 复制至sdcard文件
     * @throws IOException
     */
    public static void copyAssetsToSdcard(Context context, String srcFileName, String destFileName) throws IOException {
        File file = new File(destFileName);
        File parentFile = file.getParentFile();
        if (parentFile != null & !parentFile.exists()) {
            parentFile.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        } else {
            return;
        }

        InputStream is = context.getAssets().open(srcFileName);
        OutputStream os = new FileOutputStream(destFileName);

        byte[] buffer = new byte[1024];
        int byteCount = 0;
        while ((byteCount = is.read(buffer)) != -1) {
            os.write(buffer, 0, byteCount);
        }
        os.flush();
        is.close();
        os.close();
    }

    /**
     * 获取默认地图主题路径
     *
     * @param context 上下文
     * @return
     */
    public static String getDefaultThemePath(Context context) {
        return getThemePath(context, DEFAULT_THEME_ID);
    }

    /**
     * 获取本地主题路径
     *
     * @param context 上下文
     * @param themeId 主题名称
     * @return
     */
    public static String getThemePath(Context context, String themeId) {
        String path = getThemePath(themeId);
        File file = new File(path);
        if (!file.exists()) {
            copyAssetsThemeToSdcard(context);
        }
        return path;
    }

    /**
     * 将assets目录下theme.zip主题复制、解压到sdcard中
     *
     * @param context 上下文
     */
    public static void copyAssetsThemeToSdcard(Context context) {
        String srcFileName = "theme.zip";
        String themeDir = FMDataManager.getFMThemeResourceDirectory();
        String destFileName = themeDir + srcFileName;
        try {
            copyAssetsToSdcard(context, srcFileName, destFileName);
            // 解压压缩包文件并删除主题压缩包文件
            ZipUtils.unZipFolder(destFileName, themeDir);
            deleteDirectory(destFileName);

            // 遍历目录是否存在主题文件,不存在则解压
            File themeFile = new File(themeDir);
            File[] files = themeFile.listFiles();

            String extension = ".zip";
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(extension)) {
                    File f = new File(file.getName().replace(extension, ""));
                    String fileDir = file.getAbsolutePath();
                    if (!f.exists()) {
                        ZipUtils.unZipFolder(fileDir, themeDir);
                    }
                    deleteDirectory(fileDir);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param fileDir 文件夹路径
     * @return
     */
    public static boolean deleteDirectory(String fileDir) {
        if (fileDir == null) {
            return false;
        }

        File file = new File(fileDir);
        if (file == null || !file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                }
            }
        }

        file.delete();
        return true;
    }

}
