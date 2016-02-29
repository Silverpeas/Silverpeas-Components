package com.silverpeas.components.organizationchart.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PersonCategoryTest {

  @Test
  public void equalityWithNullKey() throws Exception {
    PersonCategory secretary = new PersonCategory("Secretary", "secretary", 0);
    PersonCategory personnel = new PersonCategory("Personnel", null, 1);
    assertThat(secretary.equals(personnel), is(false));
  }

  @Test
  public void equality() throws Exception {
    PersonCategory firstSecretary = new PersonCategory("Secretary", "secretary", 0);
    PersonCategory secondSecretary = new PersonCategory("Secretary", "secretary", 1);
    assertThat(firstSecretary.equals(secondSecretary), is(true));
  }

  @Test
  public void addingToTreSet() throws Exception {
    Set<PersonCategory> categories = new HashSet<PersonCategory>();
    PersonCategory firstSecretary = new PersonCategory("Secretary", "secretary", 0);
    categories.add(firstSecretary);
    PersonCategory secondSecretary = new PersonCategory("Secretary", "secretary", 1);
    categories.add(secondSecretary);
    assertThat(categories, hasSize(1));

    PersonCategory personnel = new PersonCategory("Personnel", null, 1);
    categories.add(personnel);
    assertThat(categories, hasSize(2));
  }

}
