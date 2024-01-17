/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * Silverpeas Web Application Component to manage a community of users for a collaborative space. By
 * default, a collaborative space is visible either only to the users having access rights to it or
 * to all users in the case of a public space. The decision to give access rights to users in
 * Silverpeas are decided unilaterally only by space managers. With this application, a
 * collaborative space can become a community space, that is a space with a community of users. In
 * this case, the space is visible to anyone (but not its content in subspaces, applications,
 * contributions, ...) and any user in Silverpeas can ask to join the community of the space. When
 * joining such a community, the user gain then access rights to the space.
 * <p>
 * To allow this Community application to manage the community of users for a given space, the
 * recommended way is to instantiated this application into the targeted space and then to declare
 * it as the home page of that space.
 * </p>
 * @author mmoquillon
 */
package org.silverpeas.components.community;