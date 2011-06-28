package com.silverpeas.classifieds.control;

public enum ClassifiedsRole implements Comparable<ClassifiedsRole> {
  ANONYMOUS("anonymous"), READER("reader"), PUBLISHER("publisher"), MANAGER("admin");

  private String name = null;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  private ClassifiedsRole(String name) {
    this.name = name;
  }

  public static ClassifiedsRole getRole(String roleName) {
    for (ClassifiedsRole role : values()) {
      if ( role.getName().equals(roleName) ) {
        return role;
      }
    }

    return READER;
  }

  public static ClassifiedsRole getRole(String[] profiles) {
    ClassifiedsRole highestRole = READER;
    for (String profile : profiles) {
      ClassifiedsRole role = ClassifiedsRole.getRole(profile);
      if (role.compareTo(highestRole) > 0 ) {
        highestRole = role;
      }
    }

    return highestRole;
  }

}
