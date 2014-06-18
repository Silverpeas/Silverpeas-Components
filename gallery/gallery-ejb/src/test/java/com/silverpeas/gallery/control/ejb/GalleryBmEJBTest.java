/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.silverpeas.gallery.control.ejb;

import com.silverpeas.gallery.BaseGalleryTest;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.Order;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

public class GalleryBmEJBTest extends BaseGalleryTest {

  private GalleryBmEJBMock galleryBmEJB;
  private NodeBm nodeBm;
  private static final String ALBUM_NAME = "Nature";

  @Before
  public void prepareGalleryBmEJB() throws Exception {
    List<NodeDetail> nodes = new ArrayList<NodeDetail>();
    final NodeDetail nodeSon1 =
        new NodeDetail("1", ALBUM_NAME,
            "Noeud de test contenant des images sur le thème de la nature", "2014/06/10", "0", "/0/",
            2, "0", null, null, null, null);
    nodeSon1.setOrder(1);
    nodes.add(nodeSon1);
    final NodeDetail nodeSon2 =
        new NodeDetail("2", "Automobile",
            "Noeud de test contenant des images sur le thème automobile", "2014/06/10", "0", "/0/",
            2, "0", null, null, null, null);
    nodeSon2.setOrder(2);
    nodes.add(nodeSon2);
    final NodeDetail nodeDetail =
        new NodeDetail("0", "Accueil", "La Racine", "2014/06/10", "0", "/", 1, "-1", "", "Visible",
            null, null);
    nodeDetail.setChildrenDetails(nodes);

    nodeBm = Mockito.mock(NodeBm.class);

    when(nodeBm.getDetail(Mockito.any(NodePK.class))).thenAnswer( new Answer<NodeDetail>() {
      @Override
      public NodeDetail answer(InvocationOnMock invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        if (arguments != null && arguments.length > 0 && arguments[0] != null) {
          NodePK key = (NodePK) arguments[0];
          if (key.getId().equals("0")) {
            return nodeDetail;
          } else if (key.getId().equals("1")) {
            return nodeSon1;
          } else if (key.getId().equals("2")) {
            return nodeSon2;
          }
        }
        return null;
      }
    });

    galleryBmEJB = new GalleryBmEJBMock(nodeBm, getDataSource());
  }

  @Test
  public void testGetRootAlbum() {
    NodePK nodePK = new NodePK("0", INSTANCE_A);
    AlbumDetail album = galleryBmEJB.getAlbum(nodePK, false);
    assertThat(album, notNullValue());
    assertThat(album.getChildrenDetails(), hasSize(2));
    assertThat(album.getName(), equalTo("Accueil"));
    assertThat(album.getPhotos(), empty());
  }

  @Test
  public void testGetAlbumWithPhotos() {
    NodePK nodePK = new NodePK("1", INSTANCE_A);
    AlbumDetail album = galleryBmEJB.getAlbum(nodePK, false);
    assertThat(album.getName(), equalTo(ALBUM_NAME));
    assertThat(album.getPhotos(), hasSize(3));
  }

  @Test
  public void testGetAllOrders() {
    List<Order> orders = galleryBmEJB.getAllOrders(writerUser.getId(), INSTANCE_A);
    assertThat(orders, hasSize(2));

    orders = galleryBmEJB.getAllOrders(adminAccessUser.getId(), INSTANCE_A);
    assertThat(orders, hasSize(0));

    orders = galleryBmEJB.getAllOrders(writerUser.getId(), "otherInstanceId");
    assertThat(orders, hasSize(0));
  }

  @Test
  public void testGetOrder() {
    Order order = galleryBmEJB.getOrder("201", INSTANCE_A);
    assertThat(order, notNullValue());

    order = galleryBmEJB.getOrder("201", "otherInstanceId");
    assertThat(order, nullValue());
  }

  @Test
  public void testDeleteOrders() {
    List<Order> orders = galleryBmEJB.getAllOrders(writerUser.getId(), INSTANCE_A);
    assertThat(orders, hasSize(2));

    galleryBmEJB.deleteOrders(orders);

    orders = galleryBmEJB.getAllOrders(writerUser.getId(), INSTANCE_A);
    assertThat(orders, hasSize(0));
  }

  @Test
  public void testGetAllOrderToDelete() {
    List<Order> orders = galleryBmEJB.getAllOrderToDelete(0);
    assertThat(orders, hasSize(3));

    orders = galleryBmEJB.getAllOrderToDelete(365000);
    assertThat(orders, hasSize(0));
  }
}
