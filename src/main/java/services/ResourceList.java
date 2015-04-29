package services;

import java.util.ArrayList;

public class ResourceList extends ArrayList<Resource> {

	private static final long serialVersionUID = -5700137488625529826L;

	public Resource getByName(String resourceName) {
		for (Resource resource : this) {
			if (resource.getName().equals(resourceName)) {
				return resource;
			}
		}
		
		return null;
	}
	
	// @return the removed resource if exists, null otherwise
	public Resource removeByName(String resourceName) {
		for (int i=0; i<this.size(); i++) {
			Resource resource = this.get(i);
			if (resource.getName().equals(resourceName)) {
				this.remove(i);
				return resource;
			}
		}
		
		return null;
	}
	
}
