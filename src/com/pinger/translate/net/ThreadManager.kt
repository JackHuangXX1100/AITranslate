package com.fungo.baselib.manager

import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Pinger
 * @since 2018/4/7 8:23
 */
object ThreadManager {

    private const val DEFAULT_SINGLE_POOL_NAME = "DEFAULT_SINGLE_POOL_NAME"

    private var mLongPool: ThreadPoolProxy? = null
    private val mLongLock = Any()

    private var mShortPool: ThreadPoolProxy? = null
    private val mShortLock = Any()

    private var mDownloadPool: ThreadPoolProxy? = null
    private val mDownloadLock = Any()

    private val mMap = HashMap<String, ThreadPoolProxy>()
    private val mSingleLock = Any()

    /** 获取下载线程  */
    val downloadPool: ThreadPoolProxy
        get() = synchronized(mDownloadLock) {
            if (mDownloadPool == null) {
                mDownloadPool = ThreadPoolProxy(3, 3, 5L)
            }
            return mDownloadPool!!
        }

    /**
     * 获取一个用于执行长耗时任务的线程池，避免和短耗时任务处在同一个队列 而阻塞了重要的短耗时的任务，通常用联网操作s
     */
    val longPool: ThreadPoolProxy
        get() = synchronized(mLongLock) {
            if (mLongPool == null) {
                mLongPool = ThreadPoolProxy(5, 5, 5L)
            }
            return mLongPool!!
        }

    /** 获取一个用于执行短耗时任务的线程池，避免因为和耗时长的任务处在同一个队列而长时间得不到执行，通常用来执行本地的IO/SQL  */
    val shortPool: ThreadPoolProxy
        get() = synchronized(mShortLock) {
            if (mShortPool == null) {
                mShortPool = ThreadPoolProxy(2, 2, 5L)
            }
            return mShortPool!!
        }

    /** 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题  */
    val singlePool: ThreadPoolProxy
        get() = getSinglePool(DEFAULT_SINGLE_POOL_NAME)

    /** 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题  */
    fun getSinglePool(name: String): ThreadPoolProxy {
        synchronized(mSingleLock) {
            var singlePool: ThreadPoolProxy? = mMap[name]
            if (singlePool == null) {
                singlePool = ThreadPoolProxy(1, 1, 5L)
                mMap[name] = singlePool
            }
            return singlePool
        }
    }

    class ThreadPoolProxy constructor(private val mCorePoolSize: Int, // 线程池维护线程的最少数量
                                      private val mMaximumPoolSize: Int, // 线程池维护线程的最大数量
                                      private val mKeepAliveTime: Long // 线程池维护线程所允许的空闲时间
    ) {
        private var mPool: ThreadPoolExecutor? = null

        // 执行任务，当线程池处于关闭，将会重新创建的线程池
        @Synchronized
        fun execute(run: Runnable?) {
            if (run == null) {
                return
            }
            if (mPool == null || mPool!!.isShutdown) {
                // 参数说明
                // 当线程池中的线程小于mCorePoolSize，直接创建新的线程加入线程池执行任务
                // 当线程池中的线程数目等于mCorePoolSize，将会把任务放入任务队列BlockingQueue中
                // 当BlockingQueue中的任务放满了，将会创建新的线程去执行，
                // 但是当总线程数大于mMaximumPoolSize时，将会抛出异常，交给RejectedExecutionHandler处理
                // mKeepAliveTime是线程执行完任务后，且队列中没有可以执行的任务，存活的时间，后面的参数是时间单位
                // ThreadFactory是每次创建新的线程工厂
                mPool = ThreadPoolExecutor(mCorePoolSize,
                        mMaximumPoolSize, mKeepAliveTime,
                        TimeUnit.MILLISECONDS, LinkedBlockingQueue(),
                        Executors.defaultThreadFactory(), ThreadPoolExecutor.AbortPolicy())
            }
            mPool?.execute(run)
        }

        /** 取消线程池中某个还未执行的任务  */
        @Synchronized
        fun cancel(run: Runnable) {
            if (mPool != null && !mPool!!.isShutdown || mPool!!.isTerminating) {
                mPool!!.queue.remove(run)
            }
        }

        /** 取消线程池中某个还未执行的任务  */
        @Synchronized
        operator fun contains(runn: Runnable): Boolean {
            return if (mPool != null && (!mPool!!.isShutdown || mPool!!.isTerminating)) {
                mPool!!.queue.contains(runn)
            } else {
                false
            }
        }

        /** 立刻关闭线程池，并且正在执行的任务也将会被中断  */
        fun stop() {
            if (mPool != null && (!mPool!!.isShutdown || mPool!!.isTerminating)) {
                mPool!!.shutdown()
            }
        }

        /** 平缓关闭单任务线程池，但是会确保所有已经加入的任务都将会被执行完毕才关闭  */
        @Synchronized
        fun shutdown() {
            if (mPool != null && (!mPool!!.isShutdown || mPool!!.isTerminating)) {
                mPool!!.shutdownNow()
            }
        }
    }

    /** 运行在子线程 */
    fun runOnSubThread(run: Runnable) {
        singlePool.execute(run)
    }

    /** 关闭所有的线程连接，避免内存泄漏 */
    fun shutdown() {
        singlePool.shutdown()
        downloadPool.shutdown()
        longPool.shutdown()
        shortPool.shutdown()
    }
}
