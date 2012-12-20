/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.crm.vo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.img;

import org.silverpeas.attachment.model.SimpleDocument;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public abstract class ElementVO {

  protected ResourcesWrapper resources;
  private DateFormat dateFormat = null;
  private DateFormat displayDateFormat = null;

  public ElementVO(ResourcesWrapper resources) {
    this.resources = resources;
    dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    displayDateFormat = new SimpleDateFormat(resources.getString("GML.dateFormat"));
  }

  protected String getAttachments(List<SimpleDocument> attachments) {
    ElementContainer container = new ElementContainer();
    String context =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    for (SimpleDocument attachment : attachments) {
      container.addElement(getImageLink(context + attachment.getAttachmentURL(), "CRMWindow",
          resources.getIcon("crm.attachedFiles"), attachment.getFilename()));
      container.addElement("&nbsp;");
    }
    return container.toString();
  }

  protected String getOperationLinks(String type, String name, String id) {
    ElementContainer container = new ElementContainer();
    container.addElement(getImageLink(
            "javascript:edit" + type + "('" + id + "')", null, resources.getIcon("crm.update"),
            resources.getString("crm.update") + " '" + EncodeHelper.javaStringToHtmlString(name) +
            "'"));
    container.addElement("&nbsp;&nbsp;&nbsp;");
    container
        .addElement(getImageLink(
            "javascript:delete" + type + "('" + id + "')", null, resources.getIcon("crm.delete"),
            resources.getString("crm.delete") + " '" + EncodeHelper.javaStringToHtmlString(name) +
            "'"));
    return container.toString();
  }

  protected String getActive(String active) {
    String key = (active.equals("1") ? "crm.actif" : "crm.nonActif");
    img image = new img();
    image.setSrc(resources.getIcon(key));
    image.setAlt(key);
    return image.toString();
  }

  protected String getDate(String date) {
    if (StringUtil.isDefined(date)) {
      try {
        return displayDateFormat.format(dateFormat.parse(date));
      } catch (ParseException e) {
        // do nothing
      }
    }
    return "";
  }

  private a getImageLink(String href, String target, String src, String alt) {
    a link = new a();
    link.setHref(href);
    if (StringUtil.isDefined(target)) {
      link.setTarget(target);
    }
    img image = new img();
    image.setSrc(src);
    image.setAlt(alt);
    image.setTitle(alt);
    link.addElement(image);
    return link;
  }

}
