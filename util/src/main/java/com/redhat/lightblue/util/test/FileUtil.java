package com.redhat.lightblue.util.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by lcestari on 3/27/14.
 */
public class FileUtil {
    public static String readFile(String path) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(FileUtil.class.getClassLoader().getResource(path).toURI()))).replaceAll("\\s","").replaceAll("\\r|\\n","");
    }
}
