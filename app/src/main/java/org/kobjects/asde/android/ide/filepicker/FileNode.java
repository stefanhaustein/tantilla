package org.kobjects.asde.android.ide.filepicker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileNode implements Node {
  private final String name;
  private final File file;

  public FileNode(String name, File file) {
    this.file = file;
    this.name = name;
  }

  FileNode(File file) {
    this.file = file;
    this.name = file.getName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    return "file://" + file.getAbsolutePath();
  }

  @Override
  public boolean isLeaf() {
    return !file.isDirectory();
  }

  @Override
  public List<Node> getChildren() {
    ArrayList<Node> result = new ArrayList<>();
    File[] files = file.listFiles();
    if (files != null) {
      for (File child : file.listFiles()) {
        result.add(new FileNode(child));
      }
      Collections.sort(result, new NodeComparator());
    }
    return result;
  }

  @Override
  public boolean isWritable() {
    return file.canWrite();
  }

  @Override
  public Node createChild(boolean leaf, String childName) {
    File childFile = new File(file, childName);
    if (leaf) {
      try {
        childFile.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      childFile.mkdir();
    }
    return new FileNode(childFile);
  }

  @Override
  public void delete() {
    file.delete();
  }

  public File getFile() {
    return file;
  }
}
