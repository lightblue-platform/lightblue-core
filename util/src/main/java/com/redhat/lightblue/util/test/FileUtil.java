/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.util.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Created by lcestari on 3/27/14.
 */
public final class FileUtil {

    private FileUtil() {

    }

    public static String readFile(String path) throws IOException, URISyntaxException {
        StringBuilder everything = new StringBuilder();

        try (InputStreamReader isr = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), Charset.forName("UTF-8"));
                BufferedReader bufferedReader = new BufferedReader(isr);) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                everything.append(line);
            }
        }
        return everything.toString().replaceAll("\\s", "").replaceAll("\\r|\\n", "");
    }
}
