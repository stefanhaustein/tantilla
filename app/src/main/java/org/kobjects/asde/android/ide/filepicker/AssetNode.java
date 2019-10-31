package org.kobjects.asde.android.ide.filepicker;

import android.content.res.AssetManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssetNode implements Node {

  private final AssetManager assetManager;
  private String path;
  private String name;
  private Node[] extraNodes;

  public AssetNode(AssetManager assetManager, String name, String path, Node... extraNodes) {
    this.assetManager = assetManager;
    this.name = name;
    this.path = path;
    this.extraNodes = extraNodes;
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    return "file:///android_asset/" + path;
  }

  @Override
  public boolean isLeaf() {
    try {
      return assetManager.list(path).length == 0;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Node> getChildren() {
    try {
      ArrayList<Node> result = new ArrayList<>();
      for (String child : assetManager.list(path)) {
        result.add(new AssetNode(assetManager, child, path + "/" + child));
      }
      for (Node extra : extraNodes) {
        result.add(extra);
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
