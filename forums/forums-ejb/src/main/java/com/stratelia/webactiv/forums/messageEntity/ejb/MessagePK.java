package com.stratelia.webactiv.forums.messageEntity.ejb;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Clé primaire associée à un message.
 * @author frageade
 * @since November 2000
 */
public class MessagePK extends WAPrimaryKey {
  
	private String domain;

	public MessagePK(String component, String domain, String id) {
		super(id, component);
		this.domain = domain;
	}
	
	public MessagePK(String component, String domain) {
		this(component, domain, "0");
	}

	public String getDomain() {
		return domain;
	}
  
	public boolean equals(Object other) {
  		return ((other instanceof MessagePK)
  			&& (getInstanceId().equals(((MessagePK) other).getInstanceId()))
  			&& (domain.equals(((MessagePK) other).getDomain()))
  			&& (getId().equals(((MessagePK) other).getId())));
  	}
  
}