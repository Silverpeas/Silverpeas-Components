/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.kmelia.export;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.odftoolkit.odfdom.pkg.OdfPackage;
import java.net.URI;
import java.net.URISyntaxException;
import org.w3c.dom.NamedNodeMap;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.dom.element.style.StyleMasterPageElement;
import java.util.Iterator;
import org.odftoolkit.odfdom.dom.element.draw.DrawFrameElement;
import org.odftoolkit.odfdom.dom.element.draw.DrawImageElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeMasterStyles;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.draw.Frame;
import org.odftoolkit.simple.draw.Image;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.silverpeas.util.StringUtil.*;

/**
 * A decorator of an OpenDocument text document. It decorates an ODT document by adding document
 * merging capabilities to it.
 *
 * With the content of the document to merge, the merging operation imports only the text and
 * paragraph styles; styles on tables, lists, and others elements aren't imported, so it is the
 * styles defined in the document source that are applied on theses elements of the imported content.
 *
 * The merger is smart enough to rename the imported text and paragraph styles in order to avoid
 * conflicts with eponymous styles defined in the document source, and to take new
 * document layout definitions from the document to merge.
 */
class ODTDocumentsMerging extends TextDocument {

  /**
   * The prefix to concat with the names and with the display name of the styles to import.
   */
  protected static final String MERGE_STYLE_PREFIX = "IMPORTED";
  /**
   * The prefix to concat with the display name of the styles to import.
   */
  protected static final String MERGE_STYLE_DISPLAY_NAME_PREFIX = MERGE_STYLE_PREFIX + " ";
  /**
   * The prefix to concat with thr name of the styles to import.
   */
  protected static final String MERGE_STYLE_NAME_PREFIX = MERGE_STYLE_PREFIX + "_";

  /**
   * Creates a merger for the specified ODT document.
   * @param document the document to which merges can be done.
   * @return an ODTDocumentsMerger instance.
   */
  public static ODTDocumentsMerging decorates(final TextDocument document) {
    return new ODTDocumentsMerging(document);
  }

  /**
   * A convenient method to improve readability of the merge method call that waits for a section
   * name as parameter.
   * @param sectionName name of a text section in the wrapped document.
   * @return the name of the section passed as parameter.
   */
  public static String atSection(String sectionName) {
    return sectionName;
  }

  /**
   * Merges the specified document into self. The content is merged at the end of the
   * decorated document.
   * @param theDocument the document to merge.
   * @return the document resulting of the merge.
   */
  public TextDocument merge(final TextDocument theDocument) {
    try {
      importGlobalStylesOf(theDocument);
      importContentStylesOf(theDocument);
      Node myContent = getTextDocument().getContentDom().getFirstChild().getLastChild().
              getFirstChild();
      insertContentTextOf(theDocument, into(myContent));
      return getTextDocument();
    } catch (Exception ex) {
      throw new DocumentMergeException(ex.getMessage(), ex);
    }
  }

  /**
   * Merges the specified document into the wrapped one. The content is merge at the specified
   * text section.
   * @param document the document to merge.
   * @param section the name of the section at which the imported document as to be inserted.
   * @return the document resulting of the merge.
   */
  public TextDocument merge(final TextDocument theDocument, String section) {
    try {
      importGlobalStylesOf(theDocument);
      importContentStylesOf(theDocument);
      Node theSection = getTextDocument().getSectionByName(section).getOdfElement();
      insertContentTextOf(theDocument, into(theSection));
      return getTextDocument();
    } catch (Exception ex) {
      throw new DocumentMergeException(ex.getMessage(), ex);
    }
  }

