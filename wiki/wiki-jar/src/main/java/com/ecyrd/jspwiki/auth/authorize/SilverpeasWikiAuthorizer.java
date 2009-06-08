package com.ecyrd.jspwiki.auth.authorize;

import java.security.Principal;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiSession;
import com.ecyrd.jspwiki.auth.WikiPrincipal;
import com.ecyrd.jspwiki.auth.WikiSecurityException;
import com.ecyrd.jspwiki.auth.authorize.WebAuthorizer;


public class SilverpeasWikiAuthorizer implements WebAuthorizer {

	
	public static final String ROLE_ATTR_NAME = "SilverpeasWikiRole";
	public static final String USER_ATTR_NAME = "SilverpeasWikiUser";
	private static Principal[] principals;
	static {
		principals = new WikiPrincipal[3];
		principals[0] = new WikiPrincipal("Admin");
		principals[1] = new WikiPrincipal("Authenticated");
		principals[2] = new WikiPrincipal("All");

	}

	public boolean isUserInRole(HttpServletRequest request, Principal principal) {
		String[] roles = (String[]) request.getAttribute(ROLE_ATTR_NAME);
		String requiredRole = principal.getName();
		for (int i =0; i<roles.length ; i++) {
			if (roles[i].equals(requiredRole))
				return true;
		}
		return false;
	}

	public Principal findRole(String role) {
		// TODO Auto-generated method stub
		return new WikiPrincipal(role); 
	}

	public Principal[] getRoles() {
	    return principals;
	}

	public void initialize(WikiEngine arg0, Properties arg1)
			throws WikiSecurityException {
		// TODO Auto-generated method stub

	}

	public boolean isUserInRole(WikiSession arg0, Principal arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
