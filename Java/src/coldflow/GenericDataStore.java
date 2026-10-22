package coldflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A reusable, type-safe in-memory store for COLDFlow objects.
 *
 * @param <T> the type of object stored (for example, SensorDatasetReading)
 */
public class GenericDataStore<T> {

    private final List<T> items = new ArrayList<>();

    /** Adds one non-null item. */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "item cannot be null"));
    }

    /** Adds all items from a collection. */
    public void addAll(Collection<? extends T> newItems) {
        Objects.requireNonNull(newItems, "newItems cannot be null");
        for (T item : newItems) {
            add(item);
        }
    }

    /** Returns a read-only view so callers cannot modify the store directly. */
    public List<T> getAll() {
        return Collections.unmodifiableList(items);
    }

    /** Returns items matching a condition, using a generic Predicate. */
    public List<T> findAll(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        return items.stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
