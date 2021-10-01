/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.health.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FutureUtils {

    private static Timer timer = new Timer("opennms.FutureUtils", true);

    /**
     * Returns a completable future that is guaranteed to complete.
     * <p>
     * It is completed with the value returned by the given callable or the given default
     * value in case the timeout was reached. In case the invocation of the callable throws
     * an exception the returned future is completed exceptionally.
     * <p>
     * Note: The execution of the given callable is cancelled on timeout.
     */
    public static <T> CompletableFuture<T> completableFutureWithDefaultOnTimeout(
            Callable<T> callable,
            Duration timeout,
            Supplier<T> defaultValue,
            ExecutorService executorService
    ) {
        return completableFuture(callable, timeout, cf -> cf.complete(defaultValue.get()), executorService);
    }

    /**
     * Returns a completable future that is guaranteed to complete.
     * <p>
     * It is completed with the value returned by the given callable. In case the timeout is reached or that
     * the invocation of the callable throws an exception the returned future is completed exceptionally.
     * <p>
     * Note: The execution of the given callable is cancelled on timeout.
     */
    public static <T, EX extends Throwable> CompletableFuture<T> completableFutureWithTimeoutException(
            Callable<T> callable,
            Duration timeout,
            Supplier<EX> timeoutException,
            ExecutorService executorService
    ) {
        return completableFuture(callable, timeout, cf -> cf.completeExceptionally(timeoutException.get()), executorService);
    }

    /**
     * Returns a completable future that is guaranteed to complete.
     * <p>
     * It is completed with the value returned by the given callable.In case the invocation of the callable throws
     * an exception the returned future is completed exceptionally. In case of a timeout the given callback must
     * complete the completable future.
     * <p>
     * Note: The execution of the given callable is cancelled on timeout.
     */
    public static <T> CompletableFuture<T> completableFuture(
            Callable<T> callable,
            Duration timeout,
            Consumer<CompletableFuture<T>> onTimeout,
            ExecutorService executorService
    ) {
        var result = new CompletableFuture<T>();
        var future = executorService.submit(() -> {
            try {
                result.complete(callable.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        var timerTask = new TimerTask() {
            @Override
            public void run() {
                future.cancel(true);
                onTimeout.accept(result);
            }
        };
        timer.schedule(timerTask, timeout.toMillis());
        result.thenRun(timerTask::cancel);
        return result;
    }

    /**
     * Collects the results of the futures into a list. The order of results in the list corresponds to the order
     * of the given futures.
     */
    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> cfs) {
        return traverse(cfs, Function.identity());
    }

    /**
     * Converts a list of values into futures and collects their results.
     * <p>
     * All futures are created initially in order to start their execution.
     */
    public static <U, V> CompletableFuture<List<V>> traverse(
            List<U> us,
            Function<U, CompletableFuture<V>> func
    ) {
        var futures = us.stream().map(func).collect(Collectors.toList());
        CompletableFuture<List<V>> result = CompletableFuture.completedFuture(new ArrayList<>());
        for (var f: futures) {
            result = result.thenCombine(f, (list, item) -> { list.add(item); return list; });
        }
        return result;
    }

}
