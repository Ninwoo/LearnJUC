# JUC学习记录

## 线程池

在该小结将介绍各种线程池的使用及特点。首先使用线程池有什么优势呢？

Java中创建一个新的线程有两种方式[^1]，实现Runnable接口或者扩展Thread类。我们知道，可以new出来一个Thread，并通过start()可以启动一个线程。但是问题是，线程的启动和停止也不是一蹴而就的，都是有CPU的性能消耗的。当线程多的时候，重复的创建线程会对性能造成非常大的影响。解决这种问题最好的方式就是引入池化的概念，线程池应运而生。

除了解决了性能问题之外，线程池都带来了哪些好处呢：

1. 可以对启动的线程进行管理
2. 节省更多的线程资源
3. 线程的启动速度更快

接下来，我们来看线程池到底在java中是如何被定义的呢？

## ExecutorService

ExecutorService继承自Executor。源码中讲“Executor提供了创建用于跟踪一个或多个异步任务的Future对象的方法。”这里其实也就是说明了，线程池较普通创建线程的方法而言，具备了额外的线程管理功能。

## ThreadPoolExecutor

ExecutorService只定义了线程池的接口，线程池具体的创建就要靠ThreadPoolExecutor。

```java
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) 
```

ThreadPoolExecutor的输入参数很是重要，一个线程池是否能在运行环境中表现优良，完全取决于这些参数的设置。

* corePoolSize: 始终保持在线程池中的线程数量，无论是不是空闲
* maximumPoolSize: 线程池中运行的最大线程数量
* keepAliveTime: 超出核心线程数量的线程空闲后保留的时间
* unit： keepAliveTime的时间单位
* workQueue:  当线程不足时，用于存储任务的队列
* threadFactory: Executor创建线程的工厂类
* handler: 当线程到达上限，队列已满的时候的处理方式

### Worker

```java
private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
```

内部类worker是线程池中存放线程的实体，继承了AQS，并实现了Runnable接口。AQS保证每个线程在处理任务的时候都能进入同步状态。

### execute

1. 如果当前线程小于corePoolSize，则创建一个新的线程
2. 当线程可以放入到队列中，应该再次检查是否应该放入到队列中，因为放入队列的过程中，很有可能执行了shutdown操作。
3. 如果不能放入到队列中，尝试开启新的线程。

```java
	int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);
    }
```

addWorker中使用了CAS自旋，并且使用了break,continue到指定的point。

1. 先尝试增加线程数量
2. 增加成功后创建新的线程

## Executors

JDK为我们提供了一些默认的线程池实现。

### newFixedThreadPool

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
```

1. 固定线程数量。核心线程数等于最大线程数
2. 队里采用LinkedBlockingQueue,无界的阻塞队列

### newCachedThreadPool

```java
public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```

* 核心线程数为0,最大线程数位最大值。
* 线程数量不受限制，来一个创建一个，60秒后闲置自动回收
* 队列使用SynchronousQueue，一个无缓冲区的阻塞队列。
  * 优势可以快速的处理任务

### newScheduledThreadPool

```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }
```

ScheduledThreadPoolExecutor的核心是使用了延时队列DelayedWorkQueue()。

### newWorkStealingPool

```java
public static ExecutorService newWorkStealingPool(int parallelism) {
        return new ForkJoinPool
            (parallelism,
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }
```

使用ForkJoinPool创建的工作密取线程池。

## 阻塞队列



[^1]: 来自Thread源码中的注释