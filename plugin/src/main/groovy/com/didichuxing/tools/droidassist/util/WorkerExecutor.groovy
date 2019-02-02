package com.didichuxing.tools.droidassist.util

import com.google.common.collect.Lists

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class WorkerExecutor {

    private ExecutorService executorService
    private List futureList =
            Collections.synchronizedList(Lists.newArrayList())

    WorkerExecutor() {
        executorService = Executors.newCachedThreadPool()
    }

    WorkerExecutor(int threads) {
        executorService = Executors.newFixedThreadPool(threads)
    }

    void execute(Runnable task) {
        futureList.add(executorService.submit(task))
    }

    void finish() {
        futureList.each {
            try {
                it.get()
            } catch (InterruptedException ignore) {
            } catch (ExecutionException e) {
                throw new ExecutionException(e)
            }
        }
        executorService.shutdown()
    }
}
