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
      string.setValues(new char[]{' ', ' '}, 0, 1);
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
