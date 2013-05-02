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

/**
 * Class encapsulates a directory of employee contact,
 * payroll, and organizational information.
 */
public class EmployeeDirectory {
	
	public static class EmployeeDirectoryEntry
	{
		public EmployeeDirectoryEntry( String _id,
				String _firstName, String _lastName,
				String _phone, String _email, String _position,
				String _department, String _building, String _office)
		{
			id = _id;
			firstName = _firstName;
			lastName = _lastName;
			phone = _phone;
			email = _email;
			position = _position;
			department = _department;
			building = _building;
			office = _office;
		}
		public String id;
		public String firstName;
		public String lastName;
		public String phone;
		public String email;
		public String position;
		public String department;
		public String building;
		public String office;
	}
	
	private static HashMap m_directory = null;
	private static HashMap directory()
	{
		if (m_directory == null)
		{
			m_directory = new HashMap();
			m_directory.put("jsmith",  new EmployeeDirectoryEntry("jsmith", "James", "Smith", "(408) 393-3160", "jsmith@acme.com", "Associate", "Marketing", "3214 Market St", "101A"));
			m_directory.put("jjohnson",  new EmployeeDirectoryEntry("jjohnson", "John", "Johnson", "(408) 393-3161", "jjohnson@acme.com", "Associate", "Marketing", "3214 Market St", "101B"));
			m_directory.put("rwilliams",  new EmployeeDirectoryEntry("rwilliams", "Robert", "Williams", "(408) 393-3162", "rwilliams@acme.com", "Sr Associate", "Marketing", "3214 Market St", "102A"));
			m_directory.put("mjones",  new EmployeeDirectoryEntry("mjones", "Michael", "Jones", "(408) 393-3163", "mjones@acme.com", "Contractor", "Marketing", "3214 Market St", "102B"));
			m_directory.put("wbrown",  new EmployeeDirectoryEntry("wbrown", "William", "Brown", "(408) 393-3164", "wbrown@acme.com", "Contractor", "Marketing", "3214 Market St", "103A"));
			m_directory.put("sbrown",  new EmployeeDirectoryEntry("sbrown", "Susan", "Brown", "(408) 393-3165", "sbrown@acme.com", "Sr Associate", "Marketing", "3214 Market St", "103B"));
			m_directory.put("rmiller",  new EmployeeDirectoryEntry("rmiller", "Richard", "Miller", "(408) 393-3166", "rmiller@acme.com", "Sr Manager", "Marketing", "3214 Market St", "104A"));
			m_directory.put("cwilson",  new EmployeeDirectoryEntry("cwilson", "Charles", "Wilson", "(408) 393-3167", "cwilson@acme.com", "Jr Associate", "Sales", "3214 Market St", "201A"));
			m_directory.put("jmoore",  new EmployeeDirectoryEntry("jmoore", "Joseph", "Moore", "(408) 393-3168", "jmoore@acme.com", "Jr Associate", "Sales", "3214 Market St", "201B"));
			m_directory.put("ttaylor",  new EmployeeDirectoryEntry("ttaylor", "Thomas", "Taylor", "(408) 393-3169", "ttaylor@acme.com", "Associate", "Sales", "3214 Market St", "202A"));
			m_directory.put("canderson",  new EmployeeDirectoryEntry("canderson", "Christopher", "Anderson", "(408) 393-3170", "canderson@acme.com", "Manager", "Sales", "3214 Market St", "202B"));
			m_directory.put("dthomas",  new EmployeeDirectoryEntry("dthomas", "Daniel", "Thomas", "(408) 393-3171", "dthomas@acme.com", "Sr Manager", "Sales", "3214 Market St", "203A"));
			m_directory.put("pjackson",  new EmployeeDirectoryEntry("pjackson", "Paul", "Jackson", "(408) 393-3172", "pjackson@acme.com", "Director", "Sales", "3214 Market St", "203B"));
			m_directory.put("mwhite",  new EmployeeDirectoryEntry("mwhite", "Mark", "White", "(408) 393-3173", "mwhite@acme.com", "Director", "Sales", "3214 Market St", "204A"));
			m_directory.put("dharris",  new EmployeeDirectoryEntry("dharris", "Donald", "Harris", "(408) 393-3174", "dharris@acme.com", "Associate", "Support", "1900 Chestnut Ave", "2190"));
			m_directory.put("gmartin",  new EmployeeDirectoryEntry("gmartin", "George", "Martin", "(408) 393-3175", "gmartin@acme.com", "Associate", "Support", "1900 Chestnut Ave", "2190"));
			m_directory.put("kthompson",  new EmployeeDirectoryEntry("kthompson", "Kenneth", "Thompson", "(408) 393-3176", "kthompson@acme.com", "Sr Associate", "Support", "1900 Chestnut Ave", "2190"));
			m_directory.put("sgarcia",  new EmployeeDirectoryEntry("sgarcia", "Steven", "Garcia", "(408) 393-3177", "sgarcia@acme.com", "Sr Associate", "Support", "1900 Chestnut Ave", "2190"));
			m_directory.put("emartinez",  new EmployeeDirectoryEntry("emartinez", "Edward", "Martinez", "(408) 393-3178", "emartinez@acme.com", "Manager", "Support", "1900 Chestnut Ave", "2192"));
			m_directory.put("brobinson",  new EmployeeDirectoryEntry("brobinson", "Brian", "Robinson", "(408) 393-3179", "brobinson@acme.com", "Manager", "Support", "1900 Chestnut Ave", "2192"));
			m_directory.put("rbrown",  new EmployeeDirectoryEntry("rbrown", "Ronald", "Brown", "(408) 393-3180", "rbrown@acme.com", "Manager", "Support", "1900 Chestnut Ave", "2192"));
			m_directory.put("arodriguez",  new EmployeeDirectoryEntry("arodriguez", "Anthony", "Rodriguez", "(408) 393-3181", "arodriguez@acme.com", "Sr Manager", "Support", "1900 Chestnut Ave", "2192"));
			m_directory.put("klewis",  new EmployeeDirectoryEntry("klewis", "Kevin", "Lewis", "(408) 393-3182", "klewis@acme.com", "Sr Director", "Engineering", "1900 Chestnut Ave", "3011"));
			m_directory.put("jlee",  new EmployeeDirectoryEntry("jlee", "Jason", "Lee", "(408) 393-3183", "jlee@acme.com", "Sr Developer", "Engineering", "1900 Chestnut Ave", "3012"));
			m_directory.put("jwalker",  new EmployeeDirectoryEntry("jwalker", "Jeff", "Walker", "(408) 393-3184", "jwalker@acme.com", "Developer", "Engineering", "1900 Chestnut Ave", "3013"));
			m_directory.put("jhall",  new EmployeeDirectoryEntry("jhall", "Jennifer", "Hall", "(408) 393-3185", "jhall@acme.com", "Sr Developer", "Engineering", "1900 Chestnut Ave", "3022"));
			m_directory.put("mallen",  new EmployeeDirectoryEntry("mallen", "Maria", "Allen", "(408) 393-3186", "mallen@acme.com", "Manager", "Engineering", "1900 Chestnut Ave", "3023"));
			m_directory.put("dyoung",  new EmployeeDirectoryEntry("dyoung", "David", "Young", "(408) 393-3187", "dyoung@acme.com", "Tech Lead", "Engineering", "1900 Chestnut Ave", "3026"));
			m_directory.put("mhernandez",  new EmployeeDirectoryEntry("mhernandez", "Margaret", "Hernandez", "(408) 393-3188", "mhernandez@acme.com", "Systems Admin", "Operations", "1900 Chestnut Ave", "3030"));
			m_directory.put("dking", new EmployeeDirectoryEntry("dking", "Dorothy", "King", "(408) 393-3189", "dking@acme.com", "Systems Admin", "Operations", "1900 Chestnut", "3037"));
		}
		return m_directory;
	}
	public static Iterator iterator()
	{
		return directory().values().iterator();
	}
	public static int size()
	{
		return directory().size();
	}
	public static EmployeeDirectoryEntry getEmployee(String id)
	{
		return (EmployeeDirectoryEntry)directory().get(id);
	}
}
