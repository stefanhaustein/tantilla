package org.kobjects.annotatedtext;

import java.util.ArrayList;
import java.util.List;

public class Text {

  public final String title;
  public final Iterable<Section> sections;


  public Text (String title, Iterable<Section> sections) {
    this.title = title;
    this.sections = sections;
  }

  public static class Builder {
    private String title;
    private ArrayList<Section> sections = new ArrayList<>();

    public Text build() {
      return new Text(title, new ArrayList<>(sections));
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder addSection(Section section) {
      sections.add(section);
      return this;
    }

    public Builder addParagraph(CharSequence text) {
      sections.add(new Section(Section.Kind.PARAGRAPH, text));
      return this;
    }

    public Builder addSubtitle(String text) {
      sections.add(new Section(Section.Kind.SUBTITLE, text));
      return this;
    }
  }

}
