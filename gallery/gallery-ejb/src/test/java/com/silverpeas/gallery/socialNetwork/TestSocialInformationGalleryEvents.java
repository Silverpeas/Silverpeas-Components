/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.gallery.socialNetwork;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.dbunit.database.IDatabaseConnection;
import org.junit.Test;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.stratelia.webactiv.beans.admin.UserDetail;


public class TestSocialInformationGalleryEvents extends AbstractTestDao{
  
  private SocialGallery gallery;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gallery = new SocialGallery();
  }
  
  @Test
  public void testUpdatePhoto() throws Exception{    
    IDatabaseConnection connexion = null;
    connexion = getConnection();
    this.setUp(); 
    
    UserDetail user = new UserDetail();
    user.setId("1");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Date fleurCreated = sdf.parse("2010/06/15");
    Date fleurUpdate =  sdf.parse("2010/06/16");  
    Date merCreated = sdf.parse("2010/07/04");
    Date merUpdate = sdf.parse("2010/07/08"); 
    Date cielCreated = sdf.parse("2010/03/01");
    Date cielUpdate = sdf.parse("2010/05/01");
    PhotoDetail ciel = new PhotoDetail("ciel", "france", cielCreated, cielUpdate, null, null, false, false);
    PhotoWithStatus updateCiel = new PhotoWithStatus(ciel, true);
    String cielId = "0";      
    PhotoPK photoPK0 = new PhotoPK(cielId,"gallery25");
    ciel.setPhotoPK(photoPK0);
    ciel.setCreatorId("0");
    ciel.setUpdateId("1");
    ciel.setSizeH(110);
    ciel.setSizeL(110);
    ciel.setAlbumId("0");
    ciel.setImageName("ciel.jpg");
    ciel.setImageSize(5146);
    ciel.setImageMimeType("image/png");    
    PhotoDetail fleur = new PhotoDetail("fleur", "tulipe", fleurCreated, fleurUpdate, null, null, false, false);
    String fleurId = "3";      
    PhotoPK photoPK3 = new PhotoPK(fleurId,"gallery26");
    fleur.setPhotoPK(photoPK3);
    fleur.setCreatorId("1");
    fleur.setUpdateId("0");
    fleur.setSizeH(110);
    fleur.setSizeL(110);
    fleur.setAlbumId("0");
    fleur.setImageName("fleur.jpg");
    fleur.setImageSize(5146);
    fleur.setImageMimeType("image/png");  
    PhotoWithStatus updateFleur = new PhotoWithStatus(fleur, false);
    PhotoDetail mer = new PhotoDetail ("mer" ,"mediterranee",merCreated, merUpdate, null, null , false, false);
    String merId = "4";
    PhotoPK photoPK4 = new PhotoPK(merId,"gallery27");
    mer.setPhotoPK(photoPK4);
    mer.setCreatorId("1");
    mer.setUpdateId("1");
    mer.setSizeH(110);
    mer.setSizeL(110);
    mer.setAlbumId("0");
    mer.setImageName("mer.jpg");
    mer.setImageSize(5146);
    mer.setImageMimeType("image/png");
    SocialInformation socialInformation1Mer = new SocialInformationGallery(new PhotoWithStatus(mer, true));
    SocialInformation socialInformation2Mer = new SocialInformationGallery(new PhotoWithStatus(mer, false));
    try {
      List<SocialInformation> photos = gallery.getAllEventByUser(user,2);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 2 date creation or update ", 2, photos.size());     
      assertEquals(socialInformation1Mer, photos.get(0));
      assertEquals(socialInformation2Mer, photos.get(1));
      
      photos = gallery.getAllEventByUser(user, 2);
      assertNotNull("Photos should exist", photos);
      assertEquals("Should have 2 date creation or update ", 2, photos.size());      
     
    } finally {
      closeConnection(connexion);
    }
  }

  @Override
  protected String getDatasetFileName() {
    // TODO Auto-generated method stub
    return "dataset_Gallery.xml";
  }
}
