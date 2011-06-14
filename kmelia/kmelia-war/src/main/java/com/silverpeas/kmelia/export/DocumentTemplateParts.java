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
  static final String FIELD_MODIFICATION_DATE = "ModifiedAt";
  
  /**
   * Field in the template corresponding to the creation date of the publication to export.
   */
  static final String FIELD_CREATION_DATE = "CreatedAt";
  
  /**
   * Field in the template corresponding to the URL of the publication to export.
   */
  static final String FIELD_URL = "URL";
  
  /**
   * Field in the template corresponding to the version of the publication to export.
   */
  static final String FIELD_VERSION = "Version";
  
  /**
   * The field in the template corresponding to the description of the publication to export.
   */
  static final String FIELD_DESCRIPTION = "Subject";
  
  /**
   * The section in the template relative to the information about the publication.
   */
  static final String SECTION_INFO = "Info";
  
  /**
   * The section in the template relative to the attachments of the publication.
   */
  static final String SECTION_ATTACHMENTS = "Attachments";
  
  /**
   * The section in the template relative to the links to others publications.
   */
  static final String SECTION_SEEALSO = "SeeAlso";
  
  /**
   * The section in the template relative to the classification of the publication.
   */
  static final String SECTION_CLASSIFICATION = "Classification";
  
  /**
   * The section in the template that renders the publication's comments.
   */
  static final String SECTION_COMMENTS = "Comments";
  
  /**
   * The section in the template that renders the publication's content.
   */
  static final String SECTION_CONTENT = "Content";
  
  /**
   * The area in which the metadata about the publication are rendered. The metadata are displayed
   * in the text section defined by SECTION_INFO.
   */
  static final String LIST_OF_METADATA = "PubliInfo";
  
  /*
   * The area in wich all the comments of the publication have to be rendered. The comments are
   * displayed in the text section defined by SECTION_COMMENTS.
   */
  static final String LIST_OF_COMMENTS = "PubliComments";
  
  /*
   * The area in wich all the attachments of the publication have to be rendered. The attachments
   * are displayed in the text section defined by SECTION_ATTACHMENTS.
   */
  static final String LIST_OF_ATTACHMENTS = "PubliAttachments";
  
  /*
   * The area in wich all the publications to which the publication is linked are rendered. The
   * links are displayed in the text section defined by SECTION_SEEALSO.
   */
  static final String LIST_OF_LINKS = "PubliSeeAlso";
  
}
