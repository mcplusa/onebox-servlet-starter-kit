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

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;

import com.google.enterprise.sdk.onebox.*;

/**
 * Implementation of a OneBox provider that requires single sign on
 * (SSO) based user authentication prior to access.  In this case by
 * the time the execution path comes to the OneBox provider, the user
 * has already been authenticated by the SSO system.
 * 
 * For authorization purposes the SSO cookie identifying the user is
 * passed into the <code>provideOneBoxResults</code> method.
 */
public class SampleSSOAuthOneBoxProvider extends OneBoxProvider {

	static private class UserStore {
		private static HashMap m_roles = null;
		static String ROLE_CONTRACTOR = "contractor";
		static String ROLE_EMPLOYEE = "employee";
		static String ROLE_MANAGER = "manager";
		static String ROLE_ADMIN = "admin";
		static HashMap roles()
		{
			if (m_roles == null) {
				m_roles = new HashMap();
				m_roles.put("wbrown", ROLE_CONTRACTOR);
				m_roles.put("jsmith", ROLE_EMPLOYEE);
				m_roles.put("sbrown", ROLE_EMPLOYEE);
				m_roles.put("rmiller", ROLE_MANAGER);
				m_roles.put("mhernandez", ROLE_ADMIN);
			}
			return m_roles;
		}
	}

	/**
	 * Implementation of a OneBox provider that requires SSO based user
	 * authentication and uses the SSO cookie for authorization.
	 *  
	 * @param apiMaj
	 * @param apiMin
	 * @param oneboxName
	 * @param dateTime
	 * @param ipAddr
	 * @param userName
	 * @param password
	 * @param lang
	 * @param query
	 * @param matchGroups
	 * @return
	 */
	protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin,
			String oneboxName, String dateTime, String ipAddr, Cookie ssoCookie,
			String lang, String query, String[] matchGroups)
	{
		// Create results object
		OneBoxResults res = new OneBoxResults();

		// Check api version for compatibility
		int apiVersion = ((Integer.parseInt(apiMaj) << 0xFF) | (Integer.parseInt(apiMin)));
		if (apiVersion < ((1 << 0xFF) | 0)) {	// 1.0
			res.setFailure(FailureCode.lookupFailure,
					"OneBox API versions older than 1.0 not supported by provider");
			return res;
		}
		// Check language for compatibility
		if (!"en".equalsIgnoreCase(lang)) {
			res.setFailure(FailureCode.lookupFailure,
					"Languages other than english not supported by provider");
			return res;
		}

		//
		// TODO implement your OneBox here...
		//

		res.setProviderText("SampleSSOAuthOneBoxProvider: ACME Employee Directory");
		res.setImageUrl(webAppBaseURL + "images/acme.JPG");

		// Obtain user information for authorization
		if (ssoCookie == null){
			res.setFailure(FailureCode.lookupFailure, "Lookup failure during authorization");
			return res;
		}
		String userId = extractUserId(ssoCookie);
		if (userId == null) {
			res.setFailure(FailureCode.lookupFailure, "Lookup failure of user from SSO cookie");
			return res;
		}
		String role = (String)UserStore.roles().get(userId);
		EmployeeDirectory.EmployeeDirectoryEntry currentUser =
			EmployeeDirectory.getEmployee(userId);
		if (role == null || currentUser == null) {
			res.setFailure(FailureCode.lookupFailure, "Lookup failure during authorization");
			return res;
		}
		
		int matchCount = 0;
		// Search by brute force.  In a real provider with a larger dataset or with
		// more complex computations you'd want to use a faster, more robust lookup
		// mechanism such as a relational database.
		for (Iterator iter = EmployeeDirectory.iterator(); iter.hasNext(); )
		{
			EmployeeDirectory.EmployeeDirectoryEntry emp =
				(EmployeeDirectory.EmployeeDirectoryEntry)iter.next();
			
			if (query.toUpperCase().equals(emp.lastName.toUpperCase()))
			{
				// Authorize the user to view this result
				// Admins can view all records, employees and managers can view records
				// from their department, and contractors can view only themselves
				if (UserStore.ROLE_ADMIN.equals(role) ||
					((UserStore.ROLE_EMPLOYEE.equals(role) || UserStore.ROLE_MANAGER.equals(role)) &&
								currentUser.department.equals(emp.department)) ||
					(UserStore.ROLE_CONTRACTOR.equals(role) && currentUser.id.equals(emp.id)))
				{
					matchCount++;
					if (res.canAddResult())
					{
						ModuleResult mr = new ModuleResult(emp.lastName+", "+emp.firstName,
								webAppBaseURL + "acme_directory.html");
						mr.addField(new Field("position",emp.position));
						mr.addField(new Field("department",emp.department));
						// Authorize the user to view these fields
						if (UserStore.ROLE_ADMIN.equals(role) ||
								UserStore.ROLE_MANAGER.equals(role) ||
								currentUser.id.equals(emp.id))
						{
							mr.addField(new Field("phone",emp.phone));
							mr.addField(new Field("email",emp.email));
							mr.addField(new Field("building",emp.building));
							mr.addField(new Field("office",emp.office));
						}
						res.addResult(mr);
					}
				}
			}
		}
		res.setResultsTitleLink(matchCount + " matching results in the ACME Employee Directory",
				webAppBaseURL + "acme_directory.html");
		return res;
	}
	/**
	 * Look for the user id given an SSO cookie from the user's request.  This
	 * will usually require a call to the SSO system since that system will be
	 * the only one able to interpret the cookie's value.  Alternatively the
	 * base class' {@link #processRequest processRequest()} method can be
	 * overridden to provide access to the HttpServletRequest for retrieving the 
	 * user id from the request headers, if the SSO system places them there. 
	 *  
	 * @param ssoCookie
	 * @return
	 */
	public String extractUserId(Cookie ssoCookie)
	{
		// TODO: this implementation will depend on your SSO system and the structure of
		// the systems' authentication cookie
		return ssoCookie.getValue();
	}
}
