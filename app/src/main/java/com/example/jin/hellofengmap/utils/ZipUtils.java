package com.example.jin.hellofengmap.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description Android Zip压缩解压缩
 */
public class ZipUtils {

    /**
     * 取得压缩包中的 文件列表(文件夹,文件自选)
     *
     * @param zipFileString  压缩包名字
     * @param bContainFolder 是否包括 文件夹
     * @param bContainFile   是否包括 文件
     * @return
     * @throws IOException
     */
    public static List<File> getFileList(String zipFileString,
                                         boolean bContainFolder, boolean bContainFile) throws IOException {

        List<File> fileList = new ArrayList<File>();
        ZipInputStream inZip = new ZipInputStream(
                new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String zipName = "";

        while ((zipEntry = inZip.getNextEntry()) != null) {
            zipName = zipEntry.getName();

            if (zipEntry.isDirectory()) {
                // 压缩文件名称
                zipName = zipName.substring(0, zipName.length() - 1);
                File folder = new File(zipName);
                if (bContainFolder) {
                    fileList.add(folder);
                }
            } else {
                File file = new File(zipName);
                if (bContainFile) {
                    fileList.add(file);
                }
            }
        }

        inZip.close();

        return fileList;
    }

    /**
     * 解压zip包
     *
     * @param zipDirectory     压缩目录
     * @param storageDirectory 保存目录
     * @param fileName         文件名称
     * @return
     */
    public static boolean unZip(String zipDirectory, String storageDirectory,
                                String fileName) {
        int buffer = 2048;
        boolean result = false;
        // 压缩文件名
        String zipName = zipDirectory + fileName;
        String fileDir = storageDirectory;
        ZipFile zipFile = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            zipFile = new ZipFile(zipName);
            Enumeration<? extends ZipEntry> emum = zipFile.entries();
            while (emum.hasMoreElements()) {
                ZipEntry entry = emum.nextElement();
                if (entry.isDirectory()) {
                    // 目录创建
                    new File(fileDir + entry.getName()).mkdirs();
                    continue;
                }
                // 读取压缩文件
                bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(fileDir + entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                // 写入压缩文件
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos, buffer);
                int count;
                byte data[] = new byte[buffer];
                while ((count = bis.read(data, 0, buffer)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    /**
     * 解压一个压缩文档 到指定位置
     *
     * @param zipFileString 压缩包的名字
     * @param outPathString 指定的路径
     * @throws IOException
     */
    public static void unZipFolder(String zipFileString, String outPathString)
            throws IOException {

        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String zipName = "";

        while ((zipEntry = inZip.getNextEntry()) != null) {
            zipName = zipEntry.getName();

            if (zipEntry.isDirectory()) {
                // 压缩文件夹
                zipName = zipName.substring(0, zipName.length() - 1);
                File folder = new File(outPathString
                        + File.separator + zipName);
                folder.mkdirs();
            } else {
                // 压缩文件
                File file = new File(outPathString + File.separator + zipName);
                file.getParentFile().mkdirs();
                file.createNewFile();
                // 写入压缩文件
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];

                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }

        inZip.close();
    }

    /**
     * 返回压缩包中的文件InputStream
     *
     * @param zipFileString 压缩文件的名字
     * @param fileString    解压文件的名字
     * @return InputStream
     * @throws IOException
     */
    public static InputStream upZip(String zipFileString,
                                    String fileString) throws IOException {
        ZipFile zipFile = new ZipFile(zipFileString);
        ZipEntry zipEntry = zipFile.getEntry(fileString);

        return zipFile.getInputStream(zipEntry);

    }

    /**
     * 压缩文件
     *
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws IOException
     */
    private static void zipFiles(String folderString, String fileString,
                                 ZipOutputStream zipOutputSteam) throws IOException {

        if (zipOutputSteam == null) {
            return;
        }

        File file = new File(folderString + fileString);

        // 判断是不是文件
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(
                    file);
            zipOutputSteam.putNextEntry(zipEntry);

            int len;
            byte[] buffer = new byte[4096];

            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }

            zipOutputSteam.closeEntry();
            inputStream.close();
        } else {
            // 文件夹的方式,获取文件夹下的子文件
            String fileList[] = file.list();

            // 如果没有子文件, 则添加进去即可
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(
                        fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }

            // 如果有子文件, 遍历子文件
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderString, fileString + File.separator
                        + fileList[i], zipOutputSteam);
            }

        }

    }

    /**
     * 压缩文件,文件夹
     *
     * @param srcFileString 要压缩的文件/文件夹名字
     * @param zipFileString 指定压缩的目的和名字
     * @throws IOException
     */
    public static void zipFolder(String srcFileString, String zipFileString)
            throws IOException {
        // 创建Zip包
        ZipOutputStream outZip = new ZipOutputStream(
                new FileOutputStream(zipFileString));
        // 打开要输出的文件
        File file = new File(srcFileString);
        // 压缩
        zipFiles(file.getParent() + File.separator, file.getName(),
                outZip);
        // 完成,关闭
        outZip.finish();
        outZip.close();
    }

}
