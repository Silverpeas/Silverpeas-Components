package com.silverpeas.silvercrawler.control;

/**
 * Helper class to manage user profile
 *
 * @author Ludovic Bertin
 *
 */
public class ProfileHelper {

  /**
   * Extract best profile from given profiles.
   *
   * @param profiles  an array of profile
   *
   * @return  the best profile
   */
  public static String getBestProfile(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }

      else if (profiles[i].equals("publisher")) {
        flag = "publisher";
      }
    }
    return flag;
  }
}
