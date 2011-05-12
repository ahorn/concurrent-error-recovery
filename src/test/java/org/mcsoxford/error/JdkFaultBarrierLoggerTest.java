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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@code FaultLogger} internal helper class.
 * 
 * @author Alex Horn
 */
@RunWith(MockitoJUnitRunner.class)
public class JdkFaultBarrierLoggerTest {

  @Mock
  private Logger jdkLoggerMock;

  private FaultBarrierLogger logger;

  @Before
  public void setup() {
    this.logger = new JdkFaultBarrierLogger(this.jdkLoggerMock, Level.INFO);
  }

  @Test
  public void recover() {
    when(this.jdkLoggerMock.isLoggable(Level.INFO)).thenReturn(true);
    this.logger.recover(null);

    verify(this.jdkLoggerMock).log(isA(LogRecord.class));
  }

  @Test
  public void recoverIgnore() {
    this.logger.recover(null);

    verify(this.jdkLoggerMock, never()).log(isA(LogRecord.class));
  }

  @Test
  public void abort() {
    when(this.jdkLoggerMock.isLoggable(Level.INFO)).thenReturn(true);
    this.logger.abort(null);

    verify(this.jdkLoggerMock).log(isA(LogRecord.class));
  }

  @Test
  public void abortIgnore() {
    this.logger.abort(null);

    verify(this.jdkLoggerMock, never()).log(isA(LogRecord.class));
  }

  @Test
  public void create() {
    assertNotNull(JdkFaultBarrierLogger.create(Level.INFO));
  }

  @Test(expected = IllegalArgumentException.class)
  public void createWithNullLevel() {
    JdkFaultBarrierLogger.create(null);
  }

}
