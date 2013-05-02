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

import com.google.enterprise.sdk.onebox.*;

/**
 * Implementation of a OneBox provider that requires LDAP based user
 * authentication prior to access.  In this case by the time the
 * execution path comes to the OneBox provider, the user has already
 * been authenticated by the LDAP server.
 * 
 * For authorization purposes the user's distinguished name (DN) from
 * the LDAP server is passed into the <code>provideOneBoxResults</code>
 * method and can be used as a trusted identity of the user.
 */
public class SampleLDAPAuthOneBoxProvider extends OneBoxProvider {

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
	 * Implementation of a OneBox provider that requires LDAP based
	 * user authentication and uses the user's DN for authorization.
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
			String oneboxName, String dateTime, String ipAddr, String distinguishedName,
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
		
		res.setProviderText("SampleLDAPAuthOneBoxProvider: ACME Employee Directory");
		res.setImageUrl(webAppBaseURL + "images/acme.JPG");

		// Obtain user information for authorization
		String userId = extractUserId(distinguishedName);
		if (userId == null) {
			res.setFailure(FailureCode.lookupFailure, "Missing UID from LDAP DN");
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
	 * Look for the user id (UID) attribute in the distinguished name (DN).  DNs
	 * will vary widely depending on your LDAP setup.  The method makes some
	 * assumptions for simplicity that you may not want to make in your environment.
	 *  
	 * @param distinguishedName
	 * @return null if UID is not found, otherwise the user id
	 */
	protected String extractUserId(String distinguishedName)
	{
		// tokenize the relative distinguished names and the attributes and
		// then select the attribute with the UID type
		StringTokenizer rdnTok = new StringTokenizer(distinguishedName, ",");
		while (rdnTok.hasMoreTokens())
		{
			String rdn = rdnTok.nextToken();
			StringTokenizer atvTok = new StringTokenizer(rdn, "+"); 
			while (atvTok.hasMoreTokens())
			{
				String atv = atvTok.nextToken();
				if (atv.toUpperCase().startsWith("UID="))
				{
					// return the user id
					return atv.substring(4);
				}
			}
		}
		return null;
	}
}
