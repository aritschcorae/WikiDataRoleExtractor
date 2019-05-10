package valueobject;

public class Role {

	private String qID;
	private String name;
	private String description;
	private String defaultDescription;

	public String getqID() {
		return qID;
	}

	public void setqID(String qID) {
		this.qID = qID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		if(description == null) {
			return "";
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultDescription() {
		return defaultDescription;
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultDescription == null) ? 0 : defaultDescription.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((qID == null) ? 0 : qID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (defaultDescription == null) {
			if (other.defaultDescription != null)
				return false;
		} else if (!defaultDescription.equals(other.defaultDescription))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (qID == null) {
			if (other.qID != null)
				return false;
		} else if (!qID.equals(other.qID))
			return false;
		return true;
	}

}
