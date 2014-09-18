package com.stratelia.webactiv.forums.control;

import static org.junit.Assert.*;

import com.stratelia.webactiv.forums.models.ForumPK;
import org.junit.Test;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.forums.control.ForumsSessionController;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import static com.stratelia.webactiv.forums.models.Message.*;
import static org.mockito.Mockito.*;

public class TestForumsSessionController {

  @Test
  public void testIsVisible() throws Exception {
    int forumId = 12;
    MainSessionController mainController = mock(MainSessionController.class);
    UserDetail user = mock(UserDetail.class);
    when(mainController.getCurrentUserDetail()).thenReturn(user);
    when(user.getId()).thenReturn("5");
    ComponentContext context = mock(ComponentContext.class);
    ForumsSessionController controller = new ForumsSessionController(mainController, context);
    ForumsBM forum = mock(ForumsBM.class);
    when(forum.isModerator(eq("5"), any(ForumPK.class))).thenReturn(false);
    when(forum.getForumParentId(forumId)).thenReturn(new Integer(0));
    controller.setForumsBM(forum);
    boolean result = controller.isVisible(STATUS_VALIDATE, forumId);
    assertEquals(true, result);
    verify(forum, times(1)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(1)).getForumParentId(forumId);
    result = controller.isVisible(STATUS_FOR_VALIDATION, forumId);
    assertEquals(false, result);
    verify(forum, times(2)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(2)).getForumParentId(forumId);
    forum = mock(ForumsBM.class);
    when(forum.isModerator(eq("5"), any(ForumPK.class))).thenReturn(true);
    when(forum.getForumParentId(forumId)).thenReturn(new Integer(0));
    controller.setForumsBM(forum);
    result = controller.isVisible(STATUS_VALIDATE, forumId);
    assertEquals(true, result);
    verify(forum, times(1)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(1)).getForumParentId(forumId);
    result = controller.isVisible(STATUS_FOR_VALIDATION, forumId);
    assertEquals(true, result);
    verify(forum, times(2)).isModerator(eq("5"), any(ForumPK.class));
    verify(forum, times(2)).getForumParentId(forumId);
  }

  @Test
  public void testAllProfiles() {
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    ForumsSessionController controller = new ForumsSessionController(mainController, context);
    when(context.getCurrentProfile()).thenReturn(new String[]{SilverpeasRole.admin.toString(),
          SilverpeasRole.user.toString(), SilverpeasRole.reader.toString()});
    assertTrue(controller.isAdmin());
    assertTrue(controller.isUser());
    assertTrue(controller.isReader());

  }

  @Test
  public void testSingleProfiles() {
    MainSessionController mainController = mock(MainSessionController.class);
    ComponentContext context = mock(ComponentContext.class);
    ForumsSessionController controller = new ForumsSessionController(mainController, context);
    when(context.getCurrentProfile()).thenReturn(new String[]{SilverpeasRole.admin.toString()});
    assertTrue(controller.isAdmin());
    assertFalse(controller.isUser());
    assertFalse(controller.isReader());
    when(context.getCurrentProfile()).thenReturn(new String[]{SilverpeasRole.user.toString()});
    assertFalse(controller.isAdmin());
    assertTrue(controller.isUser());
    assertFalse(controller.isReader());
    when(context.getCurrentProfile()).thenReturn(new String[]{SilverpeasRole.reader.toString()});
    assertFalse(controller.isAdmin());
    assertFalse(controller.isUser());
    assertTrue(controller.isReader());
  }
}
