package org.ed.async.exercises;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Reads data from files from the related directory, sum per each file, combine and deliver result to console.
 * All operations happens in async mode.
 */
public class AsyncFileSystemNumbersSumator {

    private static final String PATH = "src/main/java/org/ed/files/";

    private List<Integer> results = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(new AsyncFileSystemNumbersSumator().sumNumbersFromPath(PATH));
    }

    public Integer sumNumbersFromPath(String path) throws IOException, InterruptedException {
        Set<String> files = AsyncFileSumatorUtils.getFiles(path);
        List<CompletableFuture<Stream<Integer>>> features = AsyncFileSumatorUtils.getFilesDataAsync(path, files);
        List<CompletableFuture<Integer>> combiners = combineFilesSumsAsync(features);
        return deliverResult(combiners);
    }

    private List<CompletableFuture<Integer>> combineFilesSumsAsync(List<CompletableFuture<Stream<Integer>>> features) {
        fixNonEvenSize(features);

        int i = 0;
        int j = features.size() - 1;
        List<CompletableFuture<Integer>> combiners = new LinkedList<>();

        CompletableFuture<Stream<Integer>> one, two;
        while (i < j) {
            one = features.get(i);
            two = features.get(j);
            combiners.add( one.thenCombineAsync(two, (v, z) -> {
                Integer val1 = v.collect(Collectors.toList()).get(0);
                Integer val2 = z.collect(Collectors.toList()).get(0);
                return val1 + val2;
            }).thenApplyAsync((t) -> {
                results.add(t);
                return null;
            }));

            i++;
            j--;
        }

        return combiners;
    }

    private Integer deliverResult(List<CompletableFuture<Integer>> combiners) throws InterruptedException {
        CompletableFuture<Integer>[] completableFutures = completableFuturesListToArray(combiners);

        CompletableFuture.allOf(completableFutures);

        while(completableFutures.length != results.size()) {
            System.out.println("waiting for result....");
            Thread.sleep(2_000);
        }

        return results.parallelStream().reduce(Integer::sum).orElse(0);
    }

    private static void fixNonEvenSize(List<CompletableFuture<Stream<Integer>>> features) {
        boolean isSizeOfListEven = features.size() % 2 == 0;

        if (!isSizeOfListEven) {
            features.add(CompletableFuture.completedFuture(Stream.of(0)));
        }
    }

    private static CompletableFuture<Integer>[] completableFuturesListToArray(List<CompletableFuture<Integer>> combiners) {
        CompletableFuture<Integer>[] completableFutures = new CompletableFuture[combiners.size()];
        combiners.toArray(completableFutures);
        return completableFutures;
    }

}
