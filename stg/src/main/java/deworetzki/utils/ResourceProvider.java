package deworetzki.utils;

@FunctionalInterface
public interface ResourceProvider<R, E extends Exception> {

    R get() throws E;

}
