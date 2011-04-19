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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.mailinglist.service.util.neko;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.cyberneko.html.filters.ElementRemover;

public class NekoElementRemover extends ElementRemover {
  @Override
  public void comment(org.apache.xerces.xni.XMLString text,
      org.apache.xerces.xni.Augmentations augs)
      throws org.apache.xerces.xni.XNIException {
    return;
  }

  @Override
  public void ignorableWhitespace(org.apache.xerces.xni.XMLString text,
      org.apache.xerces.xni.Augmentations augs)
      throws org.apache.xerces.xni.XNIException {
    return;
  }

  /** End element. */
  public void endElement(QName element, Augmentations augs)
      throws XNIException {
    if (fElementDepth <= fRemovalElementDepth && elementAccepted(element.rawname)) {
      super.endElement(element, augs);
    }
    fElementDepth--;
    if (fElementDepth == fRemovalElementDepth) {
      fRemovalElementDepth = Integer.MAX_VALUE;
    }
    XMLString string = new XMLString();
    string.setValues(new char[] { ' ', ' ' }, 0, 1);
    characters(string, augs);
  }

  @Override
  public void processingInstruction(java.lang.String target,
      org.apache.xerces.xni.XMLString data,
      org.apache.xerces.xni.Augmentations augs)
      throws org.apache.xerces.xni.XNIException {
    return;
  }

}
