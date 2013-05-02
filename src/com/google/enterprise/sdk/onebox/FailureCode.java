/*
 * Copyright (C) 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.enterprise.sdk.onebox;

/**
 * Failure return codes that a OneBox provider can report.
 * <p>
 * The failure return codes recognized by OneBox clients include
 * {@link #lookupFailure}, {@link #securityFailure}, and
 * {@link #timeout}.
 * <p>
 * In the case of failure during OneBox provider processing, a
 * failure code is set using the
 * {@link OneBoxResults#setFailure OneBoxResults.setFailure()}
 * method.
 * 
 * 
 * @see OneBoxResults
 */
public final class FailureCode {
    private FailureCode(String desc) { m_desc = desc; }
	private String m_desc;
    public String toString() {return m_desc; }
    public static final FailureCode lookupFailure = new FailureCode("lookupFailure");
    public static final FailureCode securityFailure = new FailureCode("securityFailure");
    public static final FailureCode timeout = new FailureCode("timeout");
  }
