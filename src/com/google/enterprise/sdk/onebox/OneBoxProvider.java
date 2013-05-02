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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;


/**
 * Abstract base class servlet for providing OneBox results.
 * <p>
 * Services GET method requests from OneBox clients by unpacking their
 * request parameters and delegating to one of the overloaded
 * {@link #provideOneBoxResults provideOneBoxResults()} methods depending on the
 * <code>authType</code> parameter.
 * <p>
 * Providers need to extend this class and override one or more
 * of its <code>provideOneBoxResults</code> methods.  The base class
 * implementation of those methods will all throw
 * <code>UnsupportedOperationException</code>.
 * 
 * @see OneBoxResults
 */
public abstract class OneBoxProvider extends HttpServlet
{
	protected String webAppBaseURL;
	
	/**
	 * Called by the application server's servlet runner when GET method
	 * requests are made for this servlet.  OneBox clients (such as the
	 * Google Search Appliance) make HTTP requests to OneBox providers
	 * exclusivley using the GET method.
	 */
	public void doGet (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		getServletContext().log("--------------------------");
		logHeaders(request);
		webAppBaseURL = request.getScheme() + "://" +
						request.getServerName() + ":" + request.getServerPort() +
						request.getContextPath() + "/";
		
		processRequest(request, response);
		
		getServletContext().log("--------------------------");
	}

