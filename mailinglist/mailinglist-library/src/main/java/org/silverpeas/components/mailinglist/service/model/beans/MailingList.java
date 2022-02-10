/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist.service.model.beans;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "sc_mailinglist_list")
@NamedQueries({
    @NamedQuery(name = "mailinglist.findByComponentId", query = "from MailingList where componentId = " +
        ":componentId"),
    @NamedQuery(name = "mailinglist.findAll", query = "from MailingList")})
public class MailingList extends IdentifiableObject {
  private static final long serialVersionUID = 3983404426767796807L;

  private String componentId;
  @Transient
  private String name;
  @Transient
  private String subscribedAddress;
  @Transient
  private String description;
  @Transient
  private boolean open;
  @Transient
  private boolean moderated;
  @Transient
  private boolean notify;
  @Transient
  private boolean supportRSS;
  @Transient
  private Set<InternalUser> moderators = new HashSet<>();
  @Transient
  private Set<InternalUser> readers = new HashSet<>();
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "listId", nullable = false)
  private Set<ExternalUser> externalSubscribers = new HashSet<>();
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "mailingListId", nullable = false)
  private Set<InternalSubscriber> internalSubscribers = new HashSet<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSubscribedAddress() {
    return subscribedAddress;
  }

  public void setSubscribedAddress(String subscribedAddress) {
    this.subscribedAddress = subscribedAddress;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean publiclyVisible) {
    this.open = publiclyVisible;
  }

  public boolean isModerated() {
    return moderated;
  }

  public void setModerated(boolean moderated) {
    this.moderated = moderated;
  }

  public Set<InternalUser> getModerators() {
    return moderators;
  }

  public void setModerators(Set<InternalUser> moderators) {
    this.moderators = moderators;
  }

  public Set<InternalUser> getReaders() {
    return readers;
  }

  public void setReaders(Set<InternalUser> readers) {
    this.readers = readers;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public Set<ExternalUser> getExternalSubscribers() {
    return externalSubscribers;
  }

  public boolean isNotify() {
    return notify;
  }

  public void setNotify(boolean notify) {
    this.notify = notify;
  }

  public void removeExternalSubscriber(ExternalUser user) {
    externalSubscribers.remove(user);
  }

  public void addExternalSubscriber(ExternalUser user) {
    externalSubscribers.add(user);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isSupportRSS() {
    return supportRSS;
  }

  public void setSupportRSS(boolean supportRSS) {
    this.supportRSS = supportRSS;
  }

  public boolean isEmailAuthorized(String email) {
    for (ExternalUser user : externalSubscribers) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    for (InternalUser user : readers) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    for (InternalUser user : moderators) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    return false;
  }

  public Set<InternalGroupSubscriber> getGroupSubscribers() {
    return new InternalSubscriberSet<>(InternalGroupSubscriber.class);
  }

  public Set<InternalUserSubscriber> getInternalSubscribers() {
    return new InternalSubscriberSet<>(InternalUserSubscriber.class);
  }

  private class InternalSubscriberSet<T extends InternalSubscriber> implements Set<T> {

    private Class<T> subscriberType;

    InternalSubscriberSet(Class<T> typeOfT) {
      subscriberType = typeOfT;
    }

    private Stream<T> filter() {
      return (Stream<T>) internalSubscribers.stream()
          .filter(s -> s.getClass().isAssignableFrom(subscriberType));
    }

    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
      return (int) filter().count();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
      return this.size() == 0;
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     * @param o element whose presence in this set is to be tested
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException if the type of the specified element
     * is incompatible with this set
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     * set does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(final Object o) {
      return internalSubscribers.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     * @return an iterator over the elements in this set
     */
    @Override
    public Iterator<T> iterator() {
      return filter().collect(Collectors.toSet()).iterator();
    }

    /**
     * Returns an array containing all of the elements in this set.
     * If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the
     * elements in the same order.
     * <p>
     * <p>The returned array will be "safe" in that no references to it
     * are maintained by this set.  (In other words, this method must
     * allocate a new array even if this set is backed by an array).
     * The caller is thus free to modify the returned array.
     * <p>
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     * @return an array containing all the elements in this set
     */
    @Override
    public Object[] toArray() {
      return filter().collect(Collectors.toSet()).toArray();
    }

    /**
     * Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     * If the set fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this set.
     * <p>
     * <p>If this set fits in the specified array with room to spare
     * (i.e., the array has more elements than this set), the element in
     * the array immediately following the end of the set is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * set <i>only</i> if the caller knows that this set does not contain
     * any null elements.)
     * <p>
     * <p>If this set makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements
     * in the same order.
     * <p>
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     * <p>
     * <p>Suppose <tt>x</tt> is a set known to contain only strings.
     * The following code can be used to dump the set into a newly allocated
     * array of <tt>String</tt>:
     * <p>
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     * <p>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     * @param a the array into which the elements of this set are to be
     * stored, if it is big enough; otherwise, a new array of the same
     * runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException if the runtime type of the specified array
     * is not a supertype of the runtime type of every element in this
     * set
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public <T1> T1[] toArray(final T1[] a) {
      return filter().collect(Collectors.toSet()).toArray(a);
    }

    /**
     * Adds the specified element to this set if it is not already present
     * (optional operation).  More formally, adds the specified element
     * <tt>e</tt> to this set if the set contains no element <tt>e2</tt>
     * such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.  In combination with the
     * restriction on constructors, this ensures that sets never contain
     * duplicate elements.
     * <p>
     * <p>The stipulation above does not imply that sets must accept all
     * elements; sets may refuse to add any particular element, including
     * <tt>null</tt>, and throw an exception, as described in the
     * specification for {@link Collection#add Collection.add}.
     * Individual set implementations should clearly document any
     * restrictions on the elements that they may contain.
     * @param t element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     * is not supported by this set
     * @throws ClassCastException if the class of the specified element
     * prevents it from being added to this set
     * @throws NullPointerException if the specified element is null and this
     * set does not permit null elements
     * @throws IllegalArgumentException if some property of the specified element
     * prevents it from being added to this set
     */
    @Override
    public boolean add(final T t) {
      return internalSubscribers.add(t);
    }

    /**
     * Removes the specified element from this set if it is present
     * (optional operation).  More formally, removes an element <tt>e</tt>
     * such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this set contains such an element.  Returns <tt>true</tt> if this set
     * contained the element (or equivalently, if this set changed as a
     * result of the call).  (This set will not contain the element once the
     * call returns.)
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException if the type of the specified element
     * is incompatible with this set
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     * set does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     * is not supported by this set
     */
    @Override
    public boolean remove(final Object o) {
      return internalSubscribers.remove(o);
    }

    /**
     * Returns <tt>true</tt> if this set contains all of the elements of the
     * specified collection.  If the specified collection is also a set, this
     * method returns <tt>true</tt> if it is a <i>subset</i> of this set.
     * @param c collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements of the
     * specified collection
     * @throws ClassCastException if the types of one or more elements
     * in the specified collection are incompatible with this
     * set
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     * or more null elements and this set does not permit null
     * elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     * or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
      return internalSubscribers.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present (optional operation).  If the specified
     * collection is also a set, the <tt>addAll</tt> operation effectively
     * modifies this set so that its value is the <i>union</i> of the two
     * sets.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.
     * @param c collection containing elements to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     * is not supported by this set
     * @throws ClassCastException if the class of an element of the
     * specified collection prevents it from being added to this set
     * @throws NullPointerException if the specified collection contains one
     * or more null elements and this set does not permit null
     * elements, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     * specified collection prevents it from being added to this set
     * @see #add(Object)
     */
    @Override
    public boolean addAll(final Collection<? extends T> c) {
      return internalSubscribers.addAll(c);
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this set all of its elements that are not contained in the
     * specified collection.  If the specified collection is also a set, this
     * operation effectively modifies this set so that its value is the
     * <i>intersection</i> of the two sets.
     * @param c collection containing elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     * is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     * is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this set contains a null element and the
     * specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     * or if the specified collection is null
     * @see #remove(Object)
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
      return internalSubscribers.retainAll(c);
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection (optional operation).  If the specified
     * collection is also a set, this operation effectively modifies this
     * set so that its value is the <i>asymmetric set difference</i> of
     * the two sets.
     * @param c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     * is not supported by this set
     * @throws ClassCastException if the class of an element of this set
     * is incompatible with the specified collection
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this set contains a null element and the
     * specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     * or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
      return internalSubscribers.removeAll(c);
    }

    /**
     * Removes all of the elements from this set (optional operation).
     * The set will be empty after this call returns.
     * @throws UnsupportedOperationException if the <tt>clear</tt> method
     * is not supported by this set
     */
    @Override
    public void clear() {
      Set<T> subscribers = filter().collect(Collectors.toSet());
      internalSubscribers.removeAll(subscribers);
    }
  }
}
