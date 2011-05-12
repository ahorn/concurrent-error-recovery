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

/**
 * Helper API to log retry attempts.
 * 
 * @see FaultBarrier
 * @author Alex Horn
 */
public interface FaultBarrierLogger {

  /**
   * Notify the logger facility that a failure has occurred from which the
   * {@link FaultBarrier} will try to recover. The message should be of high
   * severity but it does not require immediate action by the operator.
   * 
   * @param failure exception that caused the failure
   */
  void recover(Exception failure);

  /**
   * Write high-level diagnostic message to the logger facility to indicate that
   * the retry logic was unable to recover from a system failure.
   * 
   * @param fatal cause of the unrecoverable error
   */
  void abort(Exception fatal);

}
