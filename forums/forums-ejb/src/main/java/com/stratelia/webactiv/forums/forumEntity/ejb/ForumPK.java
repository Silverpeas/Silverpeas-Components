package com.stratelia.webactiv.forums.forumEntity.ejb;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Clé primaire associée à un forum.
 * @author frageade
 * @since November 2000
 */
public class ForumPK extends WAPrimaryKey {
  
	private String domain;

	public ForumPK(String component, String domain, String id) {
		super(id, component);
		this.domain = domain;
	}
	
	public ForumPK(String component, String domain) {
		this(component, domain, "0");
	}

	public String getDomain() {
		return domain;
	}
	
  	public boolean equals(Object other) {
  		return ((other instanceof ForumPK)
  			&& (getInstanceId().equals(((ForumPK) other).getInstanceId()))
  			&& (domain.equals(((ForumPK) other).getDomain()))
  			&& (getId().equals(((ForumPK) other).getId())));
  	}

}