	/**
	 * Convenience method for logging the HTTP headers sent in the request.  This
	 * will log to the application server's log channel set up for this servlet
	 * and is purely meant for use during debugging. 
	 */
	protected void logHeaders(HttpServletRequest request)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Headers:\n");
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = (String)headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			buf.append("["+headerName +" = "+headerValue+"]\n");
		}
		getServletContext().log(buf.toString());
	}
	/**
	 * Top level request processor for this OneBox provider.
	 * <p>
	 * Unpacks the HTTP request's parameters and passes them into one of the
	 * <code>provideOneBoxResults</code> methods, based on the value of
	 * the <code>authType</code> parameter, for further processing.
	 * <p>
	 * Returns an HTTP response with XML that adhere's to the Google DTD
	 * defining the schema for OneBox for Enterprise provider results.
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String apiMaj = request.getParameter("apiMaj"); // integer (1,2,3,...)
		String apiMin = request.getParameter("apiMin"); // integer (0,1,2,3,...)
		String oneboxName = request.getParameter("oneboxName");
		
		String dateTime = request.getParameter("dateTime"); // optional, UTC format
		String ipAddr = request.getParameter("ipAddr"); // optional
		
		String lang = request.getParameter("lang"); // two-character language code
		String query = request.getParameter("query"); // UTF-8 encoded, URL-escaped
		
		// optional, match groups from regular expression evaluation
		int count = 0;
		ArrayList _matchGroups = new ArrayList();
		String match = request.getParameter("p"+(count++));
		while (match != null) {
			_matchGroups.add(match);
			match = request.getParameter("p"+(count++));
		}
		String[] matchGroups = (String[])_matchGroups.toArray(new String[_matchGroups.size()]);
		
		IOneBoxResults res = null;
		try
		{
			// authType == none | basic | ldap | sso
			String authType = request.getParameter("authType");
			if ("none".equals(authType))
			{
				res = provideOneBoxResults(apiMaj, apiMin, oneboxName, dateTime,
						ipAddr, lang, query, matchGroups);
			}
			else if ("basic".equals(authType))
			{
				String username = request.getParameter("userName");
				String password = request.getParameter("password");
				res = provideOneBoxResults(apiMaj, apiMin, oneboxName, dateTime,
						ipAddr, username, password, lang, query, matchGroups);
			}
			else if ("ldap".equals(authType))
			{
				// LDAP distinguished name (DN)
				String distinguishedName = request.getParameter("userName");
				res = provideOneBoxResults(apiMaj, apiMin, oneboxName, dateTime,
						ipAddr, distinguishedName, lang, query, matchGroups);
			}
			else if ("sso".equals(authType))
			{
				Cookie ssoCookie = null;
				String cookieName = request.getParameter("userName");
				Cookie[] cookies = request.getCookies();
				if (cookies != null)
				{
					for (int i=0; i<cookies.length; i++ )
					{
						Cookie c = cookies[i];
						if (c.getName().equals(cookieName))
						{
							ssoCookie = c;
							break;
						}
					}
				}
				res = provideOneBoxResults(apiMaj, apiMin, oneboxName, dateTime,
						ipAddr, ssoCookie, lang, query, matchGroups);
			}
			else
			{
				res = new OneBoxResults();
				((OneBoxResults)res).setFailure(FailureCode.securityFailure,
						"User authentication type not recognized: "+authType);
			}
		}
		catch (UnsupportedOperationException osoe)
		{
			res = new OneBoxResults();
			((OneBoxResults)res).setFailure(FailureCode.securityFailure,
					osoe.getMessage());
		}
		
		// Defines response MIME type
		response.setContentType("text/xml");
		PrintWriter out = response.getWriter();
		out.print(res.serialize());
		out.close();
	}

	/**
	 * Method to handle OneBox requests where no user authentication is required.
	 * 
	 * @param apiMaj the OneBox for Enterprise API major version
	 * @param apiMin the OneBox for Enterprise API minor version 
	 * @param oneboxName the name of the OneBox module
	 * @param dateTime the date and time of the OneBox query
	 * @param ipAddr the IP address of the end user as determined by the OneBox client
	 * @param lang the language of the user's browser, two character language code 
	 * @param query the end user's query, UTF-8 encoded and URL-escaped
	 * @param matchGroups reqular expression evaluation match groups
	 * @return IOneBoxResults
	 */
	protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin, String oneboxName,
			String dateTime, String ipAddr, String lang, String query, String[] matchGroups)
	{
		throw new UnsupportedOperationException("User authentication type not supported: \"None\"");
	}
	
	/**
	 * Method to handle OneBox requests where simple username / password based
	 * authentication of the end user is required.
	 * 
	 * @param apiMaj the OneBox for Enterprise API major version
	 * @param apiMin the OneBox for Enterprise API minor version
	 * @param oneboxName the name of the OneBox module
	 * @param dateTime the date and time of the OneBox query
	 * @param ipAddr the IP address of the end user as determined by the OneBox client
	 * @param userName the username entered by the end user
	 * @param password the password entered by the end user
	 * @param lang the language of the user's browser, two character language code
	 * @param query the end user's query, UTF-8 encoded and URL-escaped
	 * @param matchGroups reqular expression evaluation match groups
	 * @return IOneBoxResults
	 */
	protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin, String oneboxName,
			String dateTime, String ipAddr, String userName, String password, String lang,
			String query, String[] matchGroups)
	{
		throw new UnsupportedOperationException("User authentication type not supported: \"Basic\"");
	}
	/**
	 * Method to handle OneBox requests where LDAP based authentication of the
	 * end user is required. 
	 * 
	 * @param apiMaj the OneBox for Enterprise API major version
	 * @param apiMin the OneBox for Enterprise API minor version
	 * @param oneboxName the name of the OneBox module
	 * @param dateTime the date and time of the OneBox query
	 * @param ipAddr the IP address of the end user as determined by the OneBox client
	 * @param distinguishedName the distinguished name from the LDAP lookup of the end user
	 * @param lang the language of the user's browser, two character language code
	 * @param query the end user's query, UTF-8 encoded and URL-escaped
	 * @param matchGroups reqular expression evaluation match groups
	 * @return IOneBoxResults
	 */
	protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin, String oneboxName,
			String dateTime, String ipAddr, String distinguishedName, String lang, String query,
			String[] matchGroups)
	{
		throw new UnsupportedOperationException("User authentication type not supported: \"LDAP\"");
	}
	
	/**
	 * Method to handle OneBox requests where forms based (Single Sign-On) authentication
	 * of the end user is required.
	 * 
	 * @param apiMaj the OneBox for Enterprise API major version
	 * @param apiMin the OneBox for Enterprise API minor version
	 * @param oneboxName the name of the OneBox module
	 * @param dateTime the date and time of the OneBox query
	 * @param ipAddr the IP address of the end user as determined by the OneBox client
	 * @param ssoCookie the cookie from the authentication system containing the trusted user identity
	 * @param lang the language of the user's browser, two character language code
	 * @param query the end user's query, UTF-8 encoded and URL-escaped
	 * @param matchGroups reqular expression evaluation match groups
	 * @return IOneBoxResults
	 */
	protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin, String oneboxName,
			String dateTime, String ipAddr, Cookie ssoCookie, String lang, String query,
			String[] matchGroups)
	{
		throw new UnsupportedOperationException("User authentication type not supported: \"SSO\"");
	}
}
