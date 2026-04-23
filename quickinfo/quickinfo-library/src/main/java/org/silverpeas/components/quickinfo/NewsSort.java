package org.silverpeas.components.quickinfo;

public enum NewsSort {
    ASC,
    DESC;

    public static NewsSort fromString(String value) {
        if (value == null) {
            return ASC; // default value
        }
        try {
            return NewsSort.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid NewsSort value: " + value);
        }
    }
}
