package org.kobjects.markdown;

public class Section {
  public enum Kind {
    PARAGRAPH, SUBTITLE
  }

  public final Kind kind;
  public final CharSequence text;

  public Section(Kind kind, CharSequence text) {
    this.kind = kind;
    this.text = text;
  }
}
