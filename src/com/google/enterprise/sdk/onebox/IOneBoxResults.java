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
 * Interface that must be implemented by classes encapsulating OneBox
 * for Enterprise result sets.  This interface is used in the
 * {@link OneBoxProvider#processRequest OneBoxProvider.processRequest()}
 * method to write out an HTTP response to the OneBox request.
 * <p>
 * Your OneBoxProvider can create a custom results set object as long
 * as it implements this interface.
 * 
 * @see OneBoxProvider
 * @see OneBoxResults
 */
public interface IOneBoxResults {
	/**
	 * Returns an XML String that describes a OneBox results set and
	 * adheres to Google's OneBox for Enterprise response Schema.
	 */
	public String serialize();
}
