package lunatic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Maintains an interval tree of objects. This allows for efficient lookups of overlapping
 * intervals.
 */
class SparseIntervalTree<T> {

  private Class<? extends T> valueClass;
  T[] values;
  long[] starts;
  long[] ends;
  private long[] max;
  private IdentityHashMap<T, Integer> indexOfValue;
  private int lowWaterMark;
  int valueCount;

  private List<Listener<T>> listeners;

  public interface Listener<T> {

    void onAdded(T value, long start, long end);

    void onChanged(T value, long oldStart, long oldEnd, long newStart, long newEnd);

    void onRemoved(T value, long start, long end);
  }

  static <T> SparseIntervalTree<T> create(Class<T> valueClass) {
    return new SparseIntervalTree<>(valueClass, null, 0, 0);
  }

  SparseIntervalTree(Class<? extends T> valueClass, SparseIntervalTree<T> source, long start,
      long end) {
    this.valueClass = valueClass;

    //noinspection unchecked
    values = (T[]) Array.newInstance(valueClass, 0);

    final long[] empty = new long[] {};
    starts = empty;
    ends = empty;
    max = empty;
    listeners = new ArrayList<>();

    if (source != null) {
      T[] sourceValues = source.find(start, end);

      for (T sourceValue : sourceValues) {
        long st = source.getStart(sourceValue) - start;
        long en = source.getEnd(sourceValue) - start;

        if (st < 0) {
          st = 0;
        }
        if (st > end - start) {
          st = end - start;
        }

        if (en < 0) {
          en = 0;
        }
        if (en > end - start) {
          en = end - start;
        }

        set(false, sourceValue, st, en);
      }
      restoreInvariants();
    }
  }

  SparseIntervalTree(Class<? extends T> valueClass, int valueCount, T[] values, long[] starts,
      long[] ends) {
    this.valueClass = valueClass;
    this.valueCount = valueCount;
    this.values = values;
    this.starts = starts;
    this.ends = ends;

    max = new long[2 * treeRoot() + 1];
    listeners = new ArrayList<>();

    restoreInvariants();
  }

  int size() {
    return valueCount;
  }

  void set(T what, long start, long end) {
    set(true, what, start, end);
  }

  void remove(T what) {
    if (indexOfValue == null) {
      return;
    }

    Integer i = indexOfValue.remove(what);
    if (i != null) {
      remove(i);
    }
  }

  void clear() {
    for (int i = valueCount - 1; i >= 0; i--) {
      T what = values[i];
      long start = starts[i];
      long end = ends[i];

      valueCount = i;
      values[i] = null;

      notifyRemoved(what, start, end);
    }
    if (indexOfValue != null) {
      indexOfValue.clear();
    }
  }

  T[] find(long queryStart, long queryEnd) {
    if (valueCount == 0) {
      //noinspection unchecked
      return (T[]) Array.newInstance(valueClass, 0);
    }
    int count = count(queryStart, queryEnd, treeRoot());
    if (count == 0) {
      //noinspection unchecked
      return (T[]) Array.newInstance(valueClass, 0);
    }

    //noinspection unchecked
    T[] ret = (T[]) Array.newInstance(valueClass, count);
    getValuesRecursive(queryStart, queryEnd, treeRoot(), ret, 0);
    return ret;
  }

  private long getStart(T what) {
    if (indexOfValue == null) {
      return -1;
    }
    Integer i = indexOfValue.get(what);
    return i == null ? -1 : starts[i];
  }

  private long getEnd(T what) {
    if (indexOfValue == null) {
      return -1;
    }
    Integer i = indexOfValue.get(what);
    return i == null ? -1 : ends[i];
  }

  SparseIntervalTree<T> slice(long start, long end) {
    return new SparseIntervalTree<>(valueClass, this, start, end);
  }

  void addListener(Listener<T> listener) {
    listeners.add(listener);
  }

  void removeListener(Listener<T> listener) {
    listeners.remove(listener);
  }

  protected void set(boolean notify, T what, long start, long end) {
    if (indexOfValue != null) {
      Integer index = indexOfValue.get(what);
      if (index != null) {
        int i = index;
        long oldStart = starts[i];
        long oldEnd = ends[i];
        starts[i] = start;
        ends[i] = end;

        if (notify) {
          restoreInvariants();
          notifyChanged(what, oldStart, oldEnd, start, end);
        }

        return;
      }
    }

    values = ArrayUtils.append(values, valueCount, what);
    starts = ArrayUtils.append(starts, valueCount, start);
    ends = ArrayUtils.append(ends, valueCount, end);
    invalidateIndex(valueCount);
    valueCount++;

    // Make sure there is enough room for empty interior nodes.
    // This magic formula computes the size of the smallest perfect binary
    // tree no smaller than valueCount.
    int sizeOfMax = 2 * treeRoot() + 1;
    if (max.length < sizeOfMax) {
      max = new long[sizeOfMax];
    }

    if (notify) {
      restoreInvariants();
      notifyAdded(what, start, end);
    }
  }

  private void remove(int i) {
    T value = values[i];

    long start = starts[i];
    long end = ends[i];

    int count = valueCount - (i + 1);
    System.arraycopy(values, i + 1, values, i, count);
    System.arraycopy(starts, i + 1, starts, i, count);
    System.arraycopy(ends, i + 1, ends, i, count);

    valueCount--;

    invalidateIndex(i);
    values[valueCount] = null;

    restoreInvariants();

    notifyRemoved(value, start, end);
  }