  /**
   * Imports some global styles of the specified document.
   * The global styles are defined in the styles.xml files nested in the ODT document.
   * Currently, only text and paragraph styles are fetched. Different master styles (id est document
   * layout definitions) are also imported.
   * @param document the document to import.
   * @throws Exception if an error occurs while importing the global styles.
   */
  protected void importGlobalStylesOf(final TextDocument document) throws Exception {
    OdfOfficeStyles documentStyles = document.getDocumentStyles();
    OdfOfficeStyles stylesNode = (OdfOfficeStyles) getTextDocument().getStylesDom().importNode(
            documentStyles, true);
    renameAndImportStyles(stylesNode.getStylesForFamily(OdfStyleFamily.Paragraph),
            getTextDocument().getDocumentStyles());
    renameAndImportStyles(stylesNode.getStylesForFamily(OdfStyleFamily.Text),
            getTextDocument().getDocumentStyles());

    // import new master styles (page layouts)
    OdfOfficeMasterStyles documentMasterStyles = document.getOfficeMasterStyles();
    OdfOfficeMasterStyles templateMasterStyles = getTextDocument().getOfficeMasterStyles();
    Iterator<StyleMasterPageElement> masterPages = documentMasterStyles.getMasterPages();
    while (masterPages.hasNext()) {
      StyleMasterPageElement styleMasterPageElement = masterPages.next();
      StyleMasterPageElement existingMasterPage =
              templateMasterStyles.getMasterPage(styleMasterPageElement.getStyleNameAttribute());
      if (existingMasterPage == null) {
        Node masterPageNode = getTextDocument().getStylesDom().importNode(styleMasterPageElement,
                true);
        getTextDocument().getOfficeMasterStyles().appendChild(masterPageNode.cloneNode(true));
      }
    }
  }

  /**
   * Imports the styles that are particular to the specified document.
   * Such styles come from the customization of some global styles for the document and they are
   * automatically generated by the ODT editor. They are defined within the XML element
   * automatic-styles in the XML file content.xml nested in the ODT document.
   * Currently, only text and paragraph styles are fetched.
   * @param document the doccument to import.
   * @throws Exception if an error occurs while importing the specific styles of the specified
   * document.
   */
  protected void importContentStylesOf(final TextDocument document) throws Exception {
    OdfOfficeAutomaticStyles automaticStyles = document.getContentDom().getAutomaticStyles();
    OdfOfficeAutomaticStyles automaticStylesNode = (OdfOfficeAutomaticStyles) getTextDocument().
            getContentDom().importNode(automaticStyles, true);
    renameAndImportStyles(automaticStylesNode.getStylesForFamily(OdfStyleFamily.Paragraph),
            getTextDocument().getContentDom().getAutomaticStyles());
    renameAndImportStyles(automaticStylesNode.getStylesForFamily(OdfStyleFamily.Text),
            getTextDocument().getContentDom().getAutomaticStyles());
  }

  /**
   * Imports the text content of the specified document and appends it at the specified location of
   * this document.
   *
   * During the import of the text content, the external images referenced in the text of the
   * specified document are embedded into this document.
   * @param document the document to import.
   * @param content a node in the XML structure of this document within which the text has to be
   * imported.
   * @throws Exception if an error occurs while importing the text of the document.
   */
  protected void insertContentTextOf(final TextDocument document, final Node content) throws
          Exception {
    Node textContent = document.getContentDom().getElementsByTagName(
            OpenDocumentTextElements.ELEMENT_OFFICE_TEXT).item(0);
    copyXMLNode(textContent, content);

    NodeList imageNodes = getTextDocument().getContentDom().getElementsByTagName(
            OpenDocumentTextElements.ELEMENT_DRAW_IMAGE);
    embedImages(imageNodes);
  }

  /**
   * Renames the specified styles and then imports them in the specified location of this document.
   *
   * The styles are renamed by prefixing them with the terms as defined by the
   * MERGE_STYLE_NAME_PREFIX constant.
   * @param styles the styles to import.
   * @param importNode the node in the XML structure of this document and within which styles are
   * defined.
   */
  protected void renameAndImportStyles(final Iterable<OdfStyle> styles, final Node importNode) {
    for (OdfStyle style : styles) {
      style.setStyleNameAttribute(MERGE_STYLE_NAME_PREFIX + style.getStyleNameAttribute());
      String displayStyleName = style.getStyleDisplayNameAttribute();
      if (isDefined(displayStyleName)) {
        style.setStyleDisplayNameAttribute(MERGE_STYLE_DISPLAY_NAME_PREFIX + displayStyleName);
      }
      String parentStyleName = style.getStyleParentStyleNameAttribute();
      if (isDefined(parentStyleName)) {
        style.setStyleParentStyleNameAttribute(MERGE_STYLE_NAME_PREFIX + parentStyleName);
      }
      String nextStyleName = style.getStyleNextStyleNameAttribute();
      if (isDefined(nextStyleName)) {
        style.setStyleNextStyleNameAttribute(MERGE_STYLE_NAME_PREFIX + nextStyleName);
      }

      importNode.appendChild(style.cloneNode(true));
    }
  }

