/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.quizz;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;

/**
 * Class declaration
 *
 *
 * @author
 */
public class QuizzStatistics implements ComponentStatisticsInterface
{

    private QuestionContainerBm questionContainerBm = null;

    public Collection getVolume(String spaceId, String componentId) throws Exception
    {
        ArrayList  myArrayList = new ArrayList();

        Collection c = getQuizz(spaceId, componentId);
        Iterator   iter = c.iterator();
        while (iter.hasNext())
        {
            QuestionContainerHeader qcHeader = (QuestionContainerHeader) iter.next();

            UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
            myCouple.setUserId(qcHeader.getCreatorId());
            myCouple.setCountVolume(1);
            myArrayList.add(myCouple);
        }

        return myArrayList;
    }

    private QuestionContainerBm getQuestionContainerBm()
    {
        if (questionContainerBm == null)
        {
            try
            {
                QuestionContainerBmHome questionContainerBmHome = (QuestionContainerBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.QUESTIONCONTAINERBM_EJBHOME, QuestionContainerBmHome.class);
                questionContainerBm = questionContainerBmHome.create();
            }
            catch (Exception e)
            {
                throw new EJBException(e);
            }
        }
        return questionContainerBm;
    }


    public Collection getQuizz(String spaceId, String componentId) throws RemoteException
    {
        Collection result = getQuestionContainerBm().getNotClosedQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
        return result;
    }

}
