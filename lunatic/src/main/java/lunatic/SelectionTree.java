package lunatic;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

class SelectionTree extends SparseIntervalTree<Selection> implements Parcelable {

  SelectionTree() {
    super(Selection.class, null, 0, 0);
  }

  private SelectionTree(int valueCount, Selection[] values, long[] starts, long[] ends) {
    super(Selection.class, valueCount, values, starts, ends);
  }

  Selection[] findByTag(String tag) {
    Selection[] results = new Selection[] {};
    for (Selection s : values) {
      if (s == null) {
        return results;
      }
      if (s.tag.equals(tag)) {
        results = ArrayUtils.append(results, results.length, s);
      }
    }
    return results;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel out, int flags) {
    out.writeInt(valueCount);
    out.writeParcelableArray(Arrays.copyOf(values, valueCount), flags);
    out.writeLongArray(starts);
    out.writeLongArray(ends);
  }

  public static final Creator<SelectionTree> CREATOR = new ClassLoaderCreator<SelectionTree>() {
    @Override public SelectionTree createFromParcel(Parcel source, ClassLoader loader) {
      return new SelectionTree(
          source.readInt(),
          (Selection[]) source.readParcelableArray(loader),
          source.createLongArray(),
          source.createLongArray());
    }

    @Override public SelectionTree createFromParcel(Parcel source) {
      return createFromParcel(source, null);
    }

    @Override public SelectionTree[] newArray(int size) {
      return new SelectionTree[size];
    }
  };
}
