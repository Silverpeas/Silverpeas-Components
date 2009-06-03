/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.questionReply;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.questionReply.control.QuestionManager;
import com.silverpeas.questionReply.model.Question;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

/*
 * CVS Informations
 *
 * $Id: QuestionReplyStatistics.java,v 1.2 2004/06/22 16:30:33 neysseri Exp $
 *
 * $Log: QuestionReplyStatistics.java,v $
 * Revision 1.2  2004/06/22 16:30:33  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.1.1.1  2002/08/06 14:48:01  nchaix
 * no message
 *
 * Revision 1.2  2002/05/16 10:14:22  mguillem
 * merge branch V2001_Statistics01
 *
 * Revision 1.1.2.2  2002/04/26 11:52:46  pbialevich
 * Statistic of Belorussion Guy
 *
 * Revision 1.1.2.1  2002/04/26 11:51:31  pbialevich
 * Statistic of Belorussion Guy
 *
 * Revision 1.2  2002/04/17 17:22:32  mguillem
 * alimentation des stats de volume
 *
 * Revision 1.1  2002/04/05 16:58:24  mguillem
 * alimentation des stats de volume
 *
 */

/**
 * Class declaration
 *
 *
 * @author
 */
public class QuestionReplyStatistics implements ComponentStatisticsInterface
{

    public Collection getVolume(String spaceId, String componentId) throws Exception
    {
        ArrayList  myArrayList = new ArrayList();
        Collection c = getQuestionReplys(spaceId, componentId);
        Iterator   iter = c.iterator();
        while (iter.hasNext())
        {
            Question       detail = (Question) iter.next();

            UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

            myCouple.setUserId(detail.getCreatorId());
            myCouple.setCountVolume(1);
            myArrayList.add(myCouple);
        }

        return myArrayList;
    }

    public Collection getQuestionReplys(String spaceId, String componentId) throws QuestionReplyException
    {
        Collection result = QuestionManager.getInstance().getQuestions(componentId);
        return result;
    }

}
