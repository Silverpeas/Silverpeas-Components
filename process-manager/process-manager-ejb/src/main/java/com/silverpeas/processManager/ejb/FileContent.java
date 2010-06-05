/*
 * Copyright (C) 2000 - 2010 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.processManager.ejb;

import java.io.Serializable;

/**
 * Object dealing with a file and its content loaded into memory in a array of
 * bytes. A property specifies the original name of the file.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class FileContent implements Serializable {

    private static final long serialVersionUID = 1985142203319238806L;

    /** Name of the file. */
    private String name;

    /** Content of the file, <code>null</code> if none. */
    byte[] content;

    /**
     * Default constructor.
     */
    public FileContent() {
        this(null, null);
    }

    /**
     * Constructor with name and content specifications.
     * 
     * @param name
     *            original name of the file
     * @param content
     *            binary data, already loaded, content of the file.
     */
    public FileContent(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    /**
     * Return the value of the name property.
     * 
     * @return the value of name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of the name property.
     * 
     * @param name
     *            the new value of name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the value of the content property.
     * 
     * @return the value of content (never <code>null</code>, may be an empty
     *         array).
     */
    public byte[] getContent() {
        return (content != null) ? content : new byte[0];
    }

    /**
     * Set the value of the content property.
     * 
     * @param content
     *            the new value of content.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileContent [name=" + name + "]";
    }
}
