package ca.phon.functor;

/**
 * A functor which operates on type T
 * and returns a value of type R.
 * 
 * @param <R>
 * @param <T>
 */
public interface Functor<R, T> {
	
	public R op(T obj);

}
