package org.kobjects.markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Text {

  public final String title;
  public final Iterable<Section> sections;
  public final SortedMap<String, String> metadata;

  public Text (String title, Iterable<Section> sections, Map<String, String> metadata) {
    this.title = title;
    this.sections = sections;
    this.metadata = Collections.unmodifiableSortedMap(new TreeMap<>(metadata));
  }

  public static class Builder {
    private String title;
    private ArrayList<Section> sections = new ArrayList<>();
    private HashMap<String, String> metadata = new HashMap<>();

    public Text build() {
      return new Text(title, new ArrayList<>(sections), metadata);
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

    public void addMetadata(String key, String value) {
      metadata.put(key, value);
    }
  }

}
