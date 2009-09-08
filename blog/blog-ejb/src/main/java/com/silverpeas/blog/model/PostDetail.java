package com.silverpeas.blog.model;

import java.io.Serializable;
import java.util.Date;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class PostDetail implements Serializable
{
	private String content;
	private PublicationDetail publication;
	private Category category;
	private String categoryId;
	private int nbComments;
	private String creatorName;
	private Date dateEvent;
	
	
	public PostDetail(PublicationDetail publication, String categoryId)
	{
		setPublication(publication);
		setCategoryId(categoryId);
	}
	
	public PostDetail(PublicationDetail publication, String categoryId, Date dateEvent)
	{
		setPublication(publication);
		setCategoryId(categoryId);
		setDateEvent(dateEvent);
	}
	
	public PostDetail(PublicationDetail publication, Category category, int nbComments) 
	{
		setPublication(publication);
		setCategory(category);
		setNbComments(nbComments);
	}
	
	public PostDetail(PublicationDetail publication, Category category, int nbComments, Date dateEvent) 
	{
		setPublication(publication);
		setCategory(category);
		setNbComments(nbComments);
		setDateEvent(dateEvent);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public int getNbComments() {
		return nbComments;
	}

	public void setNbComments(int nbComments) {
		this.nbComments = nbComments;
	}

	public PublicationDetail getPublication() {
		return publication;
	}

	public void setPublication(PublicationDetail publication) {
		this.publication = publication;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	
	public Date getDateEvent() {
		return dateEvent;
	}

	public void setDateEvent(Date dateEvent) {
		this.dateEvent = dateEvent;
	}
	
	public String getPermalink()
	{
		if (URLManager.displayUniversalLinks())
			return URLManager.getApplicationURL() + "/Post/" + publication.getPK().getId();

		return null;
	}
	
	public String getURL()
	{
		return "searchResult?Type=Publication&PostId=" + getPublication().getId();
	}
}