  private void updateStylesIn(final Node node) {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes != null) {
      Node attribute = attributes.getNamedItem(OpenDocumentTextElements.ATTRIBUTE_STYLE_NAME);
      if (attribute != null) {
        String style = attribute.getNodeValue();
        if (style.startsWith("P") || style.startsWith("Text")) {
          attribute.setNodeValue("T3");
        } else {
          attribute.setNodeValue(MERGE_STYLE_NAME_PREFIX + style);
        }
      }
    }
    if (node.hasChildNodes()) {
      NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        updateStylesIn(children.item(i));
      }
    }
  }

  /**
   * Copies the XML tree from the specified source node into the destination node of another XML
   * tree.
   * @param source the root node from which the XML tree should be copied.
   * @param destination the XML node at which the source has to be copied.
   * @throws Exception if an error occurs while copying a node to another one.
   */
  private void copyXMLNode(final Node source, final Node destination) throws Exception {
    Node importedTextContent = getTextDocument().getContentDom().importNode(source, true);
    NodeList importedTextContentNodes = importedTextContent.getChildNodes();
    for (int i = 0; i < importedTextContentNodes.getLength(); i++) {
      Node aTextContent = importedTextContentNodes.item(i);
      updateStylesIn(aTextContent);
      destination.appendChild(aTextContent.cloneNode(true));
    }
  }

  /**
   * Embeds external images mapped with the specified XML nodes into this document.
   * @param imageNodes a list of XML nodes mapping each of them an image.
   * @throws URISyntaxException if the URI defining an image is malformed.
   */
  private void embedImages(final NodeList imageNodes) {
    for (int i = 0; i < imageNodes.getLength(); i++) {
      Node imageNode = imageNodes.item(i);
      Node hrefNode = imageNode.getAttributes().getNamedItem(
              OpenDocumentTextElements.ATTRIBUTE_LINK_REF);
      if (hrefNode != null) {
        URI imageURI = getAttachedImageURI(hrefNode.getNodeValue());
        if (imageURI != null) {
          Image image = Image.getInstanceof((DrawImageElement) imageNode);
          Frame imageFrame = image.getFrame();
          String height = imageFrame.getDrawFrameElement().getSvgHeightAttribute();
          String width = imageFrame.getDrawFrameElement().getSvgWidthAttribute();
          Image embeddedImage = Image.newImage(imageFrame, imageURI);
          DrawFrameElement drawFrame = embeddedImage.getFrame().getDrawFrameElement();
          drawFrame.setSvgWidthAttribute(width);
          drawFrame.setSvgHeightAttribute(height);
          image.remove();
        } else {
          imageNode.getParentNode().removeChild(imageNode);
        }
      }
    }
  }

  private URI getAttachedImageURI(String href) {
    URI imageURI = null;
    SilverpeasImageFinder imageFinder = SilverpeasImageFinder.getImageFinder();
    try {
      String imagePath = imageFinder.findImageReferenceddBy(href);
      imageURI = URI.create(imagePath);
    } catch (Exception ex) {
      Logger.getLogger(ODTDocumentsMerging.class.getName()).log(Level.SEVERE, "Cannot find "
              + "the image referenced by the href='" + href + "'", ex);
    }
    return imageURI;
  }

  /**
   * Constructs a new TextDocument instance decorated with merging features.
   * As the specified text document is already opened, this one will refer the same data of the
   * specified parameter.
   * @see TextDocument#TextDocument(OdfPackage, String, TextDocument.OdfMediaType)
   * @param document the ODT document from which the new ODTDocumentsMerging has to be constructed.
   */
  private ODTDocumentsMerging(final TextDocument document) {
    super(document.getPackage(), document.getDocumentPath(), TextDocument.OdfMediaType.TEXT);
  }

  private TextDocument getTextDocument() {
    return this;
  }

  private static Node into(final Node node) {
    return node;
  }

  /**
   * This interface defines the qualified name of all of the XML elements that made up the text of
   * an ODT document.
   */
  protected static interface OpenDocumentTextElements {

    /**
     * The root XML element of the text content of an ODT document.
     */
    final String ELEMENT_OFFICE_TEXT = "office:text";
    /**
     * The XML element of an image within a text content.
     */
    final String ELEMENT_DRAW_IMAGE = "draw:image";
    /**
     * The attribute of an XML element refering an external inserted object.
     */
    final String ATTRIBUTE_LINK_REF = "xlink:href";
    /**
     * The attribute an XML element indicating the name of a style. It can be the name of a style
     * definition as well the name of the style to apply onto a text element of a text content.
     */
    final String ATTRIBUTE_STYLE_NAME = "text:style-name";
  }
}
