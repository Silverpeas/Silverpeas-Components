package com.stratelia.webactiv.forums.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.stratelia.webactiv.forums.messageEntity.ejb.MessagePK;

public class Message
	implements Serializable
{
	
	private int id;
	private String title;
	private String author;
	private Date date;
	private int forumId;
	private int parentId;
	private String text;
	private String instanceId;
	private MessagePK pk;
	
	public Message(int id, String title, String author, Date date, int forumId, int parentId)
	{
		this.id = id;
		this.title = title;
		this.author = author;
		this.date = date;
		this.forumId = forumId;
		this.parentId = parentId;
	}
	
	public Message(int id, String title, String author, Date date, int forumId, int parentId,
		String instanceId)
	{
		this(id, title, author, date, forumId, parentId);
		this.instanceId = instanceId;
		
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public int getForumId()
	{
		return forumId;
	}

	public void setForumId(int forumId)
	{
		this.forumId = forumId;
	}

	public int getParentId()
	{
		return parentId;
	}

	public void setParentId(int parentId)
	{
		this.parentId = parentId;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
	
	public String getInstanceId()
	{
		return instanceId;
	}

	public void setInstanceId(String instanceId)
	{
		this.instanceId = instanceId;
	}

	public MessagePK getPk()
	{
		return pk;
	}

	public void setPk(MessagePK pk)
	{
		this.pk = pk;
	}

}