  private int count(long queryStart, long queryEnd, int i) {
    int count = 0;
    if ((i & 1) != 0) {
      // Internal tree node
      int left = leftChild(i);
      long m = max[left];
      if (m >= queryStart) {
        count = count(queryStart, queryEnd, left);
      }
    }
    if (i < valueCount) {
      long start = starts[i];
      if (start <= queryEnd) {
        long end = ends[i];
        if (end >= queryStart && (start == end || queryStart == queryEnd || (start != queryEnd
            && end != queryStart))) {
          count++;
        }
        if ((i & 1) != 0) {
          count += count(queryStart, queryEnd, rightChild(i));
        }
      }
    }
    return count;
  }

  /**
   * Fills the result array with the values found under the current interval tree node.
   *
   * @param queryStart Start index for the interval query.
   * @param queryEnd End index for the interval query.
   * @param i Index of the current tree node.
   * @param ret Array to be filled with results.
   * @param count The number of found values.
   * @return The total number of values found.
   */
  private int getValuesRecursive(long queryStart, long queryEnd, int i, T[] ret, int count) {
    if ((i & 1) != 0) {
      // Internal tree node
      int left = leftChild(i);
      long m = max[left];
      if (m >= queryStart) {
        count = getValuesRecursive(queryStart, queryEnd, left, ret, count);
      }
    }
    if (i >= valueCount) {
      return count;
    }
    long start = starts[i];
    if (start <= queryEnd) {
      long end = ends[i];
      if (end >= queryStart && (start == end || queryStart == queryEnd || (start != queryEnd
          && end != queryStart))) {
        ret[count] = values[i];
        count++;
      }
      if (count < ret.length && (i & 1) != 0) {
        count = getValuesRecursive(queryStart, queryEnd, rightChild(i), ret, count);
      }
    }
    return count;
  }

  private void notifyAdded(T what, long start, long end) {
    for (Listener<T> listener : listeners) {
      listener.onAdded(what, start, end);
    }
  }

  private void notifyChanged(T what, long oldStart, long oldEnd, long newStart, long newEnd) {
    for (Listener<T> listener : listeners) {
      listener.onChanged(what, oldStart, oldEnd, newStart, newEnd);
    }
  }

  private void notifyRemoved(T what, long start, long end) {
    for (Listener<T> listener : listeners) {
      listener.onRemoved(what, start, end);
    }
  }

  // Primitives for treating object list as binary tree

  // The objects (along with start and end offsets) are stored in linear arrays sorted
  // by start offset. For fast searching, there is a binary search structure imposed over these
  // arrays. This structure is inorder traversal of a perfect binary tree, a slightly unusual
  // but advantageous approach.

  // The value-containing nodes are indexed 0 <= i < n (where n = valueCount), thus preserving
  // logic that accesses the values as a contiguous array. Other balanced binary tree approaches
  // (such as a complete binary tree) would require some shuffling of node indices.

  // Basic properties of this structure: For a perfect binary tree of height m:
  // The tree has 2^(m+1) - 1 total nodes.
  // The root of the tree has indexOfValue 2^m - 1.
  // All leaf nodes have even indexOfValue, all interior nodes odd.
  // The height of a node of indexOfValue i is the number of trailing ones in i's binary representation.
  // The left child of a node i of height h is i - 2^(h - 1).
  // The right child of a node i of height h is i + 2^(h - 1).

  // Note that for arbitrary n, interior nodes of this tree may be >= n. Thus, the general
  // structure of a recursive traversal of node i is:
  // * traverse left child if i is an interior node
  // * process i if i < n
  // * traverse right child if i is an interior node and i < n

  private int treeRoot() {
    return Integer.highestOneBit(valueCount) - 1;
  }

  private static int leftChild(int i) {
    return i - (((i + 1) & ~i) >> 1);
  }

  private static int rightChild(int i) {
    return i + (((i + 1) & ~i) >> 1);
  }

  private long calcMax(int i) {
    long m = 0;
    if ((i & 1) != 0) {
      // Internal tree node
      m = calcMax(leftChild(i));
    }
    if (i < valueCount) {
      m = Math.max(m, ends[i]);
      if ((i & 1) != 0) {
        m = Math.max(m, calcMax(rightChild(i)));
      }
    }
    max[i] = m;
    return m;
  }

  private void restoreInvariants() {
    if (valueCount == 0) {
      return;
    }

    // Invariant 1: starts are non-decreasing
    for (int i = 1; i < valueCount; i++) {
      if (starts[i] < starts[i - 1]) {
        T value = values[i];
        long start = starts[i];
        long end = ends[i];
        int j = i;
        do {
          values[j] = values[j - 1];
          starts[j] = starts[j - 1];
          ends[j] = ends[j - 1];
          j--;
        } while (j > 0 && start < starts[j - 1]);
        values[j] = value;
        starts[j] = start;
        ends[j] = end;
        invalidateIndex(j);
      }
    }

    // Invariant 2: max is max end for each node and its descendants
    calcMax(treeRoot());

    // Invariant 3: indexOfValue maps values back to indices
    if (indexOfValue == null) {
      indexOfValue = new IdentityHashMap<>();
    }
    for (int i = lowWaterMark; i < valueCount; i++) {
      Integer existing = indexOfValue.get(values[i]);
      if (existing == null || existing != 1) {
        indexOfValue.put(values[i], i);
      }
    }
    lowWaterMark = Integer.MAX_VALUE;
  }

  private void invalidateIndex(int i) {
    lowWaterMark = Math.min(i, lowWaterMark);
  }
}
