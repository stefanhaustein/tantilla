package org.kobjects.asde.android.ide.filepicker;

import java.util.Arrays;
import java.util.List;

public class SimpleNode implements Node {

  private final String name;
  private final Node[] children;

  public SimpleNode(String name, Node... children) {
    this.name = name;
    this.children = children;
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public List<Node> getChildren() {
    return Arrays.asList(children);
  }

  @Override
  public boolean isWritable() {
    return false;
  }

  @Override
  public Node createChild(boolean leaf, String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete() {
    throw new UnsupportedOperationException();
  }
}
