package org.kobjects.asde.lang;

public class LocalStack {

    int bp;
    int sp;

    Object[] stack = new Object[65536];

    public void push(Object object) {
        stack[sp++] = object;
    }

    public void drop(int count) {
        sp -= count;
    }

    public int frame(int paramCount, int totalLocalCount) {
        int oldBp = bp;
        bp = sp - paramCount;
        sp = bp + totalLocalCount;
        return oldBp;
    }

    public void setLocal(int i, Object object) {
        stack[bp + i] = object;
    }

    public Object getLocal(int i) {
        return stack[bp + i];
    }

    public void release(int oldFrameStrart, int paramCount) {
        sp = bp + paramCount;
        bp = oldFrameStrart;
    }

    public Object getParameter(int i, int of) {
        return stack[sp - of + i];
    }
}
