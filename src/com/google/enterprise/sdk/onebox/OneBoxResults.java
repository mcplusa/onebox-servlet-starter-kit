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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class encapsulating a response result set from a call to a OneBox provider.
 * <p>
 * OneBox clients expect an XML response that adhere's to Google's Schema.  This
 * class, along with {@link ModuleResult} and {@link Field}, hides those details
 * by providing an API for conveniently building a result set.  The
 * {@link #serialize()} method serializes the class' current state as a valid
 * OneBox for Enterprise XML response.
 * 
 * @see ModuleResult
 * @see Field
 */
public class OneBoxResults implements IOneBoxResults
{
	public OneBoxResults() { }
	
	// A return code from the OneBox provider. The value can be one of the following:
	// "success", "lookupFailure", "securityFailure", or "timeout"
	// The value "success" is assumed if no value is returned, and results are
	// processed only on success. Although this element is optional, it is good
	// practice to always return a result code.
	private String m_resultCode = "success";
	
	// Optional. If lookup fails, the provider uses this element to send diagnostic
	// information and sets the resultCode attribute to a value other than success.
	private String m_diagnostics = null;
	
	/**
	 * In the event of provider failure, set the failure code and a diagnostic
	 * message.  If this is called on a results set, the OneBox request will be
	 * deemed unsuccessful and results will not appear in the end user's search
	 * results.
	 *  
	 * @param failureCode the code defining the nature of the failure
	 * @param diagnosticMessage descriptive text explaining the failure 
	 */
	public void setFailure(FailureCode failureCode, String diagnosticMessage)
	{
		m_resultCode = failureCode.toString();
		m_diagnostics = diagnosticMessage;
	}
    // The name of the provider.  The name need not match the name provided in
	// the OneBox module definition, and can be more descriptive than that name.
	private String m_providerText = null;
	/**
	 * Text identifying the provider that ultimately handled the OneBox request.
	 */
	public void setProviderText(String providerText) { m_providerText = providerText; }
	
	// The title of the result, consisting of a line of text and a link to the full
	// result set from the provider. This element is optional, but if one of the
	// following child elements is present, both must be present.
	private String m_urlText = null;
	private String m_urlLink = null;
	/**
	 * Optionally provide a title link for the results set.  The link would be a URL
	 * to a full result set and the text would be a display description for that link.
	 */
	public void setResultsTitleLink(String text, String url)
	{
		m_urlText = text;
		m_urlLink = url;
	}
	
	// The path to an image that is used as an identifying icon or informational
	// image for the result set.
	private String m_imageUrl = null;
	/**
	 * Optionally provide an identifying icon for the result set.  This is the URL
	 * path to an image.
	 */
	public void setImageUrl(String imageUrl) { m_imageUrl = imageUrl; }
	
	private List m_results = new ArrayList();
	/**
	 * Add an actual result to the OneBox provider's result set.  Up to eight results
	 * can be returned.
	 * @throws IndexOutOfBoundsException when too many results are added
	 */
	public void addResult(ModuleResult result)
	{
		if (m_results.size() >= 8)
			throw new IndexOutOfBoundsException("Attempt to return too many OneBox results");
		m_results.add(result);
	}
	/**
	 * Determines whether there is room to add another result.  If this returns true
	 * the next call to {@link #addResult(ModuleResult) addResult()} will succeed.
	 */
	public boolean canAddResult()
	{
		return m_results.size() < 8;
	}
	/**
	 * Serialize the current state of this OneBoxResults object to an XML string
	 * that conforms to the Google Schema for OneBox for Enterprise provider responses.
	 */
	public String serialize()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buf.append("<OneBoxResults xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
			.append(" xsi:noNamespaceSchemaLocation=\"oneboxresults.xsd\">");
		buf.append("<resultCode>").append(m_resultCode).append("</resultCode>");
		if (m_diagnostics != null) {
			buf.append("<Diagnostics>")
				.append(m_diagnostics.substring(0,Math.min(m_diagnostics.length(),256)))
				.append("</Diagnostics>");
		}
		if (m_providerText != null) {
			buf.append("<provider>")
				.append(m_providerText.substring(0,Math.min(m_providerText.length(),128)))
				.append("</provider>");
		}
		if ((m_urlText != null) && (m_urlLink != null)) {
			buf.append("<title>");
			buf.append("<urlText>")
				.append(m_urlText.substring(0,Math.min(m_urlText.length(),40)))
				.append("</urlText>");
			buf.append("<urlLink>").append(m_urlLink).append("</urlLink>");
			buf.append("</title>");
		}
		if (m_imageUrl != null) {
			buf.append("<IMAGE_SOURCE>").append(m_imageUrl).append("</IMAGE_SOURCE>");
		}
		for (Iterator iter = m_results.iterator(); iter.hasNext(); )
		{
			buf.append("<MODULE_RESULT>");
			ModuleResult moduleResult = (ModuleResult)iter.next();
			String url = moduleResult.getUrl();
			if (url != null) {
				buf.append("<U>").append(url).append("</U>");
			}
			String text = moduleResult.getText();
			if (text != null) {
				buf.append("<Title>").append(text).append("</Title>");
			}
			for (Iterator iter2 = moduleResult.getFields().iterator(); iter2.hasNext(); )
			{
				Field field = (Field)iter2.next();
				buf.append("<Field");
				for (Iterator iter3 = field.getAttrs().iterator(); iter3.hasNext(); )
				{
					String attr = (String)iter3.next();
					buf.append(" ").append(attr);
				}
				buf.append(">");
				if (field.getValue() != null) {
					buf.append(field.getValue());
				}
				buf.append("</Field>");
			}
			buf.append("</MODULE_RESULT>");
		}
		buf.append("</OneBoxResults>");
		return buf.toString();
	}
}
