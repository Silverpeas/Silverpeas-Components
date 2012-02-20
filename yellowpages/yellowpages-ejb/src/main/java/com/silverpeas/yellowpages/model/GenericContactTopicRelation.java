/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.yellowpages.model;

import javax.persistence.*;

@Entity
@Table(name = "sc_contact_genericcontact_topic")
public class GenericContactTopicRelation implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int relationId;


//    @OneToMany
//    @JoinColumn(name = "genericContactId")
//    private Set<GenericContact> genericContacts;

    @Column(name = "genericContactId")
    private int genericContactId;

    @Column(name = "nodeId")
    private int nodeId;

    public GenericContactTopicRelation() {
    }

    public GenericContactTopicRelation(int nodeId, int genericContactId) {
        //this.genericContacts = new HashSet<GenericContact>();
        this.nodeId = nodeId;
        this.genericContactId = genericContactId;
    }

//    public void addGenericContact(GenericContact genericContact) {
//        this.genericContacts.add(genericContact);
//        genericContact.getTopicIds().add(this);
//    }


    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    public int getGenericContactId() {
        return genericContactId;
    }

    public void setGenericContactId(int genericContactId) {
        this.genericContactId = genericContactId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
}
