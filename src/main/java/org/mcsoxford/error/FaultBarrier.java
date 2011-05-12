/**
 * Copyright (c) 2009 Cerner Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Cerner Corporation - initial API and implementation
 */
package org.mcsoxford.error;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * API to recover from failures. The implementation supports concurrent failures
 * without causing interference of the retry logic. The usefulness of fault
 * barriers is best understood by comparing and contrasting unrecoverable faults and
 * contingencies.
 * 
 * @author A. Horn
 * @author D. Edwards
 */
public class FaultBarrier {

  /**
   * Log failures as well as fatal errors from which the retry logic was not
   * able to recover.
   */
  private final FaultBarrierLogger logger;

  /**
   * The read lock should be held for normal operation and the write lock must
   * be acquired when the retry logic is invoked in order to eliminate
   * interference of concurrent operations.
   */
  private final ReadWriteLock lock;

  /**
   * Guard against multiple retry attempts. Use the {@link #lock} to protect
   * access to this integer field.
   * <p>
   * It acts as a ticket dispatcher where a thread only initiates the retry
   * logic if its local ticket number is the same to the global value.
   */
  private int retry = 0;

  /**
   * Internal constructor to inject a lock to eliminate interleaving of retry
   * operations as well as a custom logger. Subclasses must synchronize against
   * the injected lock when calling the logger.
   */
  protected FaultBarrier(final ReadWriteLock lock,
      final FaultBarrierLogger logger) {
    this.logger = logger;
    this.lock = lock;
  }

  /**
   * Convenience constructor for an object that is able to orchestrate the
   * recovery of exceptions. Abnormal execution flows are reported to the JDK
   * logger in form of {@link Level#INFO} messages.
   */
  public FaultBarrier() {
    this(JdkFaultBarrierLogger.create(Level.INFO));
  }

  /**
   * Constructor for an object that is able to orchestrate the recovery of
   * exceptions.
   */
  public FaultBarrier(final FaultBarrierLogger logger) {
    this(new ReentrantReadWriteLock(), logger);
  }

  /**
   * Same as {@link #execute(Callable, RetryHandler)} with a retry handler that
   * is {@code null}.
   */
  public final <S> S execute(final Callable<S> callable) throws Exception {
    return execute(callable, null);
  }

  /**
   * Delegate to {@link Callable#call()} but handle any exceptions that it may
   * throw. If in fact a fault occurs, then the
   * {@link RetryHandler#retry(Callable, Exception)} logic will be invoked
   * provided that the {@code retryHandler} is not {@code null}. If the retry
   * handler is {@code null} then the callable should be invoked again but a
   * subclass may provide an alternative recovery strategy by overriding {@code
   * retry(Callable, Exception)}. Note, {@link #execute(Callable, RetryHandler)}
   * throws only an exception if a severe, fatal error occurs from which it
   * cannot recover.
   * 
   * @param callable closure-style object that is triggered for every normal
   *          execution flow
   * @param retryHandler closure-style object whose
   *          {@link RetryHandler#retry(Callable, Exception)} method is invoked
   *          if the normal execution path results in an exception
   * @throws Exception if the {@link RetryHandler} could not recover from the
   *           failure
   * @throws IllegalArgumentException if the callable is {@code null}
   * @see RetryHandler#retry(Callable, Exception)
   */
  public final <S> S execute(final Callable<S> callable,
      final RetryHandler<S> retryHandler) throws Exception {

    if (callable == null) {
      throw new IllegalArgumentException("Callable must not be null");
    }

    int _retry = 0;
    S exit = null;
    Exception failure = null;

    final Lock readLock = this.lock.readLock();
    readLock.lock();
    try {

      /* copy by value with read lock held */
      _retry = this.retry;

      exit = callable.call();
    } catch (final Exception e) {
      /* INFO: encountered failure but attempt to recover */
      this.logger.recover(e);

      failure = e;
    } finally {
      readLock.unlock();
    }

    if (failure != null) {
      final Lock writeLock = this.lock.writeLock();
      writeLock.lock();
      try {

        /* counter detects concurrent failures after read lock has been released */
        if (_retry == this.retry) {
          try {
            if (retryHandler == null) {
              exit = retry(callable, failure);
            } else {
              /* No one else has initiated the retry logic yet */
              exit = retryHandler.retry(callable, failure);
            }
          } finally {
            /*
             * finally { ... } block causes the FaultBarrier to disallow retries
             * for concurrently queued up operations even if a fatal error has
             * occurred.
             */
            this.retry++;
          }
        }
      } catch (final Exception fatal) {
        /* Log INFO or DEBUG before fatal error propagates */
        this.logger.abort(fatal);

        throw fatal;
      } finally {
        writeLock.unlock();
      }
    }

    return exit;
  }

  /**
   * Protected API to trigger the normal execution flow again after a failure
   * occurred the first time it was invoked. This method is only called if the
   * {@link #execute(Callable, RetryHandler)} method's retry handler was {@code
   * null}.
   */
  protected <S> S retry(final Callable<S> callable, final Exception failure)
      throws Exception {
    return callable.call();
  }

}
