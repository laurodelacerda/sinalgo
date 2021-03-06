/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package sinalgo.tools.storage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sinalgo.exception.DoublyLinkedListErrorException;
import sinalgo.tools.logging.Logging;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * A doubly linked list implementation which allows insertion and deletion of
 * objects in O(1).
 *
 * <i>This list implementation does not support multiple entries of the same
 * object, neither null objects.</i> I.e. each object may only be contained once
 * per list.
 * <p>
 * This special implementation of a linked list targets applications that store
 * huge amount of objects in a list, and often remove specific elements. In the
 * java.util.LinkedList implementation, removal of an object requires to iterate
 * over the list to find the object to be removed. However, if the object itself
 * knew the 'next' and 'previous' pointers normally used in a doubly linked
 * list, this would not be necessary.
 * <p>
 * In this implementation, we provide a generic linked list, which requires that
 * each entry knows the 'next' and 'previous' pointer. To do so, each entry must
 * implement the DoubleLinkedListEntry interface.
 * <p>
 * When storing an entry only in one list, a single pointer pair ['next',
 * 'previous'] would be sufficient. However, adding the entry to several lists
 * would not be possible. Therefore, the DoubleLinkedListEntry interface
 * requires its subclass to keep a vector of <code>Finger</code> objects, which
 * hold the two pointers ['next', 'previous'] for each list this entry is
 * contained in.
 * <p>
 * In order to allow for increased performance, each list can specify whether
 * the <code>Finger</code> is also removed from the entry when it is removed
 * from the list. Not removing the <code>Finger</code> may increase performance
 * if the same object is repeatedly added and removed from to lists (not
 * necessarily the same). Remember that not deleting the <code>Finger</code>
 * from an entry when it is removed implies, that the size of the entry object
 * is actually bigger than it needs be. Therefore, only set the flag to not
 * delete the <code>Finger</code> for lists where the objects contained in the
 * lists exist long and are added and removed often to the lists.
 *
 * @param <E> The generic type the DLL is created for.
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class DoublyLinkedList<E extends DoublyLinkedListEntry> implements Iterable<E> {

    private boolean keepFinger; // if true, the finger is not removed from the objects list after the object is
    // removed from this list.
    private int size; // # of elements in the list
    private int modCount; // # of modifications
    private DoublyLinkedListEntry.Finger head = new DoublyLinkedListEntry.Finger(null, null); // before the first entry
    // of the list, the
    // terminator
    private DoublyLinkedListEntry.Finger tail = this.getHead(); // the last entry, points to head if the list is empty

    /**
     * Creates a new instance of a Doubly Linked List.
     * <p>
     * This method lets you specify whether entries keep their finger-entry when
     * they are removed from this list. This may increase performance if the same
     * entries are added and removed several times to/from this list. Note that the
     * iterator() method always returns a new iterator ignoring the parameters set
     * at the constructor. Use the getIterator method to get a iterator depending on
     * the parameters.
     *
     * @param keepFinger If set to true, entries keep their finger for later reuse (in this
     *                   or a different list) when they are removed from this list. When
     *                   set to false, the finger is removed.
     */
    public DoublyLinkedList(boolean keepFinger) {
        this.setKeepFinger(keepFinger);
    }

    /**
     * Default constructor. Creates a new doubly linked list that removes the finger
     * from removed entries and creates a new iterator object for each call to
     * <code>iterator()</code>.
     */
    public DoublyLinkedList() {
        this.setKeepFinger(false);
    }

    /**
     * Appends an entry to the end of the list if it is not already contained in the
     * list.
     *
     * <b>NOTE:</b> An entry can be present at most once per list.
     *
     * @param entry The entry to be added
     * @return True if the entry was added, false if it was already contained in the
     * list.
     */
    public boolean append(E entry) {
        return this.addAfter(entry, this.getTail());
    }

    /**
     * Adds an entry after another entry already in the list.
     *
     * @param entry The entry to be added
     * @param after The entry after which the new entry is added
     * @return True if the entry was added, false if it was already contained in the
     * list.
     * @throws DoublyLinkedListErrorException if <code>after</code> is not contained in the list.
     */
    public boolean addAfter(E entry, E after) throws DoublyLinkedListErrorException {
        DoublyLinkedListEntry.Finger pos = after.getDoublyLinkedListFinger().getFinger(this);
        if (pos == null || (pos.getNext() == null && pos.getPrevious() == null)) {
            throw new DoublyLinkedListErrorException(
                    "Cannot add an element into doubly linked list after an element which is not contained in the list.");
        }
        return this.addAfter(entry, pos);
    }

    /**
     * Adds an entry after a given finger.
     *
     * @param entry The entry to be added
     * @param pos   The finger after which this entry is added
     * @return True if the entry was added, false if it was already contained in the
     * list.
     */
    private boolean addAfter(E entry, DoublyLinkedListEntry.Finger pos) {
        DoublyLinkedListEntry.Finger f = entry.getDoublyLinkedListFinger().getFinger(this);
        if (f != null) {
            return false; // already in list
        }
        f = entry.getDoublyLinkedListFinger().getNewFinger(this, entry); // get new finger
        if (pos == this.getTail()) { // insert at the end
            f.setPrevious(this.getTail());
            this.getTail().setNext(f);
            this.setTail(f);
        } else { // insert not after last entry
            f.setNext(pos.getNext());
            f.setPrevious(pos);
            pos.getNext().setPrevious(f); // must exist, as pos != tail
            pos.setNext(f);
        }
        this.setSize(this.getSize() + 1);
        this.setModCount(this.getModCount() + 1);
        return true;
    }

    /**
     * Adds an entry before another entry already in the list.
     *
     * @param entry  The entry to be added
     * @param before The entry before which the new entry is added
     * @return True if the entry was added, false if it was already contained in the
     * list.
     * @throws DoublyLinkedListErrorException if <code>before</code> is not contained in the list.
     */
    public boolean addBefore(E entry, E before) throws DoublyLinkedListErrorException {
        DoublyLinkedListEntry.Finger pos = before.getDoublyLinkedListFinger().getFinger(this);
        if (pos == null || (pos.getNext() == null && pos.getPrevious() == null)) {
            throw new DoublyLinkedListErrorException(
                    "Cannot add an element into doubly linked list before an element which is not contained in the list.");
        }
        return this.addBefore(entry, pos);
    }

    private boolean addBefore(E entry, DoublyLinkedListEntry.Finger pos) {
        DoublyLinkedListEntry.Finger f = entry.getDoublyLinkedListFinger().getFinger(this);
        if (f != null) {
            return false; // already in list
        }
        f = entry.getDoublyLinkedListFinger().getNewFinger(this, entry); // get new finger
        if (pos == this.getHead()) { // insert in front (actually, we don't insert BEFORE the head, but after the
            // head)
            f.setNext(this.getHead().getNext());
            f.setPrevious(this.getHead());
            if (this.getHead() != this.getTail()) { // not empty list
                this.getHead().getNext().setPrevious(f);
            } else {
                this.setTail(f);
            }
            this.getHead().setNext(f);
        } else { // insert not before first entry
            f.setNext(pos);
            f.setPrevious(pos.getPrevious());
            pos.getPrevious().setNext(f);
            pos.setPrevious(f);
        }
        this.setSize(this.getSize() + 1);
        this.setModCount(this.getModCount() + 1);
        return true;
    }

    /**
     * Removes an entry from this list.
     *
     * @param entry The entry to be removed from this list.
     * @return True if the entry was in the list, otherwise false.
     */
    public boolean remove(E entry) {
        DoublyLinkedListEntry.Finger f = entry.getDoublyLinkedListFinger().getFinger(this);
        return this.remove(f);
    }

    /**
     * Same as remove, but with different arguments.
     *
     * @param entry The entry to be removed from this list.
     * @return True if the entry was in the list, otherwise false.
     */
    private boolean remove2(DoublyLinkedListEntry entry) {
        DoublyLinkedListEntry.Finger f = entry.getDoublyLinkedListFinger().getFinger(this);
        return this.remove(f);
    }

    /**
     * Removes an entry given its finger from this list.
     *
     * @param f The finger of the entry
     * @return True if the entry was in this list, otherwise false.
     */
    private boolean remove(DoublyLinkedListEntry.Finger f) {
        if (f == null) {
            return false; // not in list and no finger
        }
        if (f.getNext() == null && f.getPrevious() == null) {
            f.getObject().getDoublyLinkedListFinger().releaseFinger(f, this.isKeepFinger());
            return false; // not in list, but had a dummy finger.
        }
        f.getPrevious().setNext(f.getNext()); // there's always a previous
        if (f.getNext() != null) {
            f.getNext().setPrevious(f.getPrevious());
        } else { // was last entry
            this.setTail(f.getPrevious());
        }
        f.getObject().getDoublyLinkedListFinger().releaseFinger(f, this.isKeepFinger());
        this.setSize(this.getSize() - 1);
        this.setModCount(this.getModCount() + 1);
        return true;
    }

    /**
     * Removes and returns the first entry of the list.
     *
     * @return The first entry of the list and removes it. Null if the list is
     * empty.
     */
    @SuppressWarnings("unchecked")
    public E pop() {
        if (this.getHead().getNext() != null) {
            DoublyLinkedListEntry e = this.getHead().getNext().getObject();
            this.remove(this.getHead().getNext());
            return (E) e;
        }
        return null;
    }

    /**
     * Returns the first entry of the list
     *
     * @return The first entry of the list, null if the list is empty.
     */
    @SuppressWarnings("unchecked")
    public E peek() {
        if (this.getHead().getNext() != null) {
            return (E) this.getHead().getNext().getObject();
        }
        return null;
    }

    /**
     * Inserts an entry at the beginning of the list.
     *
     * @param entry The entry to be added.
     * @return True if the entry was added to the list, false if the entry was
     * already contained in the list.
     */
    public boolean push(E entry) {
        return this.addBefore(entry, this.getHead()); // note that this does not insert the element BEFORE the special entry 'head',
        // but after head as first elelemtn of the list.
    }

    /**
     * @return The number of entries in this list.
     */
    public int size() {
        return this.getSize();
    }

    /**
     * Returns true if the list is empty, otherwise false.
     *
     * @return True if the list is empty, otherwise false.
     */
    public boolean isEmpty() {
        return this.getSize() == 0;
    }

    @Override
    public ReusableListIterator<E> iterator() {
        return new ListItr(0);
    }

    /**
     * Retrieves the element at a given index in the list.
     *
     * @param index The zero-based index of the element to retrieve in the list. E.g.
     *              0 for the first element, 1 for the second, ..., (size-1) for the
     *              last.
     * @return The element at the given index
     * @throws ArrayIndexOutOfBoundsException if the index is negative or not less than the current size of
     *                                        this Vector object. given.
     */
    public E elementAt(int index) throws ArrayIndexOutOfBoundsException {
        for (E e : this) {
            if (index == 0) {
                return e;
            }
            index--;
        }
        throw new ArrayIndexOutOfBoundsException(
                Logging.getCodePosition() + " Invalid index: index=" + index + " size of list=" + this.getSize());
    }

    /**
     * Removes and returns the element at a given index in the list.
     *
     * @param index The zero-based index of the element to retrieve in the list. E.g.
     *              0 for the first element, 1 for the second, ..., (size-1) for the
     *              last.
     * @return The element at the given index
     * @throws ArrayIndexOutOfBoundsException if the index is negative or not less than the current size of
     *                                        this Vector object. given.
     */
    public E remove(int index) throws ArrayIndexOutOfBoundsException {
        E e = this.elementAt(index);
        this.remove(e);
        return e;
    }

    // /**
    // * This method returns a Iterator. It either resets the reusable iterator or
    // creates a
    // * new one depending on the parameters set in the constructor.
    // *
    // * @return An iterator over the list
    // */
    // public ReusableListIterator<E> getIterator(){
    // if(reuseIterator) {
    // return reusableIterator();
    // } else {
    // return newIterator();
    // }
    // }

    // /**
    // * Returns an iterator object associated with this list. Successive calls to
    // this
    // * method return the same iterator object. In each call, the iterator is
    // reset, such
    // * that iteration over all elements is possilbe.
    // * <p>
    // * <b>Note:</b> If this list was initialized with the
    // <code>reuseIterator</code> flag set
    // * to true, a call to <code>getIterator()</code> is equivalent to calling this
    // method.
    // * @return The iterator associated with this list.
    // */
    // public ReusableListIterator<E> reusableIterator() {
    // iterator.reset();
    // return iterator;
    // }

    // /**
    // * Creates and returns a new, independent instance of an iterator for this
    // list.
    // * @return A new iterator instance for this list.
    // */
    // public ReusableListIterator<E> newIterator() {
    // return new ListItr(0);
    // }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        int count = 0;
        for (E e : this) {
            count++;
            s.append(e.toString()).append((count < this.getSize()) ? ", " : "");
        }
        return s + "]";
    }

    /**
     * An iterator implementation, mostly copied from java.util.LinkedList.
     *
     * @see java.util.LinkedList
     */
    private class ListItr implements ReusableListIterator<E> {

        private DoublyLinkedListEntry.Finger lastReturned = DoublyLinkedList.this.getHead();
        private DoublyLinkedListEntry.Finger next; // finger of next element to be returned
        private int nextIndex; // 0-based index of next element to be returned
        private int expectedModCount = DoublyLinkedList.this.getModCount();

        /**
         * Create a new ListItr Object and initialize it such that the next returned
         * element is at position with index, where the index starts with 0 for the
         * first element.
         *
         * @param index The zero-based offset into the list from where the iterator is
         *              initialized.
         */
        private ListItr(int index) {
            if (index < 0 || index > DoublyLinkedList.this.getSize()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + DoublyLinkedList.this.getSize());
            }
            if (index < (DoublyLinkedList.this.getSize() >> 1)) {
                this.next = DoublyLinkedList.this.getHead().getNext();
                for (this.nextIndex = 0; this.nextIndex < index; this.nextIndex++) {
                    this.next = this.next.getNext();
                }
            } else {
                this.next = DoublyLinkedList.this.getTail();
                for (this.nextIndex = DoublyLinkedList.this.getSize() - 1; this.nextIndex > index; this.nextIndex--) {
                    this.next = this.next.getPrevious();
                }
            }
        }

        @Override
        public void reset() {
            this.nextIndex = 0;
            this.expectedModCount = DoublyLinkedList.this.getModCount();
            this.lastReturned = DoublyLinkedList.this.getHead();
            this.next = DoublyLinkedList.this.getHead().getNext();
        }

        @Override
        public boolean hasNext() {
            if (DoublyLinkedList.this.getSize() == 0) {
                return false;
            }
            return this.nextIndex != DoublyLinkedList.this.getSize();
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            this.checkForComodification();
            if (this.nextIndex == DoublyLinkedList.this.getSize()) { // reached end of list.
                throw new NoSuchElementException();
            }

            this.lastReturned = this.next;
            this.next = this.next.getNext();
            this.nextIndex++;
            return (E) this.lastReturned.getObject();
        }

        @Override
        public boolean hasPrevious() {
            return this.nextIndex != 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E previous() {
            if (this.nextIndex == 0) {
                throw new NoSuchElementException();
            }
            if (this.next != null) {
                this.lastReturned = this.next = this.next.getPrevious();
            } else {
                this.lastReturned = this.next = DoublyLinkedList.this.getTail().getPrevious(); // index > 0 => not tail is not head.
            }
            this.nextIndex--;
            this.checkForComodification();
            return (E) this.lastReturned.getObject();
        }

        @Override
        public int nextIndex() { // the (zero-based) index of the next element to be returned.
            return this.nextIndex;
        }

        @Override
        public int previousIndex() { // corresponds to the (zero-based) index of the currently returned element
            return this.nextIndex - 1;
        }

        @Override
        public void remove() {
            this.checkForComodification();
            if (this.lastReturned == DoublyLinkedList.this.getHead()) {
                throw new IllegalStateException();
            }
            DoublyLinkedListEntry.Finger lastNext = this.lastReturned.getNext();
            if (!DoublyLinkedList.this.remove2(this.lastReturned.getObject())) {
                // could not remove the object
                throw new IllegalStateException();
            }
            if (this.next == this.lastReturned) { // when previous() was called before, lastReturned == next
                this.next = lastNext;
            } else {
                this.nextIndex--;
            }
            this.lastReturned = DoublyLinkedList.this.getHead(); // cannot remove twice
            this.expectedModCount++;
        }

        @Override
        public void set(E o) {
            if (this.lastReturned == DoublyLinkedList.this.getHead()) {
                throw new IllegalStateException();
            }
            this.checkForComodification();

            if (o.getDoublyLinkedListFinger().getFinger(DoublyLinkedList.this) != null) {
                throw new IllegalStateException(
                        "Cannot replace the current entry with an entry that is already in the list. This exception occured while iterating over the list.");
            }
            DoublyLinkedListEntry.Finger f = o.getDoublyLinkedListFinger().getNewFinger(DoublyLinkedList.this, o);
            f.setNext(this.lastReturned.getNext());
            f.setPrevious(this.lastReturned.getPrevious());
            if (this.lastReturned.getNext() != null) {
                this.lastReturned.getNext().setPrevious(f);
            }
            this.lastReturned.getPrevious().setNext(f); // there's always a previous
            // release the finger of the old entry
            this.lastReturned.getObject().getDoublyLinkedListFinger().releaseFinger(this.lastReturned, DoublyLinkedList.this.isKeepFinger());
            if (this.lastReturned == this.next) { // restore the pointers
                this.lastReturned = this.next = f;
            } else {
                this.lastReturned = f;
            }
        }

        /**
         * Adds an element to the list if it does not already exist in the list.
         *
         * @param o The element to be inserted.
         * @see java.util.ListIterator#add(Object)
         */
        @Override
        public void add(E o) {
            this.checkForComodification();
            this.lastReturned = DoublyLinkedList.this.getHead();
            if (this.next == null) { // append to the end of the list
                if (DoublyLinkedList.this.append(o)) {
                    this.expectedModCount++;
                }
            } else {
                if (DoublyLinkedList.this.addBefore(o, this.next)) { // returns true if successfully inserted
                    this.nextIndex++;
                    this.expectedModCount++;
                }
            }
        }

        /**
         * Tests whether the list has been modified other than through the iteration
         * commands. If this is the case, the method throws a
         * ConcurrentModificationException.
         *
         * @throws ConcurrentModificationException if this list was modified other than through the iterators
         *                                         methods.
         */
        final void checkForComodification() {
            if (DoublyLinkedList.this.getModCount() != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
