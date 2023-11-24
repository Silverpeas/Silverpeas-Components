/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.yellowpages.control;

import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
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

  private static final String COLUMNS = "columns";
  private static final String PHONE = "phone";
  private static final String FAX = "fax";

  private DisplayContactsHelper() {
    throw new IllegalAccessError("Utility class");
  }

  public static void displayContactsAdmin(YellowpagesSessionController yellowpagesScc,
      GraphicElementFactory gef,
      ServletRequest request,
      HttpSession session,
      MultiSilverpeasBundle resources,
      JspWriter out) throws IOException {
    String profile = request.getParameter("Profile");
    String id = getTopicId(yellowpagesScc, request);
    Collection<UserContact> contacts = yellowpagesScc.getTopic(id).getContactDetails();
    boolean isBin = id.equals(TopicDetail.BIN_ID);

    ArrayPane arrayPane = gef.getArrayPane("tableau1", "topicManager.jsp", request, session);
    int indexLastNameColumn = addColumnHeaderForContactsAdmin(resources, arrayPane);

    for (UserContact userContact : contacts) {
      ContactDetail contact = userContact.getContact();
      ArrayLine ligne1 = arrayPane.addArrayLine();
      if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
        IconPane iconPane1 = gef.getIconPane();
        Icon carte = iconPane1.addIcon();
        carte.setProperties(resources.getIcon("yellowpages.contact"), "",
            "javascript:onClick=contactGoTo('" + contact.getPK().getId() + "')");
        ligne1.addArrayCellIconPane(iconPane1);
      }
      ligne1.addArrayCellLink(WebEncodeHelper.javaStringToHtmlString(contact.getLastName()),
          "javascript:onClick=contactGoTo('" + contact.getPK().getId() + "')");
      ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getFirstName()));
      ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getEmail()));
      if (resources.getSetting(COLUMNS).contains(PHONE)) {
        ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getPhone()));
      }
      if (resources.getSetting(COLUMNS).contains(FAX)) {
        ligne1.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(contact.getFax()));
      }

      UserDetail ownerDetail = userContact.getOwner();
      if ((profile.equals("admin")) ||
          ((ownerDetail != null) && (yellowpagesScc.getUserId().equals(ownerDetail.getId())))) {
        IconPane iconPane = gef.getIconPane();
        Icon deleteIcon = iconPane.addIcon();

        String contactDeleteIcon = isBin ? resources.getIcon("yellowpages.delete") :
            resources.getIcon("yellowpages.contactDelete");

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

  private static int addColumnHeaderForContactsAdmin(MultiSilverpeasBundle resources,
      ArrayPane arrayPane) {
    int indexLastNameColumn = 1;
    if (!"no".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
      ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
      arrayColumn0.setSortable(false);
      indexLastNameColumn = 2;
    }
    arrayPane.addArrayColumn(resources.getString("GML.name"));
    arrayPane.addArrayColumn(resources.getString("GML.surname"));
    arrayPane.addArrayColumn(resources.getString("yellowpages.column.email"));
    if (resources.getSetting(COLUMNS).contains(PHONE)) {
      ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("yellowpages.column" +
          ".phone"));
      arrayColumn4.setSortable(false);
    }
    if (resources.getSetting(COLUMNS).contains(FAX)) {
      ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("GML.faxNumber"));
      arrayColumn5.setSortable(false);
    }
    ArrayColumn arrayColumn6 = arrayPane.addArrayColumn(resources.getString("Operations"));
    arrayColumn6.setSortable(false);
    return indexLastNameColumn;
  }

  private static String getTopicId(YellowpagesSessionController yellowpagesScc,
      ServletRequest request) {
    String id = request.getParameter("Id");
    TopicDetail currentTopic;
    if (!StringUtil.isDefined(id)) {
      currentTopic = yellowpagesScc.getCurrentTopic();
      if (currentTopic != null) {
        id = currentTopic.getNodePK().getId();
      } else {
        id = TopicDetail.ROOT_ID;
      }
    }
    return id;
  }

  @SuppressWarnings("unchecked")
  public static void displayContactsUser(YellowpagesSessionController yellowpagesScc,
      String id,
      GraphicElementFactory gef, ServletRequest request, HttpSession session,
      MultiSilverpeasBundle resources, JspWriter out) throws IOException {
    String nameArrayPane = "tableau1";
    Collection<ContactFatherDetail> contacts = (Collection<ContactFatherDetail>)
        request.getAttribute("Contacts");

    ArrayPane arrayPane = getArrayPane(yellowpagesScc, id, gef, request, session, nameArrayPane);

    // find the column to display
    List<String> arrayHeaders = yellowpagesScc.getArrayHeaders();
    List<String> properties = yellowpagesScc.getProperties();
    Map<String, Function<ContactDetail, String>> contactTextFunctions = new HashMap<>();
    int indexColumn = 0;
    addColumnHeaderForContactsUser(arrayHeaders, arrayPane, properties, indexColumn,
        contactTextFunctions);

    final String userIcon = resources.getIcon("yellowpages.user");
    final String contactIcon = resources.getIcon("yellowpages.contact");
    for (ContactFatherDetail contactFather : contacts) {
      ContactDetail contact = contactFather.getContactDetail();
      String fatherId = contactFather.getNodeId();
      String nodeName = contactFather.getNodeName();
      if (TopicDetail.ROOT_ID.equals(fatherId)) {
        nodeName = yellowpagesScc.getComponentLabel();
      } else if (TopicDetail.BIN_ID.equals(fatherId)) {
        continue;
      }

      IconPane iconPane = getIconPane(gef, contact, userIcon, contactIcon, fatherId);
      ArrayLine row = arrayPane.addArrayLine();
      for (String nameColumn : properties) {
        addCell(row, nameColumn, iconPane, nodeName, contactTextFunctions, contact);
      }
    }
    out.println(arrayPane.print());
  }

  private static void addCell(ArrayLine row, String nameColumn, IconPane iconPane,
      String nodeName, Map<String, Function<ContactDetail, String>> contactTextFunctions,
      ContactDetail contact) {
    if ("icon".equals(nameColumn)) {
      row.addArrayCellIconPane(iconPane);
    } else if ("topic".equals(nameColumn)) {
      row.addArrayCellText(WebEncodeHelper.javaStringToHtmlString(nodeName));
    } else {
      Function<ContactDetail, String> contactDetailStringFunction =
          contactTextFunctions.get(nameColumn);
      if (contactDetailStringFunction != null) {
        row.addArrayCellText(contact, contactDetailStringFunction);
      } else {
        row.addArrayEmptyCell();
      }
    }
  }

  private static IconPane getIconPane(GraphicElementFactory gef, ContactDetail contact,
      String userIcon, String contactIcon, String fatherId) {
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
    return iconPane;
  }

  private static void addColumnHeaderForContactsUser(List<String> arrayHeaders,
      ArrayPane arrayPane, List<String> properties, int indexColumn, Map<String,
      Function<ContactDetail, String>> contactTextFunctions) {
    for (String nameHeader : arrayHeaders) {
      arrayPane.addArrayColumn(nameHeader);
      String nameColumn = properties.get(indexColumn);
      if (nameColumn.startsWith("domain.")) {
        fillContactTextFunctions(nameColumn, contactTextFunctions);
      } else if ("lastname".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getLastName);
      } else if ("firstname".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getFirstName);
      } else if ("email".equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getEmail);
      } else if (PHONE.equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getPhone);
      } else if (FAX.equals(nameColumn)) {
        contactTextFunctions.put(nameColumn, ContactDetail::getFax);
      }
      indexColumn++;
    }
  }

  private static void fillContactTextFunctions(String nameColumn, Map<String,
      Function<ContactDetail, String>> contactTextFunctions) {
    final String property = nameColumn.substring(7);
    contactTextFunctions.put(nameColumn, contactDetail -> {
      String value = "";
      UserFull userFull = contactDetail.getUserFull();
      if (userFull != null) {
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
  }

  private static ArrayPane getArrayPane(YellowpagesSessionController yellowpagesScc, String id,
      GraphicElementFactory gef, ServletRequest request, HttpSession session,
      String nameArrayPane) {
    ArrayPane arrayPane;
    if (id != null) {
      arrayPane = gef.getArrayPane(nameArrayPane, "GoTo?Id=" + id, request, session);
      arrayPane.setVisibleLineNumber(yellowpagesScc.getNbContactPerPage());
    } else {
      arrayPane = gef.getArrayPane(nameArrayPane, "PrintList", request, session);
      arrayPane.setVisibleLineNumber(-1);
    }
    arrayPane.setExportData(true);
    arrayPane.setExportDataURL("javascript:exportCSV()");
    return arrayPane;
  }
}
