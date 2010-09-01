package server;

/** Class Pair provides easy way to put together two elements of the same type - type E. 
 * It is immutable by design.
 * @author ivaylo
 * @param <E>
 */
public final class Pair<E> {
	private final E first;
	private final E second;
	
	/**
	 * Constructs new pair of elements.
	 * @param first The first element in the pair.
	 * @param second The second element in the pair.
	 */
	public Pair(E first, E second){
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Gets the first element in the pair.
	 * @return The first element in the pair.
	 */
	public E getFirst(){
		return first;
	} 
	
	/**
	 * Gets the second element in the pair.
	 * @return The second element in the pair.
	 */
	public E getSecond(){
		return second;
	}
}
