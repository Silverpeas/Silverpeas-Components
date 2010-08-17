/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.control;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.classifieds.control.ejb.ClassifiedsBm;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBmHome;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledDeleteClassifieds implements SchedulerEventHandler {

  public static final String CLASSIFIEDSENGINE_JOB_NAME = "ClassifiedsEngineJobDelete";
  private ResourceLocator resources =
      new ResourceLocator("com.silverpeas.classifieds.settings.classifiedsSettings", "");

  public void initialize() {
    SilverTrace.error("classifieds", "ScheduledDeleteClassifieds.initialize()", "", "ENTREE");
    try {
      String cron = resources.getString("cronScheduledDeleteClassifieds");
      SimpleScheduler.removeJob(this, CLASSIFIEDSENGINE_JOB_NAME);
      SimpleScheduler.getJob(this, CLASSIFIEDSENGINE_JOB_NAME, cron, this,
          "doScheduledDeleteClassifieds");
    } catch (Exception e) {
      SilverTrace.error("classifieds", "ScheduledDeleteClassifieds.initialize()",
          "classifieds.EX_CANT_INIT_SCHEDULED_DELETE_CLASSIFIEDS", e);
    }
  }

  @Override
  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("classifieds", "ScheduledDeleteClassifieds.handleSchedulerEvent",
            "The job '" + aEvent.getJob().getJobName() + "' was not successfull");
        break;
      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("classifieds", "ScheduledDeleteClassifieds.handleSchedulerEvent",
            "The job '" + aEvent.getJob().getJobName() + "' was successfull");
        break;
      default:
        SilverTrace.error("classifieds", "ScheduledDeleteClassifieds.handleSchedulerEvent",
            "Illegal event type");
        break;
    }
  }

  public void doScheduledDeleteClassifieds(Date date) {
    SilverTrace.info("classifieds", "ScheduledDeleteClassifieds.doScheduledDeleteClassifieds()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      // recherche du nombre de jours avant suppression
      int nbDays = Integer.parseInt(resources.getString("nbDaysForDeleteClassifieds"));
      SilverTrace.info("classifieds", "ScheduledDeleteClassifieds.doScheduledDeleteClassifieds()",
          "root.MSG_GEN_PARAM_VALUE", "nbDays = " + nbDays);

      // rechercher toutes les petites annonces arrivant à échéance
      Collection<ClassifiedDetail> classifieds =
          getClassifiedsBm().getAllClassifiedsToDelete(nbDays);
      SilverTrace.info("classifieds", "ScheduledDeleteClassifieds.doScheduledDeleteClassifieds()",
          "root.MSG_GEN_PARAM_VALUE", "Petites annonces = " + classifieds.toString());

      if (classifieds != null) {
        Iterator<ClassifiedDetail> it = classifieds.iterator();
        while (it.hasNext()) {
          // pour chaque petite annonce, la supprimer
          ClassifiedDetail classified = (ClassifiedDetail) it.next();
          int classifiedId = classified.getClassifiedId();
          getClassifiedsBm().deleteClassified(Integer.toString(classifiedId));
        }
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(
          "ScheduledDeleteClassifieds.doScheduledDeleteClassifieds()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    SilverTrace.info("classifieds", "ScheduledDeleteClassifieds.doScheduledDeleteClassifieds()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private ClassifiedsBm getClassifiedsBm() {
    ClassifiedsBm classifiedsBm = null;
    try {
      ClassifiedsBmHome classifiedsBmHome =
          (ClassifiedsBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.CLASSIFIEDSBM_EJBHOME,
          ClassifiedsBmHome.class);
      classifiedsBm = classifiedsBmHome.create();
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getClassifiedsBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifiedsBm;
  }
}
