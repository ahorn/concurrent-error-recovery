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

/**
 * API to recover from exceptions.
 * 
 * @author Alex Horn
 */
public interface RetryHandler<S> {

  /**
   * Closure-style interface to invoke code that attempts to recover from a
   * failure in the intended {@code callable} execution. If successful it may
   * return an object of the same type as the non-faulty execution path would
   * have produced.
   * 
   * @param callable closure-style object that refers to the original operation
   *          that caused the failure
   * @param failure exception that was triggered when the {@code callable} was
   *          invoked
   * @throws Exception if the recovery logic results in a fatal error
   * @return object produced by recovery logic
   */
  S retry(Callable<S> callable, Exception failure) throws Exception;

}
