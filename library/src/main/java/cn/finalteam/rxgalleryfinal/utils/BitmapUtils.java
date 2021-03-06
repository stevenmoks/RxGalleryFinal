package cn.finalteam.rxgalleryfinal.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Desction:Bitmap处理工具类,图片压缩、裁剪、选择、存储
 * Author:pengjianbo
 * Date:16/5/4 下午5:03
 */
public class BitmapUtils {

    private final static int THUMBNAIL_BIG = 1;
    private final static int THUMBNAIL_SMALL = 2;

    public static String getVideoThumbnailBigPath(String thumbnailSaveDir, String originalPath) {
        return createVideoThumbnail(thumbnailSaveDir, originalPath, THUMBNAIL_BIG);
    }

    public static String getVideoThumbnailSmallPath(String thumbnailSaveDir, String originalPath) {
        return createVideoThumbnail(thumbnailSaveDir, originalPath, THUMBNAIL_SMALL);
    }

    /**
     * 创建视频缩略图
     * @param thumbnailSaveDir
     * @param originalPath
     * @param scale
     * @return
     */
    public static String createVideoThumbnail(String thumbnailSaveDir, String originalPath, int scale) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(originalPath, MediaStore.Video.Thumbnails.MINI_KIND);
        if(bitmap == null){
            return "";
        }
        int originalImageWidth = bitmap.getWidth();
        int originalImageHeight = bitmap.getHeight();
        int maxValue = Math.max(originalImageWidth, originalImageHeight);
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        File targetFile = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (maxValue > 3000) {
                options.inSampleSize = scale * 6;
            } else if (maxValue > 2000 && maxValue <= 3000) {
                options.inSampleSize = scale * 5;
            } else if (maxValue > 1500 && maxValue <= 2000) {
                options.inSampleSize = scale * 4;
            } else if (maxValue > 1000 && maxValue <= 1500) {
                options.inSampleSize = scale * 3;
            } else if (maxValue > 400 && maxValue <= 1000) {
                options.inSampleSize = scale * 2;
            } else {
                options.inSampleSize = scale;
            }
            options.inJustDecodeBounds = false;

            //4、图片方向纠正和压缩(生成缩略图)
            bufferedInputStream = new BufferedInputStream(new FileInputStream(originalPath));
            Bitmap bm = BitmapFactory.decodeStream(bufferedInputStream, null, options);
            bufferedInputStream.close();
            bitmap.recycle();
            if (bm == null) {
                return "";
            }
            bitmap = bm;

            String scaleStr = (scale == THUMBNAIL_BIG ? "big" : "small");

            String extension = FilenameUtils.getExtension(originalPath);
            File original = new File(originalPath);
            targetFile = new File(thumbnailSaveDir, scaleStr + "_" + original.getName().replace(extension, "jpg"));

            fileOutputStream = new FileOutputStream(targetFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (Exception e){
            Logger.e(e);
        } finally {
            if(bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
            }

            IOUtils.close(bufferedInputStream);
            IOUtils.flush(fileOutputStream);
            IOUtils.close(fileOutputStream);
        }
        if(targetFile != null && targetFile.exists()){
            return targetFile.getAbsolutePath();
        }

        return "";
    }

    /**
     * 创建缩略图
     *
     * @param thumbnailSaveDir 缩略图保存路径
     * @param originalPath
     * @return
     */
    public static String[] createThumbnails(String thumbnailSaveDir, String originalPath) {
        String[] images = new String[2];
        images[0] = getThumbnailBigPath(thumbnailSaveDir, originalPath);
        images[1] = getThumbnailSmallPath(thumbnailSaveDir, originalPath);
        return images;
    }

    public static String getThumbnailBigPath(String thumbnailSaveDir, String originalPath) {
        return compressAndSaveImage(thumbnailSaveDir, originalPath, THUMBNAIL_BIG);
    }

    public static String getThumbnailSmallPath(String thumbnailSaveDir, String originalPath) {
        return compressAndSaveImage(thumbnailSaveDir, originalPath, THUMBNAIL_SMALL);
    }

    /**
     * 图片压缩并且存储
     * @param thumbnailSaveDir 缩略图保存路径
     * @param originalPath 图片地址
     * @param scale 图片缩放值
     * @return
     */
    public static String compressAndSaveImage(String thumbnailSaveDir, String originalPath, int scale) {

        Bitmap bitmap = null;
        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;
        String thumbnailPath = null;

        try {
            //1、得到图片的宽、高
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bufferedInputStream = new BufferedInputStream(new FileInputStream(originalPath));
            bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options);
            if (bitmap != null) {
                bitmap.recycle();
            }
            bufferedInputStream.close();

            int originalImageWidth = options.outWidth;
            int originalImageHeight = options.outHeight;

            //2、获取图片方向
            int orientation = getImageOrientation(originalPath);
            int rotate = 0;
            switch (orientation) {//判断是否需要旋转
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = -90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            //3、计算图片压缩inSampleSize
            int maxValue = Math.max(originalImageWidth, originalImageHeight);
            if (maxValue > 3000) {
                options.inSampleSize = scale * 6;
            } else if (maxValue > 2000 && maxValue <= 3000) {
                options.inSampleSize = scale * 5;
            } else if (maxValue > 1500 && maxValue <= 2000) {
                options.inSampleSize = scale * 4;
            } else if (maxValue > 1000 && maxValue <= 1500) {
                options.inSampleSize = scale * 3;
            } else if (maxValue > 400 && maxValue <= 1000) {
                options.inSampleSize = scale * 2;
            } else {
                options.inSampleSize = scale;
            }
            options.inJustDecodeBounds = false;

            //4、图片方向纠正和压缩(生成缩略图)
            bufferedInputStream = new BufferedInputStream(new FileInputStream(originalPath));
            bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options);
            bufferedInputStream.close();

            if(bitmap == null){
                return "";
            }

            String scaleStr = (scale == THUMBNAIL_BIG?"big":"small");

            String extension = FilenameUtils.getExtension(originalPath);
            File original = new File(originalPath);
            File targetFile = new File(thumbnailSaveDir, scaleStr + "_" + original.getName());

            fileOutputStream = new FileOutputStream(targetFile);
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                Bitmap bitmapOld = bitmap;
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, false);
                bitmapOld.recycle();
            }

            //5、保存图片
            if(TextUtils.equals(extension.toLowerCase(), "jpg")
                    || TextUtils.equals(extension.toLowerCase(), "jpeg")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            } else if(TextUtils.equals(extension.toLowerCase(), "webp")
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
            } else {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            }
            thumbnailPath = targetFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bufferedInputStream);
            IOUtils.flush(fileOutputStream);
            IOUtils.close(fileOutputStream);
            if(bitmap != null && bitmap.isRecycled()){
                bitmap.recycle();
            }
        }

        return thumbnailPath;
    }

    /**
     * 获取一张图片在手机上的方向值
     * @param uri
     * @return
     * @throws IOException
     */
    public static int getImageOrientation(String uri) throws IOException {
        ExifInterface exif = new ExifInterface(uri);
        int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        return orientation;
    }
}
