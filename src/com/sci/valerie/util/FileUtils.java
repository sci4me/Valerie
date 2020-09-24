package com.sci.valerie.util;

import java.io.*;

public final class FileUtils {
    public static String readFile(final InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
        }

        reader.close();

        return sb.toString();
    }

    private FileUtils() {
    }
}