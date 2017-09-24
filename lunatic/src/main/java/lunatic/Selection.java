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
