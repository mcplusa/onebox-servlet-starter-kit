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

import com.google.enterprise.sdk.onebox.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Implementation of a OneBox provider that implements its own
 * authentication and authorization based on the end user's username
 * and password.
 * 
 * In this case by the time the execution path comes to the OneBox
 * provider, the user has already been challenged for authentication
 * credentials via either the HTTP Basic or the NTLM challenge protocols
 * or via an LDAP server.  The username and password provided by the
 * user is passed into the <code>provideOneBoxResults</code> method
 * and can be used to authenticate the user and to make authorization
 * decisions.
 */
public class SampleBasicAuthOneBoxProvider extends OneBoxProvider {

	static private class UserStore {
		private static HashMap m_passwords = null;
		static HashMap passwords()
		{
			if (m_passwords == null) {
				m_passwords = new HashMap();
				m_passwords.put("wbrown", "wbrown");
				m_passwords.put("jsmith", "jsmith");
				m_passwords.put("sbrown", "sbrown");
				m_passwords.put("rmiller", "rmiller");
				m_passwords.put("mhernandez", "mhernandez");
			}
			return m_passwords;
		}
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
	 * Implementation of a OneBox provider that uses the user's username
	 * and password for both authentication and authorization.
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
			String oneboxName, String dateTime, String ipAddr, String userName,
			String password, String lang, String query, String[] matchGroups)
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
		
		// Authenticate the user/pass
		String _password = (String)UserStore.passwords().get(userName);
		if ((_password == null) || (!_password.equals(password))) {
			res.setFailure(FailureCode.securityFailure, "User authentication failed");
			return res;
		}
		// If we get to this point, we know the user is who they say they are.  We still
		// need to determine whether they are authorized to view the request results.
		
		res.setProviderText("SampleBasicAuthOneBoxProvider: ACME Employee Directory");
		res.setImageUrl(webAppBaseURL + "images/acme.JPG");
		
		// Obtain user information for authorization
		String role = (String)UserStore.roles().get(userName);
		EmployeeDirectory.EmployeeDirectoryEntry currentUser = 
			EmployeeDirectory.getEmployee(userName);
		if (role == null || currentUser == null) {
			res.setFailure(FailureCode.lookupFailure, "Lookup failure during authorization");
			return res;
		}

		int matchCount = 0;
		// Search by brute force.  In a real provider with a larger dataset or with
		// more complex computations you'd want to use a faster, more robust lookup
		// mechanism such as an LDAP service or a relational database.
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
}
