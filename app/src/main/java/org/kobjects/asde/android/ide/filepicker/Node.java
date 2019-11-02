package org.kobjects.asde.android.ide.filepicker;

import java.util.List;

public interface Node {
  String getName();
  String getUrl();
  boolean isLeaf();
  List<Node> getChildren();
  boolean isWritable();
  Node createChild(boolean leaf, String name);
  void delete();
}
