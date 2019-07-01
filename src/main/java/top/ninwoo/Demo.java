package top.ninwoo;

import java.util.concurrent.*;

public class Demo {
    public static void main(String[] args) {
        /**
         * 第一部分：复习线程池
         */
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(4,8,1000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(5));
        ExecutorService executorService = Executors.newFixedThreadPool(4);

    }
}
