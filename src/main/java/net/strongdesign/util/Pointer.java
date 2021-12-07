

package net.strongdesign.util;


/**
 * Little helper class for building tree-like structures with upward pointers.
 * @author mark
 *
 * @param <E>
 */
public class Pointer<E> {
	public E value;
	public Pointer<E> pointer;
	
	public Pointer(E value, Pointer<E> pointer) {
		this.value = value;
		this.pointer = pointer;
	}
	
	public static <E>  Pointer<E> getPointer(E value, Pointer<E> pointer) {
		return new Pointer<E>(value, pointer);
	}

}
