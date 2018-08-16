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

package com.hemendra.comicreader.model.source.comics;

import android.content.Context;

import com.hemendra.comicreader.model.source.DataSource;

public abstract class ComicsDataSource extends DataSource {

    public enum SourceType {
        LOCAL_FULL,
        LOCAL_SEARCH,
        LOCAL_SORT,
        LOCAL_FILTER,
        REMOTE
    }

    protected IComicsDataSourceListener listener;

    protected ComicsDataSource(Context context, IComicsDataSourceListener listener) {
        super(context);
        this.listener = listener;
    }

    public abstract void loadComics();

    protected abstract void stopLoadingComics();

}
