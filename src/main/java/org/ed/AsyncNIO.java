package org.ed;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AsyncNIO {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        getDataAsync();
    }

    private static void getDataAsync() throws IOException, ExecutionException, InterruptedException {
        final String path = "src/main/java/org/ed/ttt";

        Path path1 = Paths.get(path);
        ByteBuffer buffer = ByteBuffer.allocate(3000);

        AsynchronousFileChannel afc = AsynchronousFileChannel.open(path1);
        CompletableFuture<String> cf = new CompletableFuture<>();
        afc.read(buffer, 0, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                String message = new String(buffer.array(), 0, result);
                cf.complete(message);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                cf.completeExceptionally(exc);
            }
        });

        cf
            .thenAccept(s -> System.out.println(s))
            .thenRun(() -> {
                try {
                    afc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).get();
    }
}
