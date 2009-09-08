/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.survey;

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
public class SurveyStatistics implements ComponentStatisticsInterface
{

  private QuestionContainerBm questionContainerBm = null;

    /**
     * Method declaration
     *
     *
     * @param spaceId
     * @param componentId
     *
     * @return
     *
     * @see
     */
    public Collection getVolume(String spaceId, String componentId) throws Exception
    {
        ArrayList  myArrayList = new ArrayList();

        Collection c = getOpenedSurveys(spaceId, componentId);
        addSurveys(c, myArrayList);
        c = getClosedSurveys(spaceId, componentId);
        addSurveys(c, myArrayList);
        c = getInWaitSurveys(spaceId, componentId);
        addSurveys(c, myArrayList);

        return myArrayList;
    }

    private void addSurveys(Collection c, ArrayList al)
    {
        Iterator   iter = c.iterator();
        while (iter.hasNext())
        {
            QuestionContainerHeader surveyHeader = (QuestionContainerHeader) iter.next();

            UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
            myCouple.setUserId(surveyHeader.getCreatorId());
            myCouple.setCountVolume(1);
            al.add(myCouple);
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
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

    /**
     * Method declaration
     *
     *
     * @param spaceId
     * @param componentId
     *
     * @return
     *
     * @throws RemoteException
     *
     * @see
     */

    public Collection getOpenedSurveys(String spaceId, String componentId) throws RemoteException
    {
        Collection result = getQuestionContainerBm().getOpenedQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
        return result;
    }

    public Collection getClosedSurveys(String spaceId, String componentId) throws RemoteException
    {
        Collection result = getQuestionContainerBm().getClosedQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
        return result;
    }

    public Collection getInWaitSurveys(String spaceId, String componentId) throws RemoteException
    {
        Collection result = getQuestionContainerBm().getInWaitQuestionContainers(new QuestionContainerPK(null, spaceId, componentId));
        return result;
    }
}
