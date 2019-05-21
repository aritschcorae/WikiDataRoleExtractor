package valueobject;

import java.util.List;

import utils.Utils;

/**
 * Valueobject representing a performance with roles and a composer
 * 
 * @author maccl
 *
 */
public class Play {

	private String name;
	private String qID;
	private String url;
	private List<String> composers;
	private List<List<String>> roles;
	private List<Role> roleNames;

	public Play(String meta) {
		String[] split = meta.split(",");
		qID = split[1];
		url = split[2];
		name = split[3];
	}
	
	public Play() {
		
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

	public List<String> getComposerList() {
		return composers;
	}
	public String getComposersAsString() {
		String composerList = Utils.EMPTY_STRING;
		for (int i = 0; i < composers.size(); i++){
			composerList += composers.get(i);
			if(i + 1 < composers.size()) {
				composerList += ",";
			}
		}
		return composerList;
	}

	public void setComposerList(List<String> componist) {
		this.composers = componist;
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
		return "Performance [name=" + name + ", qID=" + qID + ", url=" + url + ", componist=" + composers + ", roles=" + roles + "]";
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
		result = prime * result + ((composers == null) ? 0 : composers.hashCode());
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
		Play other = (Play) obj;
		if (composers == null) {
			if (other.composers != null)
				return false;
		} else if (!composers.equals(other.composers))
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
