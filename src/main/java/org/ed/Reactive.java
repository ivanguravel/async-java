package org.ed;

import rx.Observable;
import rx.Scheduler;

import java.util.concurrent.TimeUnit;

public class Reactive {

    public static void main(String[] args) {
        String[] ss = {"a", "b", "c", "d"};
        Observable.from(ss)
                .map(s -> "Letter " + s)
                .buffer(2)
                .subscribe(s -> System.out.println(s));
    }
}
