package org.ed.async.exercises;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsyncFileSumatorUtils {

    private AsyncFileSumatorUtils() {}

    static Set<String> getFiles(String path) throws IOException {
        try (Stream stream = Files.list(Paths.get(path))) {
            return (Set<String>) stream
                    .filter(file -> !Files.isDirectory((Path) file))
                    .map(f -> ((Path) f).getFileName())
                    .map(f -> f.toString())
                    .collect(Collectors.toSet());
        }
    }

    static List<CompletableFuture<Stream<Integer>>> getFilesDataAsync(String path, Set<String> files) {
        List<CompletableFuture<Stream<Integer>>> features = new ArrayList<>(files.size());

        for (String file : files) {
            features.add(new CompletableFuture()
                    .supplyAsync(() -> safeReadFileLines(path, file))
                    .thenApplyAsync((t) -> t.parallelStream()
                            .map(AsyncFileSumatorUtils::convertLinesToReducedNumber)));
        }
        return features;
    }


    private static List<String> safeReadFileLines(String path,String file) {
        try {
            return Files.readAllLines(Paths.get(path + file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static Integer convertLinesToReducedNumber(String v) {
        String[] split = v.split(" ");
        return Arrays.asList(split).parallelStream()
                .map(Integer::parseInt).reduce(Integer::sum).orElse(0);
    }
}
