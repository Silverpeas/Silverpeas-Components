/*
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
 * "http://www.silverpeas.org/legal/licensing"
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.export;

/**
 * This interface defines the different parts a template for export must be compound of.
 * Each part in the template is uniquely identified by a name and they can be programmatically
 * retrieved by this name.
 */
public interface DocumentTemplateParts {
  
  /**
   * Field in the template corresponding to the author of the publication to export.
   */
  static final String FIELD_AUTHOR = "Author";
  
  /**
   * Field in the template corresponding to the last modification date of the publication to export.
   */
  static final String FIELD_MODIFICATION_DATE = "Modification";
  
  /**
   * The section in the template relative to the attachments of the publication.
   */
  static final String SECTION_ATTACHMENTS = "Attachments";
  
  /**
   * The section in the template relative to the links to others publications.
   */
  static final String SECTION_SEEALSO = "SeeAlso";
  
  /**
   * The section in the template that renders the publication's comments.
   */
  static final String SECTION_COMMENTS = "Comments";
  
  /**
   * The section in the template that renders the publication's content.
   */
  static final String SECTION_CONTENT = "Content";
  
  /*
   * The area in wich all the comments of the publication have to be rendered.
   */
  static final String LIST_OF_COMMENTS = "PubliComments";
  
  /*
   * The area in wich all the attachments of the publication have to be rendered.
   */
  static final String LIST_OF_ATTACHMENTS = "PubliAttachments";
}
