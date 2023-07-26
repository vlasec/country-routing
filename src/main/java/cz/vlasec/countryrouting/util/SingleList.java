package cz.vlasec.countryrouting.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A simple immutable single-linked list inspired by Scala lists.
 * It can be easily prepended, which is pretty much optimal solution to extending the route during search.
 * To protect internals, class is used (rather than record), with nil object used instead of null.
 */
public class SingleList<T> {
    public final T head;
    public final SingleList<T> tail;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final SingleList nil = new SingleList(null, null);

    /** Private constructor to promote factory methods of creation. */
    private SingleList(T head, SingleList<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    /** A proper way to end the list. It represents an empty list. It is used as the terminal object. */
    @SuppressWarnings("unchecked")
    public static <T> SingleList<T> nil() {
        return (SingleList<T>) nil;
    }

    /** Creates a new list that uses existing list as its tail with new head. */
    public SingleList<T> prepend(T newHead) {
        return new SingleList<>(newHead, this);
    }

    /** Prepends current list to provided list. */
    public SingleList<T> concat(SingleList<T> newTail) {
        return reverse().fold(newTail, SingleList::prepend);
    }

    /** Creates a proper Java list copy of itself. */
    public List<T> toList() {
        return reverse().fold(new ArrayList<>(), (list, country) -> {
            list.add(country);
            return list;
        });
    }

    /** Creates a copy of itself in reverse order. */
    public SingleList<T> reverse() {
        return fold(nil(), SingleList::prepend);
    }

    /** Folds the object starting from head. */
    public <X> X fold(X initial, BiFunction<X, T, X> fnc) {
        X result = initial;
        for (var current = this; current != nil; current = current.tail) {
            result = fnc.apply(result, current.head);
        }
        return result;
    }
}
