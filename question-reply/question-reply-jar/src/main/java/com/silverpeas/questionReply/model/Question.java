package com.silverpeas.questionReply.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.DateUtil;

public class Question extends SilverpeasBean {
	private String title;
	private String content;
	private String creatorId;
	private String creationDate;
	private int status = 0;
	private int publicReplyNumber = 0;
	private int privateReplyNumber = 0;
	private int replyNumber = 0;
	private String instanceId;
	private String categoryId;
	private Collection replies = new ArrayList();
	private Collection recipients = new ArrayList();
	
	
	
	private static OrganizationController organizationController =
		new OrganizationController();

	public Question() {
	}

	public Question(String creatorId, String instanceId) {
		setCreatorId(creatorId);
		setInstanceId(instanceId);
		setCreationDate();
	}
	public String getTitle() {
		return title;
	}
	public String getContent() {
		return content;
	}
	public String getCreatorId() {
		return creatorId;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public int getStatus() {
		return status;
	}
	public int getPublicReplyNumber() {
		return publicReplyNumber;
	}
	public int getPrivateReplyNumber() {
		return privateReplyNumber;
	}
	public int getReplyNumber() {
		return replyNumber;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public Collection readReplies() {
		return replies;
	}
	public Collection readRecipients() {
		return recipients;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	public void setCreationDate() {
		this.creationDate = DateUtil.date2SQLDate(new Date());
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setPublicReplyNumber(int publicReplyNumber) {
		this.publicReplyNumber = publicReplyNumber;
	}
	public void setPrivateReplyNumber(int privateReplyNumber) {
		this.privateReplyNumber = privateReplyNumber;
	}
	public void setReplyNumber(int replyNumber) {
		this.replyNumber = replyNumber;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public void writeReplies(Collection replies) {
		this.replies = replies;
	}
	public void writeRecipients(Collection recipients) {
		this.recipients = recipients;
	}
	public void incPublicReplyNumber(int nb) {
		this.publicReplyNumber = this.publicReplyNumber + nb;
	}
	public void incPrivateReplyNumber(int nb) {
		this.privateReplyNumber = this.privateReplyNumber + nb;
	}
	public void decPublicReplyNumber(int nb) {
		this.publicReplyNumber = this.publicReplyNumber - nb;
	}
	public void decPrivateReplyNumber(int nb) {
		this.privateReplyNumber = this.privateReplyNumber - nb;
	}
	public void incReplyNumber(int nb) {
		this.replyNumber = this.replyNumber + nb;
	}
	public void decReplyNumber(int nb) {
		this.replyNumber = this.replyNumber - nb;
	}
	
	public String _getPermalink()
	{
		if (URLManager.displayUniversalLinks())
			return URLManager.getApplicationURL() + "/Question/" + getPK().getId();

		return null;
	}
	
	public String _getURL()
	{
		return "searchResult?Type=Question&Id=" + getPK().getId();
	} 

	public String readCreatorName() {
		String creatorName = null;
		UserDetail userDetail =
			organizationController.getUserDetail(
				new Integer(getCreatorId()).toString());
		if (userDetail != null)
			creatorName = userDetail.getDisplayedName();
		return creatorName;
	}

	public String _getTableName() {
		return "SC_QuestionReply_Question";
	}

	public int _getConnectionType() {
		return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
	}

	public String getCategoryId()
	{
		return categoryId;
	}

	public void setCategoryId(String categoryId)
	{
		this.categoryId = categoryId;
	}

}
