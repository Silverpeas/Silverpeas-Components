package com.stratelia.webactiv.forums;

import java.util.Vector;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.sessionController.ForumsSessionController;

public class ForumsIndexer implements ComponentIndexerInterface {
     
    private ForumsSessionController fsc = null;
    
    public void index(MainSessionController mainSessionCtrl, ComponentContext context)
    	throws Exception
    {
        fsc = new ForumsSessionController(mainSessionCtrl,context);
		indexForum(0);
    }

	private void indexForum(int forumId)
		throws Exception
	{
		int[] sonIds = fsc.getForumSonsIds(forumId);
		for (int i = 0; i < sonIds.length; i++)
		{
			indexForum(sonIds[i]);
		}
		if (forumId != 0)
		{
			fsc.indexForum(forumId);
		}

		Message[] messages = fsc.getMessagesList(forumId);
		for (int i = 0; i < messages.length; i++)
		{
			indexMessageNoRecursive(messages[i].getId());
		}
	}

	private void indexMessageNoRecursive(int messageId)
		throws Exception
	{
		fsc.indexMessage(messageId);
	}
	
}