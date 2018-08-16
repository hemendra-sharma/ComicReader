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

package com.hemendra.comicreader.model.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Holds the list of comics and their categories.
 * @author Hemendra Sharma
 * @see java.io.Serializable
 */
public class Comics implements Serializable {

    /**
     * Keeping the serial version unique identifier, so that we won't loose data when modifying
     * class structure of code.
     */
    private static final long serialVersionUID = 4603566027627855242L;

    /**
     * The combined list of all the categories even discovered while parsing the comics.
     */
    public ArrayList<String> categories = new ArrayList<>();
    /**
     * The list of comics.
     */
    public ArrayList<Comic> comics = new ArrayList<>();

}
