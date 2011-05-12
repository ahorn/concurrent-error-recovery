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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * JDK implementation of helper API that is used to log retry attempts. Log
 * messages are logged as {@link Level#INFO} in English.
 * 
 * @author Alex Horn
 */
public class JdkFaultBarrierLogger implements FaultBarrierLogger {

  /**
   * JDK logger facility
   */
  private final Logger logger;

  /**
   * Default JDK {@link Logger} level with which abnormal activities are logged.
   */
  private final Level level;

  /**
   * Constructor that allows the injection of a JDK logger facility. Subclasses
   * may wish to override the {@link #log(String, Throwable)} method to adjust
   * the severity or format of the log message.
   * 
   * @param logger JDK logger used to report errors
   */
  protected JdkFaultBarrierLogger(final Logger logger, final Level level) {
    this.logger = logger;
    this.level = level;
  }

  /**
   * Create a new {@link FaultBarrierLogger} that uses the JDK {@link Logger}
   * API to report errors in the specified {@link Level}.
   * 
   * @throws IllegalArgumentException if JDK logger {@code level} argument is
   *           {@code null}
   */
  static FaultBarrierLogger create(final Level level) {
    if (level == null) {
      throw new IllegalArgumentException("Level must not be null");
    }

    final Logger defaultLogger = Logger.getLogger(FaultBarrier.class.getName());
    return new JdkFaultBarrierLogger(defaultLogger, level);
  }

  /**
   * Notify the logger facility that a failure has occurred.
   * 
   * @param failure exception that caused the failure
   */
  public void recover(final Exception failure) {
    log("Encountered failure", failure);
  }

  /**
   * Notify the logger facility that the retry logic was unable to recover from
   * a failure.
   * 
   * @param fatal cause of the unrecoverable error
   */
  public void abort(final Exception fatal) {
    log("Abort recovery", fatal);
  }

  /**
   * Log a server error message to the reporting facility that was injected at
   * instantiation.
   * 
   * @param message human-readable message describing the nature of the error
   * @param cause cause that describes the nature of the runtime exception
   */
  protected void log(final String message, final Throwable cause) {
    if (this.logger.isLoggable(this.level)) {
      final LogRecord record = new LogRecord(this.level, message);
      record.setThrown(cause);
      this.logger.log(record);
    }
  }

}
