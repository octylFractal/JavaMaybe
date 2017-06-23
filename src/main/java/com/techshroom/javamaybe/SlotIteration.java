package com.techshroom.javamaybe;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

public final class SlotIteration<T> implements Iterable<List<T>> {

    public static final class Builder<T> {

        private final ImmutableListMultimap.Builder<Integer, T> map = ImmutableListMultimap.builder();

        public Builder<T> addItemToSlot(T item, int slot) {
            map.put(slot, item);
            return this;
        }

        public Builder<T> addItemsToSlot(Iterable<T> items, int slot) {
            map.putAll(slot, items);
            return this;
        }

        public SlotIteration<T> build() {
            return new SlotIteration<>(map.build());
        }

    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final Map<Integer, List<T>> slots;

    private SlotIteration(ImmutableListMultimap<Integer, T> slots) {
        this.slots = Multimaps.asMap(slots);
    }

    public Map<Integer, List<T>> getSlots() {
        return this.slots;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new AbstractIterator<List<T>>() {

            private final int[] indexes = new int[slots.size()];
            private boolean done;

            @Override
            protected List<T> computeNext() {
                if (done) {
                    return endOfData();
                }
                ImmutableList.Builder<T> out = ImmutableList.builder();
                for (int i = 0; i < indexes.length; i++) {
                    out.add(slots.get(i).get(indexes[i]));
                }

                if (!incrementNext()) {
                    done = true;
                }

                return out.build();
            }

            private boolean incrementNext() {
                for (int i = 0; true; i++) {
                    if (i >= indexes.length) {
                        return false;
                    }
                    indexes[i]++;
                    if (indexes[i] >= slots.get(i).size()) {
                        // reset current index to zero
                        indexes[i] = 0;
                        // continue loop to increment next
                        continue;
                    }
                    return true;
                }
            }
        };
    }

}
