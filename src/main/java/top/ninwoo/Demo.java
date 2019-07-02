package top.ninwoo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class Demo {
    public static void main(String[] args) {
        /**
         * 第一部分：复习线程池
         * 1. workQueue the queue to use for holding tasks before they are
         *      *        executed.  This queue will hold only the {@code Runnable}
         *      *        tasks submitted by the {@code execute} method.
         *
         * 这里验证是否队列只能保存Runnable接口, 事实证明Callable和submit也可以存。
         */
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(1,1,1000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(5));

        List<Future<String>> rs = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            /*Runnable run1 = new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ":" + "启动");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };*/
            //threadPoolExecutor.execute(run1);

            Callable<String> c = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Thread.sleep(2000);
                    return "hallla";
                }
            };
            Future<String> submit = threadPoolExecutor.submit(c);
            rs.add(submit);
        }

        for (Future<String> r : rs) {
            try {
                System.out.println(r.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        //ExecutorService executorService = Executors.newFixedThreadPool(4);

    }
}
