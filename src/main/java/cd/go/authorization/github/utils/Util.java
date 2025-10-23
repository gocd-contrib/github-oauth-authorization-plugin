/*
 * Copyright 2022 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.authorization.github.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Util {
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static String readResource(String resourceFile) {
        return new String(readResourceBytes(resourceFile), StandardCharsets.UTF_8);
    }

    public static byte[] readResourceBytes(String resourceFile) {
        try (InputStream in = Objects.requireNonNull(Util.class.getResourceAsStream(resourceFile))) {
            return readFully(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not find resource " + resourceFile, e);
        }
    }

    private static byte[] readFully(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    public static List<String> splitIntoLinesAndTrimSpaces(String lines) {
        if (isBlank(lines)) {
            return Collections.emptyList();
        }
        return List.of(lines.split("\\s*[\r\n]+\\s*"));
    }

    public static List<String> listFromCommaSeparatedString(String str) {
        if (isBlank(str)) {
            return Collections.emptyList();
        }
        return List.of(str.trim().split("\\s*,\\s*"));
    }

    public static String toLowerCase(String str) {
        return str == null || str.isBlank() ? str : str.toLowerCase();
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }
}
