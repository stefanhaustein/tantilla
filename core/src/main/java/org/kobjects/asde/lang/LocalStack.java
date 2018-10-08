package org.kobjects.asde.lang;

public class LocalStack {

    int frameStart;
    int frameEnd;

    Object[] stack = new Object[65536];

    public void setParam(int i, Object object) {
        stack[frameEnd + i] = object;
    }

    public void frame(int count) {
        frameStart = frameEnd;
        frameEnd = frameStart + count;
    }

    public void setLocal(int i, Object object) {
        stack[frameStart + i] = object;
    }

    public Object getLocal(int i) {
        return stack[frameStart + i];
    }


}
