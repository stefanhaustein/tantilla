package org.kobjects.asde.lang;

public class LocalStack {
    private Object[] stack;
    int limit;

    public LocalStack(int initialSize) {
        this.stack = new Object[initialSize];
        limit = initialSize;
    }

    public Object get(int index) {
        if (index >= limit) {
            throw new IndexOutOfBoundsException("limit: " + limit + " index: " + index);
        }
        return stack[index];
    }

    public void set(int index, Object value) {
        if (index >= limit) {
            throw new IndexOutOfBoundsException("limit: " + limit + " index: " + index);
        }
        stack[index] = value;
    }

    public void ensureSize(int i) {
        limit = i;
        if (stack.length < i) {
            Object[] newStack = new Object[Math.max(i, stack.length * 3 / 2)];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
    }
}
