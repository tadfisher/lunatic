package lunatic;

import android.os.Parcel;
import android.os.Parcelable;

class Selection implements Parcelable {

  enum Op {ADD, CHANGE, REMOVE, SHOW}

  final String tag;
  final Interval interval;
  final Highlight highlight;

  Selection(String tag, Interval interval, Highlight highlight) {
    this.tag = tag;
    this.interval = interval;
    this.highlight = highlight;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Selection selection = (Selection) o;

    if (!tag.equals(selection.tag)) return false;
    if (!interval.equals(selection.interval)) return false;
    return highlight.equals(selection.highlight);
  }

  @Override public int hashCode() {
    int result = tag.hashCode();
    result = 31 * result + interval.hashCode();
    result = 31 * result + highlight.hashCode();
    return result;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel out, int flags) {
    out.writeString(tag);
    out.writeParcelable(interval, flags);
    out.writeParcelable(highlight, flags);
  }

  public static final Creator<Selection> CREATOR = new ClassLoaderCreator<Selection>() {
    @Override public Selection createFromParcel(Parcel source, ClassLoader loader) {
      return new Selection(
          source.readString(),
          source.readParcelable(loader),
          source.readParcelable(loader));
    }

    @Override public Selection createFromParcel(Parcel source) {
      return createFromParcel(source, null);
    }

    @Override public Selection[] newArray(int size) {
      return new Selection[size];
    }
  };
}
