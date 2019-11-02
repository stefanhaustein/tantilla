package org.kobjects.asde.android.ide.filepicker;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
  @Override
  public int compare(Node node1, Node node2) {
    if (node1.isLeaf() != node2.isLeaf()) {
      return node1.isLeaf() ? 1 : -1;
    }
    return node1.getName().compareTo(node2.getName());
  }
}
