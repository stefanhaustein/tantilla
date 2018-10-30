package org.kobjects.asde.lang;

public class ProgramReference {
    public final String name;
    public final String url;
    public boolean urlWritable;

    public ProgramReference(String name, String url, boolean urlWritable) {
        this.name = name;
        this.url = url;
        this.urlWritable = urlWritable;
    }
}
