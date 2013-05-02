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
import java.util.Iterator;

/**
 * Implementation of a OneBox provider that requires no user authentication
 * or authorization.
 * 
 * The <code>provideOneBoxResults</code> method will have no insight into
 * the end user's identity for decision making purposes.
 */
public class SampleNoAuthOneBoxProvider extends OneBoxProvider {

	/**
	 * Implementation of a OneBox provider that requires no user authentication.
	 * 
	 * @param apiMaj
	 * @param apiMin
	 * @param oneboxName
	 * @param dateTime
	 * @param ipAddr
	 * @param lang
	 * @param query
	 * @param matchGroups
	 * @return
	 */
	protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin,
			String oneboxName, String dateTime, String ipAddr, String lang,
			String query, String[] matchGroups)
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
		
		res.setProviderText("SampleNoAuthOneBoxProvider: ACME Employee Directory");
		res.setImageUrl(webAppBaseURL + "images/acme.JPG");
		
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
				matchCount++;
				if (res.canAddResult())
				{
					ModuleResult mr = new ModuleResult(emp.lastName+", "+emp.firstName,
							webAppBaseURL + "acme_directory.html");
					mr.addField(new Field("position",emp.position));
					mr.addField(new Field("department",emp.department));
					mr.addField(new Field("phone",emp.phone));
					mr.addField(new Field("email",emp.email));
					mr.addField(new Field("building",emp.building));
					mr.addField(new Field("office",emp.office));
					res.addResult(mr);
				}
			}
		}
		res.setResultsTitleLink(matchCount + " matching results in the ACME Employee Directory",
				webAppBaseURL + "acme_directory.html");
		return res;
	}
}
