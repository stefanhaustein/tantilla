package org.kobjects.asde.lang.runtime;

public class DataArray {

    public void popN(int i) {
        size -= i;
    }

    enum DirectNumberType {
        I32, I64, F32, F64
    }

    int size;
    Object[] objects;
    long[] numbers;

    DataArray(int initialSize) {
        this.objects = new Object[initialSize];
        this.numbers = new long[initialSize];
        size = initialSize;
    }

    void ensureSize(int i) {
        int allocated = objects.length;
        if (allocated < i) {
            int newSize = Math.max(i, allocated * 3 / 2);

            Object[] newObjects = new Object[newSize];
            System.arraycopy(objects, 0, newObjects, 0, size);
            objects = newObjects;

            long[] newNumbers = new long[newSize];
            System.arraycopy(numbers, 0, newNumbers, 0, size);
            numbers = newNumbers;
        }
        size = Math.max(size, i);
    }

    public void copy(int sourceIndex, int targetIndex) {
        objects[targetIndex] = objects[sourceIndex];
        numbers[targetIndex] = numbers[sourceIndex];
    }

    public Object popObject() {
        Object result = getObject(size - 1);
        objects[--size] = null;
        return result;
    }

    public int popI32() {
        int result = getI32(size - 1);
        objects[--size] = null;
        return result;
    }

    public long popI64() {
        long result = getI64(size - 1);
        objects[--size] = null;
        return result;
    }

    public float popF32() {
        float result = getF32(size - 1);
        objects[--size] = null;
        return result;
    }

    public double popF64() {
        double result = getF64(size - 1);
        objects[--size] = null;
        return result;
    }

    public boolean popBoolean() {
        boolean result = getBoolean(size - 1);
        objects[--size] = null;
        return result;
    }

    public int push() {
        if (objects.length > size) {
            return size++;
        }
        ensureSize(size + 1);
        return size - 1;
    }

    public void pushF32(float value) {
        setF32(push(), value);
    }

    public void pushF64(double value) {
        setF64(push(), value);
    }

    public void pushI32(int value) {
        setI32(push(), value);
    }

    public void pushI64(long value) {
        setI64(push(), value);
    }

    public void pushBoolean(boolean b) {
        setBoolean(push(), b);
    }

    public void pushObject(Object o) {
        setObject(push(), o);
    }

    public void setI32(int index, int value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        objects[index] = DirectNumberType.I32;
        numbers[index] = value;
    }

    public void setI64(int index, long value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        objects[index] = DirectNumberType.I64;
        numbers[index] = value;
    }

    public void setF32(int index, float value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        objects[index] = DirectNumberType.F32;
        numbers[index] = Float.floatToRawIntBits(value);
    }

    public void setF64(int index, double value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        objects[index] = DirectNumberType.F64;
        numbers[index] = Double.doubleToRawLongBits(value);
    }

    public void setObject(int index, Object value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        objects[index] = value;
    }

    public void setBoolean(int index, boolean value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        objects[index] = value ? Boolean.TRUE : Boolean.FALSE;
    }

    public int getI32(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Object o = objects[index];
        return o == DirectNumberType.I32 ? (int) numbers[index] : o == Boolean.TRUE ? 1 : o == Boolean.FALSE ? 0 : (int) (Integer) o;
    }

    public long getI64(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Object o = objects[index];
        return o == DirectNumberType.I64 ? numbers[index] : (long) (Long) o;
    }

    public double getF64(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Object o = objects[index];
        return o == DirectNumberType.F64 ? Double.longBitsToDouble(numbers[index]) : (double) (Double) o;
    }

    public float getF32(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Object o = objects[index];
        return o == DirectNumberType.F32 ? Float.intBitsToFloat((int) numbers[index]) : (float) (Float) o;
    }

    public boolean getBoolean(int index) {
        return (Boolean) getObject(index);
    }

    public Object getObject(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Object o = objects[index];
        if (o instanceof DirectNumberType) {
            long n = numbers[index];
            switch ((DirectNumberType) o) {
                case F32:
                    o = Float.intBitsToFloat((int) n);
                    break;
                case I32:
                    o = (int) n;
                    break;
                case F64:
                    o = Double.longBitsToDouble(n);
                    break;
                case I64:
                    o = n;
                    break;
                default:
                    throw new IllegalStateException();
            }
            // Avoid potentially boxing multiple times.
            objects[index] = o;
        }
        return o;
    }

    public int size() {
        return size;
    }

}
