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

package com.hemendra.comicreader.model.source.images.remote;

import com.hemendra.comicreader.model.data.Chapter;
import com.hemendra.comicreader.model.source.FailureReason;

/**
 * Provides an interface for downloading the images for all pages.
 */
public interface OnChapterDownloadListener {

    /**
     * Getss called when the chapter had no page information loaded before starting the download
     * and then it was aquired while buffering the pages offline.
     * @param chapter The chapter which needs to be updated.
     */
    void onChapterPagesAcquired(Chapter chapter);

    /**
     * Gets called when the images for all the pages were downloaded successfully.
     * @param chapter The chapter instance that was downloaded.
     */
    void onChapterDownloaded(Chapter chapter);

    /**
     * Gets called when the page download progress is updated.
     * @param progress Array Sequence: [
     *                 Overall Progress,
     *                 Overall Count,
     *                 Total Pages Count,
     *                 Current Page's Download Progress,
     *                 Current Page Number,
     *                 Current Page Size in Bytes
     *                 ]
     */
    void onProgressUpdate(Integer... progress);

    /**
     * Gets called when it failed to download all the pages.
     * @param reason One of the {@link FailureReason}
     */
    void onFailedToDownloadChapter(FailureReason reason);
}
