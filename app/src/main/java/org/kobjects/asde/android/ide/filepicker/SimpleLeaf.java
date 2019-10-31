package org.kobjects.asde.android.ide.filepicker;

import java.util.Collections;
import java.util.List;

public class SimpleLeaf implements Node {
  private final String url;
  private final String name;

  public SimpleLeaf(String name, String url) {
    this.name = name;
    this.url = url;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public List<Node> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  @Override
  public void delete() {
    throw new UnsupportedOperationException();
  }
}
