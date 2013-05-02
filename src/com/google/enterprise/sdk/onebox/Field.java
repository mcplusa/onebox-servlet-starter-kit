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
 * Class representing a unit of metadata for a OneBox provider result.
 * <p>
 * Each Field element has at minimum a name and a value.  It then has
 * other name/value pair attributes as needed. 
 * 
 * @see ModuleResult
 */
public class Field
{
	private List m_attrs = new ArrayList();
	private String m_value;
	/**
	 * The constructor requires the name of the Field element and the
	 * value of the element.
	 * 
	 * @param name
	 * @param value
	 */
	public Field(String name, String value) { addAttr("name", name); m_value = value; }
	/**
	 * Optionally, additional attributes can be added to the Field
	 * element as needed for display formatting.
	 *  
	 * @param name
	 * @param value
	 */
	public void addAttr(String name, String value) { m_attrs.add(name+"=\""+value+"\""); }
	
	/**
	 * @return the List of attributes for this Field element where each
	 * attribute is a String of form: name="value".  The List includes
	 * the "name" attribute.
	 */
	public List getAttrs() { return m_attrs; }
	/**
	 * @return the value of the Field element
	 */
	public String getValue() { return m_value; }
}
