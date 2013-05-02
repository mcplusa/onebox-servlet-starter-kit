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

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a single result from a OneBox provider response.
 * <p>
 * Used in conjunction with the
 * {@link OneBoxResults#addResult OneBoxResults.addResult()}
 * method to add a result element to the results set.  Up to
 * eight <code>ModuleResult</code> objects may be added to
 * a <code>OneBoxResults</code> instance.
 *  
 * @see OneBoxResults
 */
public class ModuleResult
{
	private String m_text;
	private String m_url;
	private List m_fields = new ArrayList();
	/**
	 * The only constructor requires both a URL for the result link
	 * and a display text String for the link. 
	 */
	public ModuleResult(String text, String url) { m_text = text; m_url = url; }
	/**
	 * Optionally, up to eight {@link Field} elements can be added to
	 * the result to provide additional information for display.
	 */
	public void addField(Field field) { m_fields.add(field); }
	
	/**
	 * @return the display text for the result link
	 */
	public String getText() { return m_text; }
	/**
	 * @return the URL for the result link
	 */
	public String getUrl() { return m_url; }
	/**
	 * @return a List of the {@link Field} elements added, if any
	 */
	public List getFields() { return m_fields; }
}
