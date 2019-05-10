package valueobject;

import java.util.List;

public class Opera {

	private String name;
	private String qID;
	private String url;
	private List<String> componist;
	private List<List<String>> roles;
	private List<Role> roleNames;

	public Opera(String meta) {
		String[] split = meta.split(",");
		qID = split[1];
		url = split[2];
		name = split[3];
	}
	
	public Opera() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getqID() {
		return qID;
	}

	public void setqID(String qID) {
		this.qID = qID;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getComponist() {
		return componist;
	}

	public void setComponist(List<String> componist) {
		this.componist = componist;
	}

	public List<List<String>> getRoles() {
		return roles;
	}

	public void setRoles(List<List<String>> roles) {
		this.roles = roles;
	}
	
	public List<Role> getRoleNames() {
		return roleNames;
	}

	public void setRoleNames(List<Role> roleNames) {
		this.roleNames = roleNames;
	}

	@Override
	public String toString() {
		return "Opera [name=" + name + ", qID=" + qID + ", url=" + url + ", componist=" + componist + ", roles=" + roles + "]";
	}

	public String getCSVHeader() {
		return "ID,NAME,QID,URL";
	}

	public String toCSVString() {
		return qID + "," + name + "," + url;
	}
	
	public String getHeaderRow() {
		StringBuffer sb = new StringBuffer();
		for (String list : roles.get(0)) {
			sb.append(list);
			sb.append("|");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componist == null) ? 0 : componist.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((qID == null) ? 0 : qID.hashCode());
		result = prime * result + ((roleNames == null) ? 0 : roleNames.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Opera other = (Opera) obj;
		if (componist == null) {
			if (other.componist != null)
				return false;
		} else if (!componist.equals(other.componist))
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
		if (roleNames == null) {
			if (other.roleNames != null)
				return false;
		} else if (!roleNames.equals(other.roleNames))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
