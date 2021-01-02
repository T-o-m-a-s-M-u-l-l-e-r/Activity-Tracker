package model;
import java.util.Hashtable;

import org.joda.time.DateTime;

class WindowProcess {
	protected Hashtable<String, String> propertyDictionary;
	private DateTime startTime;

	public static enum Property {
		Name, Path, Id, StartTime, Company, Product, Description;
	}

	public WindowProcess(Hashtable<String, String> propertyDictionary) {
		this.propertyDictionary = propertyDictionary;
		startTime = Tracker.powershellFormatter.parseDateTime(propertyDictionary.get(Property.StartTime.toString()));
	}

	public String getProperty(String key) {
		return propertyDictionary.get(key);
	}

	public boolean equals(Object obj) {
		if (obj instanceof Program) {
			
			Program compareTo = (Program) obj;
			Property[] compareProperties = { Property.Name, Property.Company, Property.Path, Property.Product,
					Property.Description };
			for (Property property : compareProperties) {
				String key = property.toString();
				
				String compareToProperty = property.equals(Property.Path)?compareTo.getProperty(key).toLowerCase():compareTo.getProperty(key);
				String myProperty = property.equals(Property.Path)?getProperty(key).toLowerCase():getProperty(key);
				
				if (!compareToProperty.equals(myProperty)) {
					return false;
				}
			}
			return true;
		}
		
		if (obj instanceof WindowProcess) {
			WindowProcess compareTo = (WindowProcess) obj;
			String key = Property.Id.toString();
			return compareTo.getProperty(key).equals(getProperty(key));
		}
		
		return super.equals(obj);
	}

	public Hashtable<String, String> getDictionary() {
		return propertyDictionary;
	}

	public int getId() {
		return Integer.valueOf(propertyDictionary.get(Property.Id.toString()));
	}

	public DateTime getStartTime() {
		return startTime;
	}

}