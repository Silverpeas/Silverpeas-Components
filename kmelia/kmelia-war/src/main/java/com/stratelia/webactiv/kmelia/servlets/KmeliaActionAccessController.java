/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS", SilverpeasRole.reader) applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia.servlets;

import com.stratelia.webactiv.SilverpeasRole;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ebonnet
 */
public class KmeliaActionAccessController {

  private Map<String, SilverpeasRole> actionRole = new HashMap<String, SilverpeasRole>();

  public KmeliaActionAccessController() {
    actionRole.put("Main", SilverpeasRole.reader);
    actionRole.put("DeletePublication", SilverpeasRole.writer);
    actionRole.put("NewPublication", SilverpeasRole.writer);
    actionRole.put("ToUpdatePublicationHeader", SilverpeasRole.writer);
    actionRole.put("ToPubliContent", SilverpeasRole.writer);
    actionRole.put("AddLinksToPublication", SilverpeasRole.writer);
    actionRole.put("DeleteSeeAlso", SilverpeasRole.writer);



    /*
  } else if (function.startsWith("copy", SilverpeasRole.reader);
  } else if (function.startsWith("cut", SilverpeasRole.reader);
  } else if (function.startsWith("paste", SilverpeasRole.reader);
  } else if (function.startsWith("ToAlertUserAttachment", SilverpeasRole.reader); // utilisation
  de alertUser et
  } else if (function.startsWith("ToAlertUserDocument", SilverpeasRole.reader); // utilisation de
   alertUser et
  } else if (function.startsWith("ToAlertUser", SilverpeasRole.reader); // utilisation de
  alertUser et alertUserPeas
    //} else if (function.startsWith("ViewAttachments", SilverpeasRole.reader);
    //} else if (function.startsWith("ToOrderPublications", SilverpeasRole.reader);
    //} else if (function.startsWith("OrderPublications", SilverpeasRole.reader);
    //} else if (function.startsWith("Wizard", SilverpeasRole.reader);
    //} else if (function.startsWith("UpdateChain", SilverpeasRole.reader);
  */
    /*
    actionRole.put("validateClassification", SilverpeasRole.reader);
    actionRole.put("portlet", SilverpeasRole.reader);
    actionRole.put("FlushTrashCan", SilverpeasRole.reader);
    actionRole.put("GoToDirectory", SilverpeasRole.reader);
    actionRole.put("GoToTopic", SilverpeasRole.reader);
    actionRole.put("GoToCurrentTopic", SilverpeasRole.reader);
    actionRole.put("GoToBasket", SilverpeasRole.reader);
    actionRole.put("ViewPublicationsToValidate", SilverpeasRole.reader);
    actionRole.put("GoBackToResults", SilverpeasRole.reader);
    actionRole.put("searchResult", SilverpeasRole.reader);
    actionRole.put("GoToFilesTab", SilverpeasRole.reader);
    actionRole.put("publicationManager.jsp", SilverpeasRole.reader);
    actionRole.put("ToAddTopic", SilverpeasRole.admin);
    actionRole.put("ToUpdateTopic", SilverpeasRole.admin);
    actionRole.put("AddTopic", SilverpeasRole.reader);
    actionRole.put("UpdateTopic", SilverpeasRole.reader);
    actionRole.put("DeleteTopic", SilverpeasRole.reader);
    actionRole.put("ViewClone", SilverpeasRole.reader);
    actionRole.put("ViewPublication", SilverpeasRole.reader);
    actionRole.put("PreviousPublication", SilverpeasRole.reader);
    actionRole.put("NextPublication", SilverpeasRole.reader);
    actionRole.put("ReadingControl", SilverpeasRole.reader);
    actionRole.put("DeleteClone", SilverpeasRole.reader);
    actionRole.put("ViewValidationSteps", SilverpeasRole.reader);
    actionRole.put("ValidatePublication", SilverpeasRole.reader);
    actionRole.put("ForceValidatePublication", SilverpeasRole.reader);
    actionRole.put("Unvalidate", SilverpeasRole.reader);
    actionRole.put("WantToSuspendPubli", SilverpeasRole.reader);
    actionRole.put("SuspendPublication", SilverpeasRole.reader);
    actionRole.put("DraftIn", SilverpeasRole.reader);
    actionRole.put("DraftOut", SilverpeasRole.reader);
    actionRole.put("ToTopicWysiwyg", SilverpeasRole.reader);
    actionRole.put("FromTopicWysiwyg", SilverpeasRole.reader);
    actionRole.put("ChangeTopicStatus", SilverpeasRole.reader);
    actionRole.put("ViewOnly", SilverpeasRole.reader);
    actionRole.put("SeeAlso", SilverpeasRole.reader);
    actionRole.put("ImportFileUpload", SilverpeasRole.reader);
    actionRole.put("ImportFilesUpload", SilverpeasRole.reader);
    actionRole.put("ExportAttachementsToPDF", SilverpeasRole.reader);
    actionRole.put("ManageSubscriptions", SilverpeasRole.reader);
    actionRole.put("AddPublication", SilverpeasRole.reader);
    actionRole.put("UpdatePublication", SilverpeasRole.reader);
    actionRole.put("SelectValidator", SilverpeasRole.reader);
    actionRole.put("PublicationPaths", SilverpeasRole.reader);
    actionRole.put("SetPath", SilverpeasRole.reader);
    actionRole.put("ShowAliasTree", SilverpeasRole.reader);
    actionRole.put("ExportTopic", SilverpeasRole.reader);
    actionRole.put("ExportPublications", SilverpeasRole.reader);
    actionRole.put("ListModels", SilverpeasRole.reader);
    actionRole.put("ModelUsed", SilverpeasRole.reader);
    actionRole.put("SelectModel", SilverpeasRole.reader);
    actionRole.put("ChangeTemplate", SilverpeasRole.reader);
    actionRole.put("ToWysiwyg", SilverpeasRole.reader);
    actionRole.put("FromWysiwyg", SilverpeasRole.reader);
    actionRole.put("GoToXMLForm", SilverpeasRole.reader);
    actionRole.put("UpdateXMLForm", SilverpeasRole.reader);
    actionRole.put("ToOrderTopics", SilverpeasRole.reader);
    actionRole.put("ViewTopicProfiles", SilverpeasRole.reader);
    actionRole.put("TopicProfileSelection", SilverpeasRole.reader);
    actionRole.put("TopicProfileSetUsersAndGroups", SilverpeasRole.reader);
    actionRole.put("TopicProfileRemove", SilverpeasRole.reader);
    actionRole.put("CloseWindow", SilverpeasRole.reader);
    actionRole.put("SuggestDelegatedNews", SilverpeasRole.reader);
    actionRole.put("KmaxMain", SilverpeasRole.reader);
    actionRole.put("KmaxAxisManager", SilverpeasRole.reader);
    actionRole.put("KmaxAddAxis", SilverpeasRole.reader);
    actionRole.put("KmaxUpdateAxis", SilverpeasRole.reader);
    actionRole.put("KmaxDeleteAxis", SilverpeasRole.reader);
    actionRole.put("KmaxManageAxis", SilverpeasRole.reader);
    actionRole.put("KmaxManagePosition", SilverpeasRole.reader);
    actionRole.put("KmaxAddPosition", SilverpeasRole.reader);
    actionRole.put("KmaxUpdatePosition", SilverpeasRole.reader);
    actionRole.put("KmaxDeletePosition", SilverpeasRole.reader);
    actionRole.put("KmaxViewUnbalanced", SilverpeasRole.reader);
    actionRole.put("KmaxViewBasket", SilverpeasRole.reader);
    actionRole.put("KmaxViewToValidate", SilverpeasRole.reader);
    actionRole.put("KmaxSearch", SilverpeasRole.reader);
    actionRole.put("KmaxSearchResult", SilverpeasRole.reader);
    actionRole.put("KmaxViewCombination", SilverpeasRole.reader);
    actionRole.put("KmaxAddCoordinate", SilverpeasRole.reader);
    actionRole.put("KmaxDeleteCoordinate", SilverpeasRole.reader);
    actionRole.put("KmaxExportComponent", SilverpeasRole.reader);
    actionRole.put("KmaxExportPublications", SilverpeasRole.reader);
    actionRole.put("statistics", SilverpeasRole.reader);
    actionRole.put("statSelectionGroup", SilverpeasRole.reader);
    actionRole.put("SetPublicationValidator", SilverpeasRole.reader);
    */

  }


  /**
   * Check if user role has right access to the given action
   * @param action the checked action
   * @param role the highest user role
   * @return true if given role has right access to the action
   */
  public boolean hasRightAccess(String action, SilverpeasRole role) {
    boolean actionExist = actionRole.containsKey(action);
    if (actionExist && role.isGreaterThanOrEquals(actionRole.get(action))) {
      return true;
    } else if (!actionExist) {
      return true;
    }
    return false;
  }
}
