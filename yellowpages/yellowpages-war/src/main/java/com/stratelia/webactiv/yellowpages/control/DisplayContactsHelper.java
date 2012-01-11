/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.yellowpages.control;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.yellowpages.model.Company;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine;
import com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane;
import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane;
import com.stratelia.webactiv.util.viewGenerator.html.icons.Icon;
import com.stratelia.webactiv.yellowpages.model.UserContact;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DisplayContactsHelper {

    public static void displayContactsAdmin(String contactCard,
                                            YellowpagesSessionController yellowpagesScc, String profile, Collection contacts,
                                            boolean subtopicsExist, String contactDeleteIcon, GraphicElementFactory gef,
                                            ServletRequest request, HttpSession session, ResourcesWrapper resources, JspWriter out) throws
            IOException {
        int indexLastNameColumn = 1;

        ArrayPane arrayPane = gef.getArrayPane("tableau1", "topicManager.jsp", request, session);
        if (!"no".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
            ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
            arrayColumn0.setSortable(false);
            indexLastNameColumn = 2;
        }
        ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.name"));
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.surname"));
        ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("GML.eMail"));
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

        Iterator iterator = contacts.iterator();
        while (iterator.hasNext()) {
            UserContact userContact = (UserContact) iterator.next();
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
                ArrayCellText phoneCell = ligne1.addArrayCellText(
                        EncodeHelper.javaStringToHtmlString(contact.getPhone()));
                phoneCell.setNoWrap(true);
            }
            if (resources.getSetting("columns").contains("fax")) {
                ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(contact.getFax()));
            }

            UserDetail ownerDetail = userContact.getOwner();
            if ((profile.equals("admin")) || ((ownerDetail != null) && (yellowpagesScc.getUserId().equals(
                    ownerDetail.getId())))) {
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

    public static void displayContactsCompanyAdmin(String contactCard,
                                                   YellowpagesSessionController yellowpagesScc, String profile, Collection companies,
                                                   boolean subtopicsExist, String contactDeleteIcon, GraphicElementFactory gef,
                                                   ServletRequest request, HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException {
        int indexLastNameColumn = 1;

        ArrayPane arrayPane = gef.getArrayPane("tableau2", "topicManager.jsp", request, session);
        if (!"no".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
            ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
            arrayColumn0.setSortable(false);
            indexLastNameColumn = 2;
        }
        ArrayColumn arrayColumn1 = arrayPane.addArrayColumn(resources.getString("GML.name"));
        arrayColumn1.setSortable(true);
        ArrayColumn arrayColumn2 = arrayPane.addArrayColumn(resources.getString("GML.eMail"));
        arrayColumn2.setSortable(true);
        ArrayColumn arrayColumn3 = arrayPane.addArrayColumn(resources.getString("GML.phoneNumber"));
        ArrayColumn arrayColumn4 = arrayPane.addArrayColumn(resources.getString("GML.faxNumber"));
        ArrayColumn arrayColumn5 = arrayPane.addArrayColumn(resources.getString("Operations"));
        arrayColumn5.setSortable(false);

        Iterator iterator = companies.iterator();
        while (iterator.hasNext()) {
            Company company = (Company) iterator.next();
            ArrayLine ligne1 = arrayPane.addArrayLine();
            if (!"No".equalsIgnoreCase(resources.getSetting("showContactIcon"))) {
                IconPane iconPane1 = gef.getIconPane();
                Icon carte = iconPane1.addIcon();
                carte.setProperties(contactCard, "", "javascript:onClick=contactCompanyGoTo('" + company.getCompanyId() + "')");
                ligne1.addArrayCellIconPane(iconPane1);
            }
            ligne1.addArrayCellLink(EncodeHelper.javaStringToHtmlString(company.getName()), "javascript:onClick=contactCompanyGoTo('" + company.getCompanyId() + "')");
            ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(company.getEmail()));
            ArrayCellText phoneCell = ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(company.getPhone()));
            phoneCell.setNoWrap(true);
            ArrayCellText faxCell = ligne1.addArrayCellText(EncodeHelper.javaStringToHtmlString(company.getFax()));
            faxCell.setNoWrap(true);

            if ((profile.equals("admin"))) {
                IconPane iconPane = gef.getIconPane();
                Icon deleteIcon = iconPane.addIcon();
                deleteIcon.setProperties(contactDeleteIcon, resources.getString("ContactSupprimer"), "javaScript:contactCompanyDeleteConfirm('" + company.getCompanyId() + "')");
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
                                           Collection contacts, String id, String componentLabel, GraphicElementFactory gef,
                                           ServletRequest request, HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException {

        ArrayPane arrayPane = null;
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

        Iterator iterator = contacts.iterator();
        while (iterator.hasNext()) {
            ContactFatherDetail contactFather = (ContactFatherDetail) iterator.next();
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
                link = "javascript:onClick=contactGoToUserInTopic('" + contact.getPK().getId() + "','" + fatherId + "')";
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
                        ligne.addArrayCellText(EncodeHelper.javaStringToHtmlString(userFull.getValue(property)));
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
                        } else if ("company".equals(nameColumn)) {
                            // TODO get company here
                            value = "-todo-";
                            //value = contact.get.getEmail();
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

    public static void displayContactsCompany(YellowpagesSessionController yellowpagesScc,
                                              Collection companies, String id, String componentLabel, GraphicElementFactory gef,
                                              ServletRequest request, HttpSession session, ResourcesWrapper resources, JspWriter out) throws IOException {

        ArrayPane arrayPane = null;
        if (id != null) {
            arrayPane = gef.getArrayPane("tableau2", "GoTo?Id=" + id, request, session);
            arrayPane.setVisibleLineNumber(yellowpagesScc.getNbContactPerPage());
        } else {
            arrayPane = gef.getArrayPane("tableau2", "PrintList", request, session);
            arrayPane.setVisibleLineNumber(-1);
        }

        // recherche des colonnes a afficher
        List<String> arrayHeaders = yellowpagesScc.getArrayHeadersCompanies();
        for (String nameHeader : arrayHeaders) {
            arrayPane.addArrayColumn(nameHeader);
        }

        if (companies != null) {
            Iterator iterator = companies.iterator();
            while (iterator.hasNext()) {
                Company company = (Company) iterator.next();

                // remplissage des lignes
                ArrayLine ligne = arrayPane.addArrayLine();
                String icon;
                String link;
                icon = resources.getIcon("yellowpages.group");
                // TODO javascript : lien sur page de visualisation d'une company
                link = "javaScript:alert('Test !');";
                IconPane iconPane = gef.getIconPane();
                Icon carte = iconPane.addIcon();
                carte.setProperties(icon, "", link);

                List<String> properties = yellowpagesScc.getPropertiesCompanies();

                for (String nameColumn : properties) {
                    String value;
                    // recherche la valeur dans Company
                    if (nameColumn.equals("icon")) {
                        ligne.addArrayCellIconPane(iconPane);
                    } else {
                        if ("company".equals(nameColumn)) {
                            value = company.getName();
                        } else if ("email".equals(nameColumn)) {
                            value = company.getEmail();
                        } else if ("phone".equals(nameColumn)) {
                            value = company.getPhone();
                        } else if ("fax".equals(nameColumn)) {
                            value = company.getFax();
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
