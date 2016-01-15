/**
 * (C) Copyright IBM Corporation 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.netflixoss.hystrix;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;


public class WsHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private ThreadPoolExecutorFactory executorFactory;
    private BlockingQueueFactory blockingQueueFactory;
    private final ConcurrentMap<HystrixThreadPoolKey, ThreadPoolExecutor> poolMap = new ConcurrentHashMap<HystrixThreadPoolKey, ThreadPoolExecutor>();

    void setThreadPoolExecutorFactory(ThreadPoolExecutorFactory threadPoolExecutorFactory) {
        executorFactory = threadPoolExecutorFactory;
    }

    void setBlockingQueueFactory(BlockingQueueFactory blockingQueueFactory) {
        this.blockingQueueFactory = blockingQueueFactory;
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
            HystrixProperty<Integer> corePoolSize,
            HystrixProperty<Integer> maximumPoolSize,
            HystrixProperty<Integer> keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        ThreadPoolExecutor pool = poolMap.get(threadPoolKey);
        if (pool == null) {
            ThreadPoolExecutor newPool = 
                executorFactory.createThreadPoolExecutor(
                        corePoolSize.get(), maximumPoolSize.get(),
                        keepAliveTime.get(), unit, workQueue);
            pool = poolMap.putIfAbsent(threadPoolKey, newPool);
            pool = (pool == null) ? newPool : pool;
        }

        return pool;
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return blockingQueueFactory.createBlockingQueue(maxQueueSize);
    }
}
