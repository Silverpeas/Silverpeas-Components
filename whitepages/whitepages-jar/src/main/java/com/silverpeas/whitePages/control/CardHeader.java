package com.silverpeas.whitePages.control;

import java.util.*;

import com.stratelia.silverpeas.contentManager.*;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.whitePages.model.*;
import com.silverpeas.whitePages.record.*;
import com.silverpeas.form.*;
import com.stratelia.webactiv.beans.admin.*;
import com.stratelia.webactiv.util.*;

/**
 * The fileboxplus implementation of SilverContentInterface
 */
public final class CardHeader extends AbstractI18NBean implements
		SilverContentInterface, Comparable {
	private long id;
	private String label;
	private String instanceId;
	private String date;
	private String creatorId;
	private String description;
	private final static ResourceLocator whitePagesIcons = new ResourceLocator("com.silverpeas.whitePages.settings.whitePagesIcons", "");

	public void init(long id, Card card) {
		this.id = id;

		String label = null;
		try {
			UserRecord user = getUserRecord(card);
			if (user == null)
				label = "user(" + id + ")";
			else {
				/*
				 * Label value.
				 */
				Field firstName = user.getField("FirstName");
				Field name = user.getField("LastName");
				label = name.getValue("") + " " + firstName.getValue("");

				/*
				 * Description value.
				 */
				String isMailHidden = getParam("isEmailHidden", instanceId);
				if ((isMailHidden != null) && (isMailHidden.equals("yes"))) {
					this.description = buildMailLink(instanceId, card.getPK().getId());
				} else {
					this.description = ((Field) user.getField("Mail"))
							.getValue("");
				}

			}
		} catch (FormException e) {
			label = "user(" + id + ")";
		}
		this.label = label;
		this.instanceId = card.getInstanceId();
	}

	private String buildMailLink(String instanceId, String cardId) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<a href=\"");
		buffer.append( URLManager.getApplicationURL() );
		buffer.append( URLManager.getURL(null, instanceId) );
		buffer.append( "NotifyExpert?cardId=");
		buffer.append( cardId );
		buffer.append( "\"><img src=\"" );
		buffer.append( URLManager.getApplicationURL() );
		buffer.append( whitePagesIcons.getString("whitePages.notify") );
		buffer.append( "\" border=\"0\"></a>");
		
		return buffer.toString(); 
	}

	public CardHeader(long id, Card card) {
		init(id, card);
	}

	public CardHeader(long id, Card card, String instanceId, String date,
			String creatorId) {
		this.instanceId = instanceId;
		this.date = date;
		this.creatorId = creatorId;
		init(id, card);
	}

	public String getName() {
		return label;
	}

	public String getDescription() {
		return this.description;
	}

	public String getURL() {
		return "consultIdentity?userCardId=" + id;
	}

	static private UserRecord getUserRecord(Card card) {
		try {
			UserTemplate templateUser = getUserTemplate(card.getInstanceId());
			card.writeUserRecord(templateUser.getRecord(card.getUserId()));
			UserRecord user = card.readUserRecord();

			return user;
		} catch (Exception e) {
			return null;
		}
	}

	static private UserTemplate getUserTemplate(String instanceId) {
		UserTemplate template = (UserTemplate) templates.get(instanceId);

		if (template == null) {
			ResourceLocator templateSettings = new ResourceLocator(
					"com.silverpeas.whitePages.settings.template", "");
			String templateDir = templateSettings.getString("templateDir");
			String userTemplate = getParam("userTemplate", instanceId);
			template = new UserTemplate(templateDir.replace('\\', '/') + "/"
					+ userTemplate.replace('\\', '/'), "");

			templates.put(instanceId, template);
		}
		return template;
	}

	static private String getParam(String paramName, String instanceId) {
		Admin admin = new Admin();
		return admin.getComponentParameterValue(instanceId, paramName);
	}

	static Map templates = new HashMap();

	public String getId() {
		return (new Long(id)).toString();
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getTitle() {
		return this.label;
	}

	public String getDate() {
		return this.date;
	}

	public String getIconUrl() {
		return "whitePagesSmall.gif";
	}

	public String getCreatorId() {
		return this.creatorId;
	}

	public String getSilverCreationDate() {
		return this.date;
	}

	public String getDescription(String language) {
		return getDescription();
	}

	public String getName(String language) {
		return getName();
	}

	public Iterator getLanguages() {
		return null;
	}

	public int compareTo(Object other) {
		if (other instanceof CardHeader) {
			CardHeader otherCard = (CardHeader) other;
			return this.label.compareTo(otherCard.label);
		}
		else
			return 1;
	}
}