package org.kobjects.asde.lang;

public class ProgramReference {
    public final String name;
    public final String url;
    public boolean urlWritable;

    public static ProgramReference parse(String s) {
        String[] parts = s.split("\n");
        return new ProgramReference(
                parts.length > 0 ? parts[0] : "",
                parts.length > 1 ? parts[1] : "",
                parts.length > 2 && "TRUE".equalsIgnoreCase(parts[2]));
    }

    public ProgramReference(String name, String url, boolean urlWritable) {
        this.name = name;
        this.url = url;
        this.urlWritable = urlWritable;
    }

    @Override
    public String toString() {
        return name + '\n' + url + '\n' + urlWritable + '\n';
    }
}
