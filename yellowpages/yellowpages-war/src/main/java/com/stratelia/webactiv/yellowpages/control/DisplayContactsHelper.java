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
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
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
package com.stratelia.webactiv.yellowpages.control;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.silverpeas.util.EncodeHelper;
import org.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.contact.model.ContactDetail;
import com.stratelia.webactiv.contact.model.ContactFatherDetail;
import org.silverpeas.util.viewGenerator.html.GraphicElementFactory;
import org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellText;
import org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayColumn;
import org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayLine;
import org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayPane;
import org.silverpeas.util.viewGenerator.html.iconPanes.IconPane;
import org.silverpeas.util.viewGenerator.html.icons.Icon;
import com.stratelia.webactiv.yellowpages.model.UserContact;

public class DisplayContactsHelper {

  public static void displayContactsAdmin(String contactCard,
      YellowpagesSessionController yellowpagesScc, String profile, Collection<UserContact> contacts,
      boolean subtopicsExist, String contactDeleteIcon, GraphicElementFactory gef,
      ServletRequest request, HttpSession session, ResourcesWrapper resources, JspWriter out)
      throws IOException {
    int indexLastNameColumn = 1;

    ArrayPane arrayPane = gef.getArrayPane("tableau1", "topicManager.jsp", request, session);
    if (!"no".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
      ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
      arrayColumn0.setSortable(false);
      indexLastNameColumn = 2;
    }
    arrayPane.addArrayColumn(resources.getString("GML.name"));
    arrayPane.addArrayColumn(resources.getString("GML.surname"));
    arrayPane.addArrayColumn(resources.getString("GML.eMail"));
    if (resources.getSetting("columns").contains("phone")) {
      ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.phoneNumber"));
      arrayColumn4.setSortable(false);
    }
    if (resources.getSetting("columns").contains("fax")) {
      ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("GML.faxNumber"));
      arrayColumn5.setSortable(false);
    }
    ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resources.getString("Operations"));
    arrayColumn6.setSortable(false);

    for (UserContact userContact : contacts) {
      ContactDetail contact = userContact.getContact();
      ArrayLine ligne1 = arrayPane.addArrayLine();
      if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
        IconPane iconPane1 = gef.getIconPane();
        Icon carte = iconPane1.addIcon();
        carte.setProperties(contactCard, "",
            "javascript:onClick=contactGoTo('" + contact.getPK().getId() + "')");
        ligne1.addArrayCellIconPane(iconPane1);
      }
      ligne1.addArrayCellLink(EncodeHelper.javaStringToHtmlString(contact.getLastName()),
          "javascript:onClick=contactGoTo('" + contact.getPK().getId() + "')");
      ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(contact.getFirstName()));
      ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(contact.getEmail()));
      if (resources.getSetting("columns").contains("phone")) {
        ArrayCellText phoneCell =
            ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(contact.getPhone()));
        phoneCell.setNoWrap(true);
      }
      if (resources.getSetting("columns").contains("fax")) {
        ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(contact.getFax()));
      }

      UserDetail ownerDetail = userContact.getOwner();
      if ((profile.equals("admin")) ||
          ((ownerDetail != null) && (yellowpagesScc.getUserId().equals(ownerDetail.getId())))) {
        IconPane iconPane = gef.getIconPane();
        Icon deleteIcon = iconPane.addIcon();
        deleteIcon.setProperties(contactDeleteIcon, resources.getString("ContactSupprimer"),
            "javaScript:contactDeleteConfirm('" + contact.getPK().getId() + "')");
        iconPane.setSpacing("30px");
        ligne1.addArrayCellIconPane(iconPane);
      }
    }
    if (arrayPane.getColumnToSort() == 0) {
      arrayPane.setColumnToSort(indexLastNameColumn);
    }
    out.println(arrayPane.print());

  }

  public static void displayContactsUser(YellowpagesSessionController yellowpagesScc,
      Collection<ContactFatherDetail> contacts, String id, String componentLabel,
      GraphicElementFactory gef, ServletRequest request, HttpSession session,
      ResourcesWrapper resources, JspWriter out) throws IOException {

    ArrayPane arrayPane;
    if (id != null) {
      arrayPane = gef.getArrayPane("tableau1", "GoTo?Id=" + id, request, session);
      arrayPane.setVisibleLineNumber(yellowpagesScc.getNbContactPerPage());
    } else {
      arrayPane = gef.getArrayPane("tableau1", "PrintList", request, session);
      arrayPane.setVisibleLineNumber(-1);
    }

    // recherche des colonnes a afficher
    List<String> arrayHeaders = yellowpagesScc.getArrayHeaders();
    for (String nameHeader : arrayHeaders) {
      arrayPane.addArrayColumn(nameHeader);
    }

    for (ContactFatherDetail contactFather : contacts) {
      ContactDetail contact = contactFather.getContactDetail();
      UserFull userFull = contact.getUserFull();
      String nodeName = contactFather.getNodeName();
      String fatherId = contactFather.getNodeId();
      if ("0".equals(fatherId)) {
        nodeName = EncodeHelper.javaStringToHtmlString(componentLabel);
      } else if ("1".equals(fatherId)) {
        continue;
      }

      // remplissage des lignes
      ArrayLine ligne = arrayPane.addArrayLine();
      String icon;
      String link;
      if ("fromGroup".equals(contact.getPK().getId())) {
        icon = resources.getIcon("yellowpages.user");
        link = "javaScript:goToUser('" + contact.getUserId() + "');";
      } else {
        icon = resources.getIcon("yellowpages.contact");
        link = "javascript:onClick=contactGoToUserInTopic('" + contact.getPK().getId() + "','" +
            fatherId + "')";
      }
      IconPane iconPane = gef.getIconPane();
      Icon carte = iconPane.addIcon();
      carte.setProperties(icon, "", link);

      List<String> properties = yellowpagesScc.getProperties();
      for (String nameColumn : properties) {
        if (nameColumn.startsWith("domain.")) {
          String property = nameColumn.substring(7);
          // rechercher la valeur dans UserFull
          if (userFull != null) {
            ligne
                .addArrayCellText(EncodeHelper.javaStringToHtmlString(userFull.getValue(property)));
          }
        } else {
          String value;
          // recherche la valeur dans ContactDetail
          if (nameColumn.equals("icon")) {
            ligne.addArrayCellIconPane(iconPane);
          } else {
            if ("lastname".equals(nameColumn)) {
              value = contact.getLastName();
            } else if ("firstname".equals(nameColumn)) {
              value = contact.getFirstName();
            } else if ("email".equals(nameColumn)) {
              value = contact.getEmail();
            } else if ("phone".equals(nameColumn)) {
              value = contact.getPhone();
            } else if ("fax".equals(nameColumn)) {
              value = contact.getFax();
            } else if ("topic".equals(nameColumn)) {
              value = nodeName;
            } else {
              value = "";
            }
            ligne.addArrayCellText(EncodeHelper.javaStringToHtmlString(value));
          }
        }
      }
    }
    out.println(arrayPane.print());
  }
}
