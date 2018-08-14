package com.hemendra.comicreader.model.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

/**
 * This is a helper class to perform some of the basic tasks like, internet connectivity check,
 * read or write files, create image thumbnails, etc.
 */
public class Utils {

    /**
     * Check whether this device is connected to a network or not.
     * @param context The application context.
     * @return TRUE if connected, FALSE otherwise.
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo info;
            for(Network network : networks) {
                info = connectivity.getNetworkInfo(network);
                if (info != null
                        && info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Read the specified file in the form of raw bytes-array.
     * @param filePath File to read from.
     * @return Raw bytes read from file. NULL if failed to read.
     */
    @Nullable
    public static byte[] readBytesFromFile(@NonNull String filePath) {
        return readBytesFromFile(new File(filePath));
    }

    /**
     * Read the specified file in the form of raw bytes-array.
     * @param file File to read from.
     * @return Raw bytes read from file. NULL if failed to read.
     */
    @Nullable
    private static byte[] readBytesFromFile(@NonNull File file) {
        FileInputStream fin = null;
        ByteArrayOutputStream outstr = null;
        try {
            if (!file.exists())
                return null;
            fin = new FileInputStream(file);
            outstr = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numRead;
            while ((numRead = fin.read(buf)) > 0) {
                outstr.write(buf, 0, numRead);
            }
            byte[] bytes = outstr.toByteArray();
            outstr.close();
            return bytes;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }finally {
            try {
                if(fin != null)
                    fin.close();
                if(outstr != null)
                    outstr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Read the specified file in the form of textual string.
     * @param filePath File to read from.
     * @return String read from file. NULL if failed to read.
     */
    @Nullable
    public static String readStringFromFile(@NonNull String filePath) {
        return readStringFromFile(new File(filePath));
    }

    /**
     * Read the specified file in the form of textual string.
     * @param file File to read from.
     * @return String read from file. NULL if failed to read.
     */
    @Nullable
    private static String readStringFromFile(@NonNull File file) {
        BufferedReader reader = null;
        try {
            if (!file.exists())
                return null;
            StringBuilder fileData = new StringBuilder();
            reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) > 0) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            return fileData.toString();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }finally {
            try {
                if(reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Read the specified file in the form of Java Object.
     * @param filePath File to read from.
     * @return Java Object read from file. NULL if failed to read.
     */
    @Nullable
    public static Object readObjectFromFile(@NonNull String filePath) {
        return readStringFromFile(new File(filePath));
    }

    /**
     * Read the specified file in the form of Java Object.
     * @param file File to read from.
     * @return Java Object read from file. NULL if failed to read.
     */
    @Nullable
    public static Object readObjectFromFile(@NonNull File file) {
        FileInputStream instr = null;
        BufferedInputStream bufferIn = null;
        ObjectInput in = null;
        try {
            if (!file.exists())
                return null;
            instr = new FileInputStream(file);
            bufferIn = new BufferedInputStream(instr);
            in = new ObjectInputStream(bufferIn);
            Object obj = in.readObject();
            instr.close();
            bufferIn.close();
            in.close();
            return obj;
        } catch (EOFException | StreamCorruptedException | OptionalDataException ignore) {
            // 'null' was written to the file, so returning null
            deleteFile(file);
            return null;
        } catch (Throwable ex) {
            deleteFile(file);
            ex.printStackTrace();
        }finally {
            try {
                //releasing the FileInputStream and ObjectInput
                if(instr != null)
                    instr.close();
                if(bufferIn != null)
                    bufferIn.close();
                if(in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Convert the given object into byte array.
     * Note: the object must implement {@link java.io.Serializable}
     * @param obj Object to convert into byte array.
     * @return Raw byte array.
     */
    @Nullable
    private static byte[] getSerializedData(@NonNull Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            bytes = bos.toByteArray();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            try {
                bos.close();
                if (out != null)
                    out.close();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * Convert the given object into byte array and write it to the given file path.
     * Note: the object must implement {@link java.io.Serializable}
     * @param obj Object that needs to be written to file.
     * @param file File target.
     * @return TRUE if the file was written successfully. FALSE otherwise.
     */
    public static boolean writeToFile(@NonNull Object obj, @NonNull File file) {
        byte[] data = getSerializedData(obj);
        if(data == null)
            return false;
        return writeToFile(data, file);
    }

    /**
     * Write the raw byte array to the given file path.
     * @param data byte array to be written
     * @param dirPath File directory path
     * @param fileName File name
     * @return TRUE if the file was written successfully. FALSE otherwise.
     */
    public static boolean writeToFile(@NonNull byte[] data,
                                      @NonNull String dirPath, @NonNull String fileName) {
        if (dirPath.length() == 0 || fileName.length() == 0)
            return false;
        return writeToFile(data, new File(dirPath, fileName));
    }

    /**
     * Write the raw byte array to the given file path.
     * @param data byte array to be written
     * @param file Target tile
     * @return TRUE if the file was written successfully. FALSE otherwise.
     */
    private static boolean writeToFile(@NonNull byte[] data, @NonNull File file) {
        FileOutputStream fout = null;
        try {
            boolean proceed = file.getParentFile().exists()
                    || file.getParentFile().mkdirs();
            if(proceed) {
                if(file.exists() || file.createNewFile()) {
                    fout = new FileOutputStream(file);
                    fout.write(data);
                    return true;
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            try {
                if(fout != null)
                    fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Delete the file at the given path
     * @param dirPath Parent directory of the file
     * @param fileName File name
     * @return TRUE if deleted successfully, FALSE otherwise.
     */
    public static boolean deleteFile(@NonNull String dirPath, @NonNull String fileName) {
        if (dirPath.length() == 0 || fileName.length() == 0)
            return false;
        return deleteFile(new File(dirPath, fileName));
    }

    /**
     * Delete the file at the given path
     * @param file target File
     * @return TRUE if deleted successfully, FALSE otherwise.
     */
    public static boolean deleteFile(File file) {
        try {
            if (file.exists()) {
                file.delete();
                return true;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Delete the directory and all its subdirectories at the given path
     * @param dirPath target directory path
     * @return TRUE if deleted successfully, FALSE otherwise.
     */
    public static boolean deleteDirectory(@NonNull String dirPath) {
        if (dirPath.length() == 0)
            return false;
        return deleteDirectory(new File(dirPath));
    }

    /**
     * Delete the given directory and all its subdirectories
     * @param dir target directory
     * @return TRUE if deleted successfully, FALSE otherwise.
     */
    private static boolean deleteDirectory(@NonNull File dir) {
        boolean success = true;
        try {
            if (dir.exists() || dir.mkdirs()) {
                if (dir.exists()) {
                    File[] files = dir.listFiles();
                    for(File file : files) {
                        if(file.isDirectory()) {
                            success &= deleteDirectory(file);
                        } else {
                            success &= file.delete();
                        }
                    }
                    success &= dir.delete();
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return success;
    }

    /**
     * Convert the resource file to bitmap and then create the thumbnail of given size.
     * @param context The application context
     * @param resID ID of the resource file
     * @param width Output bitmap's width
     * @param height Output bitmap's height
     * @return resized bitmap object if successful. NULL otherwise.
     */
    @Nullable
    public static Bitmap getThumbnail(@NonNull Context context,
                                      @IntegerRes int resID, int width, int height) {
        return getThumbnail(BitmapFactory.decodeResource(context.getResources(), resID), width, height);
    }

    /**
     * Convert the resource file to bitmap and then create the thumbnail of given size.
     * @param data image data raw byte array
     * @param width Output bitmap's width
     * @param height Output bitmap's height
     * @return resized bitmap object if successful. NULL otherwise.
     */
    @Nullable
    public static Bitmap getThumbnail(@NonNull byte[] data, int width, int height) {
        return getThumbnail(BitmapFactory.decodeByteArray(data, 0, data.length), width, height);
    }

    /**
     * Convert the resource file to bitmap and then create the thumbnail of given size.
     * @param bmp input image bitmap object
     * @param width Output bitmap's width
     * @param height Output bitmap's height
     * @return resized bitmap object if successful. NULL otherwise.
     */
    @Nullable
    private static Bitmap getThumbnail(@NonNull Bitmap bmp, int width, int height) {
        try {
            int bmpWidth = bmp.getWidth();
            int bmpHeight = bmp.getHeight();
            if (width > height) {
                height = (int) (((double) width / (double) bmpWidth) * (double) bmpHeight);
            } else {
                width = (int) (((double) height / (double) bmpHeight) * (double) bmpWidth);
            }
            Bitmap b = Bitmap.createScaledBitmap(bmp, width, height, false);
            bmp.recycle();
            return b;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
