/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.yellowpages.control;

import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DisplayContactsHelper {

  private DisplayContactsHelper() {
    throw new IllegalAccessError("Utility class");
  }

  public static void displayContactsAdmin(String contactCard,
      YellowpagesSessionController yellowpagesScc, String profile, Collection<UserContact> contacts,
      String contactDeleteIcon, GraphicElementFactory gef,
      ServletRequest request, HttpSession session, MultiSilverpeasBundle resources, JspWriter out)
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
      ligne1.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(contact.getLastName()),
          "javascript:onClick=contactGoTo('" + contact.getPK().getId() + "')");
      ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getFirstName()));
      ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getEmail()));
      if (resources.getSetting("columns").contains("phone")) {
        ArrayCellText phoneCell =
            ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getPhone()));
        phoneCell.setNoWrap(true);
      }
      if (resources.getSetting("columns").contains("fax")) {
        ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getFax()));
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
      MultiSilverpeasBundle resources, JspWriter out) throws IOException {

    ArrayPane arrayPane;
    String nameArrayPane = "tableau1";
    if (id != null) {
      arrayPane = gef.getArrayPane(nameArrayPane, "GoTo?Id=" + id, request, session);
      arrayPane.setVisibleLineNumber(yellowpagesScc.getNbContactPerPage());
    } else {
      arrayPane = gef.getArrayPane(nameArrayPane, "PrintList", request, session);
      arrayPane.setVisibleLineNumber(-1);
    }
    arrayPane.setExportData(true);
    arrayPane.setExportDataURL("javascript:exportCSV()");

    // recherche des colonnes a afficher
    List<String> arrayHeaders = yellowpagesScc.getArrayHeaders();
    List<String> properties = yellowpagesScc.getProperties();
    Map<String, Function<ContactDetail, String>> contactTextFunctions = new HashMap<>();
    int indexColumn = 0;
    for (String nameHeader : arrayHeaders) {
      arrayPane.addArrayColumn(nameHeader);
      String nameColumn = properties.get(indexColumn);
      if (nameColumn.startsWith("domain.")) {
        final String property = nameColumn.substring(7);
        contactTextFunctions.put(nameColumn, contactDetail -> {
          String value = "";
          UserFull userFull = contactDetail.getUserFull();
          if (userFull != null){
            value = userFull.getValue(property);
            if (StringUtil.isDefined(value) &&
                userFull.getPropertyType(property).equals(DomainProperty.PROPERTY_TYPE_USERID)) {
              UserDetail anotherUser = UserDetail.getById(value);
              if (anotherUser != null) {
                value = anotherUser.getLastName() + " " + anotherUser.getFirstName();
              } else {
                value = "";
              }
            }
          }
          return value;
        });
      } else if ("lastname".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getLastName);
      } else if ("firstname".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getFirstName);
      } else if ("email".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getEmail);
      } else if ("phone".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getPhone);
      } else if ("fax".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getFax);
      }
      indexColumn++;
    }

    final String userIcon = resources.getIcon("yellowpages.user");
    final String contactIcon = resources.getIcon("yellowpages.contact");
    for (ContactFatherDetail contactFather : contacts) {
      ContactDetail contact = contactFather.getContactDetail();
      String nodeName = contactFather.getNodeName();
      String fatherId = contactFather.getNodeId();
      if ("0".equals(fatherId)) {
        nodeName = componentLabel;
      } else if ("1".equals(fatherId)) {
        continue;
      }

      // remplissage des lignes
      ArrayLine ligne = arrayPane.addArrayLine();
      String icon;
      String link;
      if ("fromGroup".equals(contact.getPK().getId())) {
        icon = userIcon;
        link = "javaScript:goToUser('" + contact.getUserId() + "');";
      } else {
        icon = contactIcon;
        link = "javascript:onClick=contactGoToUserInTopic('" + contact.getPK().getId() + "','" +
            fatherId + "')";
      }
      IconPane iconPane = gef.getIconPane();
      Icon carte = iconPane.addIcon();
      carte.setProperties(icon, "", link);

      for (String nameColumn : properties) {
        if ("icon".equals(nameColumn)) {
          ligne.addArrayCellIconPane(iconPane);
        } else if ("topic".equals(nameColumn)) {
          ligne.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(nodeName));
        } else {
          Function<ContactDetail, String> contactDetailStringFunction =
              contactTextFunctions.get(nameColumn);
          if (contactDetailStringFunction != null) {
            ligne.addArrayCellText(contact, contactDetailStringFunction);
          } else {
            ligne.addArrayEmptyCell();
          }
        }
      }
    }
    out.println(arrayPane.print());
  }
}
