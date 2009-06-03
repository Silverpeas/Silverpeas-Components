package com.silverpeas.questionReply.control;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * The questionReply implementation of ContentInterface.
 */
public class QuestionReplyContentManager implements ContentInterface
{
   /** Find all the SilverContent with the given SilverContentId */
   public List getSilverContentById(List   ids, String peasId, String userId, List   userRoles)
   {
	   return getHeaders(makeIdArray(ids), peasId);
   }

	private ArrayList makeIdArray(List idList)
	{
		ArrayList ids = new ArrayList(idList.size());
		Iterator iter = idList.iterator();

		int contentId = 0;
		String id = null;
		while (iter.hasNext())
		{
			contentId = ((Integer) iter.next()).intValue();
			try
			{
			    id = getContentManager().getInternalContentId(contentId);
				ids.add(id);
			}
			catch (ClassCastException ignored)
			{
			   // ignore unknown item
			}
			catch (ContentManagerException ignored)
			{
			   // ignore unknown item
			}
		}
		return ids;
	}

	private List getHeaders(ArrayList ids, String instanceId)
	{
		List headers = new ArrayList();
		Question question ;
		try
		{
			ArrayList questions = (ArrayList) QuestionManager.getInstance().getQuestionsByIds((ArrayList) ids);
			for (int i=0 ; i<questions.size() ; i++)
			{
				question = (Question) questions.get(i);
				headers.add(new QuestionHeader(new Long(question.getPK().getId()).longValue(), question, instanceId, question.getCreationDate(), question.getCreatorId()));
			}
		}
		catch (QuestionReplyException e)
		{
		   // skip unknown and ill formed id.
		}
		return headers;
	}

	public int getSilverObjectId(String id, String peasId) throws QuestionReplyException {
		SilverTrace.info("questionReply","QuestionReplyContentManager.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD", "id = "+id);
		try {
			return getContentManager().getSilverContentId(id, peasId);
		} catch (Exception e) {
			throw new QuestionReplyException("QuestionReplyContentManager.getSilverObjectId()",SilverpeasException.ERROR,"questionReply.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
		}
	}

	/**
	* add a new content. It is registered to contentManager service
	* @param con a Connection
	* @param pubDetail the content to register
	* @param userId the creator of the content
	* @return the unique silverObjectId which identified the new content
	*/
   public int createSilverContent(Connection con, Question question) throws ContentManagerException
   {
	  SilverContentVisibility scv = new SilverContentVisibility(isVisible(question));
	  SilverTrace.info("questionReply","QuestionReplyContentManager.createSilverContent()", "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
	  return getContentManager().addSilverContent(con, question.getPK().getId(), question.getInstanceId(), question.getCreatorId(), scv);
   }

   /**
	* update the visibility attributes of the content. Here, the type of content is a PublicationDetail
	* @param pubDetail the content
	* @param silverObjectId the unique identifier of the content
	*/
   public void updateSilverContentVisibility(Question question) throws ContentManagerException
   {
	  int silverContentId = getContentManager().getSilverContentId(question.getPK().getId(), question.getPK().getComponentName());
	  SilverContentVisibility scv = new SilverContentVisibility(isVisible(question));
	  SilverTrace.info("questionReply","QuestionReplyContentManager.updateSilverContentVisibility()", "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "+scv.toString());
	  getContentManager().updateSilverContentVisibilityAttributes(scv, question.getPK().getComponentName(), silverContentId);
	  ClassifyEngine.clearCache();
   }

   /**
	* delete a content. It is registered to contentManager service
	* @param con a Connection
	* @param pubPK the identifiant of the content to unregister
	*/
   public void deleteSilverContent(Connection con, IdPK pk) throws ContentManagerException
   {
	  int contentId = getContentManager().getSilverContentId(pk.getId(), pk.getComponentName());
	  SilverTrace.info("questionReply","QuestionReplyContentManager.deleteSilverContent()", "root.MSG_GEN_ENTER_METHOD", "id = "+pk.getId()+", contentId = "+contentId);
	  getContentManager().removeSilverContent(con, contentId, pk.getComponentName());
   }

   private boolean isVisible(Question question) 
   {	
		return (question.getPublicReplyNumber() != 0);
   }


	private ContentManager getContentManager()
	{
	   if (contentManager == null)
		{
		   try
			{
		      contentManager = new ContentManager();
			}
			catch (Exception e)
			{
			    SilverTrace.fatal("questionReply", "QuestionReplyContentManager", "root.EX_UNKNOWN_CONTENT_MANAGER", e);
			}
		}
		return contentManager;
	}

	private ContentManager contentManager = null;
}