package org.silverpeas.components.organizationchart.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

class PersonCategoryTest {

  @Test
  void equalityWithNullKey() {
    PersonCategory secretary = new PersonCategory("Secretary", "secretary", 0);
    PersonCategory personnel = new PersonCategory("Personnel", null, 1);
    assertThat(secretary.equals(personnel), is(false));
  }

  @Test
  void equality() {
    PersonCategory firstSecretary = new PersonCategory("Secretary", "secretary", 0);
    PersonCategory secondSecretary = new PersonCategory("Secretary", "secretary", 1);
    assertThat(firstSecretary.equals(secondSecretary), is(true));
  }

  @Test
  void addingToTreSet() {
    Set<PersonCategory> categories = new HashSet<>();
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
