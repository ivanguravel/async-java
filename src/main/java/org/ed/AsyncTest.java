package org.ed;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AsyncTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> f1 =  CompletableFuture.supplyAsync(() -> add(1)).thenApplyAsync(AsyncTest::add);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> add(2));

        CompletableFuture<Integer> f3 = f1.thenCombine(f2, (x, y) -> x*y);


        CompletableFuture<Integer> future = slowInt()
                .thenCompose(integer -> CompletableFuture.supplyAsync(() -> integer))
                .thenApply(r -> add(r));

        System.out.println(future.get());

        System.out.println(f3.get());


        CompletableFuture<Object> objectCompletableFuture = CompletableFuture.anyOf(f1, f2);
        System.out.println(objectCompletableFuture.get());

        CompletableFuture<CompletableFuture<Integer>> cf = CompletableFuture.supplyAsync(() -> add(2))
                .thenApply(AsyncTest::slowIntExc).exceptionally(throwable -> {
                    System.out.println(throwable.getMessage());
                    throw new IllegalArgumentException(throwable);
                });


        asyncMerge();
    }


    private static void asyncMerge() throws ExecutionException, InterruptedException {
        CompletableFuture<List<Data>> one = CompletableFuture
                .completedFuture(Arrays.asList(new Data(1, "Tony"), new Data(2, "Vova")));
        CompletableFuture<List<Data>> two = CompletableFuture
                .completedFuture(Arrays.asList(new Data(3, "Tony1"), new Data(2, "Vova")));

        CompletableFuture<List<Data>> result = one.thenCombineAsync(two, AsyncTest::merge);

        for (Data d : result.get()) {
            System.out.println(d.getId() + " " + d.getName());
        }

    }

    private static List<Data> merge(List<Data> one, List<Data> two) {
        return one.stream().filter(o -> two.contains(o)).collect(Collectors.toList());
    }

    private static int add(int i) {
        return i+1;
    }

    private static CompletableFuture<Integer> slowInt() throws InterruptedException {
        Thread.sleep(1_000);
        return CompletableFuture.completedFuture(1);
    }

    private static CompletableFuture<Integer> slowIntExc(int i)   {
        throw new UnsupportedOperationException();
    }
}
