package com.silverpeas.processManager;

import java.util.Comparator;

/**
 * A named value pair.
 */
public final class NamedValue {
  public final String name;
  public final String value;

  public NamedValue(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public boolean equals(Object o) {
    if (o instanceof NamedValue) {
      return name.equals(((NamedValue) o).name);
    } else
      return false;
  }

  public int hashCode() {
    return name.hashCode();
  }

  static Comparator ascendingValues = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 instanceof NamedValue && o2 instanceof NamedValue) {
        return ((NamedValue) o1).value.compareTo(((NamedValue) o2).value);
      } else
        throw new ClassCastException();
    }
  };
}
