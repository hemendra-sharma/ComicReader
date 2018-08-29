/*
 * Copyright (c) 2018 Hemendra Sharma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hemendra.comicreader.model.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

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
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            deleteFile(file);
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                //releasing the FileInputStream and ObjectInput
                if(instr != null)
                    instr.close();
                if(bufferIn != null)
                    bufferIn.close();
                if(in != null)
                    in.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
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
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                bos.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
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
        return data != null && writeToFile(data, file);
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
        } catch (IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        } finally {
            try {
                if(fout != null)
                    fout.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Delete the file at the given path
     * @param file target File
     * @return TRUE if deleted successfully, FALSE otherwise.
     */
    public static boolean deleteFile(@NonNull File file) {
        return file.exists() && file.delete();
    }

    /**
     * Delete the given directory and all its subdirectories
     * @param dir target directory
     * @return TRUE if deleted successfully, FALSE otherwise.
     */
    private static boolean deleteDirectory(@NonNull File dir) {
        boolean success = true;
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
        return success;
    }

    /**
     * Find out if the decoding bitmap needs to be re-sampled or not. If yes, then return
     * then appropriate sample size (>= 1)
     * @param options (out) Instance of {@link BitmapFactory.Options}
     * @param reqWidth (in) Required bitmap's width
     * @param reqHeight (in) Required bitmap's height
     * @return Returns the sample size (>= 1)
     */
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
