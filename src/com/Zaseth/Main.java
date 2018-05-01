package com.Zaseth;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.UTFDataFormatException;

class ByteArrayJava {
    private byte[] data;
    private int position;
    private int BUFFER_SIZE = 1024;
    private boolean endian;
    private boolean BIG_ENDIAN = true;
    private boolean LITTLE_ENDIAN = false;
    /*
    Constructors
     */
    public ByteArrayJava(ByteArrayJava buff) {
        if (buff instanceof ByteArrayJava) {
            this.data = buff.data;
        }
        this.position = 0;
        this.endian = this.BIG_ENDIAN;
    }
    public ByteArrayJava(int length) {
        this.data = new byte[length];
        this.position = 0;
        this.endian = this.BIG_ENDIAN;
    }
    public ByteArrayJava() {
        this.BUFFER_SIZE = this.BUFFER_SIZE;
        this.data = new byte[this.BUFFER_SIZE];
        this.position = 0;
        this.endian = this.BIG_ENDIAN;
    }
    /*
    Set, get and constructor functions
     */
    public void clear() {
        this.position = 0;
        this.data = new byte[this.BUFFER_SIZE];
    }
    public void setEndian(boolean e) {
        this.endian = e;
    }
    public int moveLeft(int v) {
        return this.position -= v;
    }
    public int moveRight(int v) {
        return this.position += v;
    }
    public void grow(int what, int by) {
        this.BUFFER_SIZE = Math.max(this.position + by, this.BUFFER_SIZE);
    }
    public int length() {
        return this.data == null ? 0 : this.data.length;
    }
    /*
    Data retrieval functions
     */
    public int bytesAvailable() {
        int value = this.length() - this.position;
        if (value > this.length() || value < 0) {
            return 0;
        }
        if (this.position >= this.length()) {
            return -1;
        }
        return value;
    }
    private String arrayToString() {
        System.out.print("Bytes available: " + this.bytesAvailable() + "\r\nPosition: " + this.position + "\r\nByte stream: ");
        return Arrays.toString(this.data).substring(0, 120);
    }
    /*
    Extra method functions
     */
    public byte atomicCompareAndSwapIntAt(int byteIndex, int expectedValue, int newValue) {
    	byte value = this.data[byteIndex];
    	if (value == expectedValue) {
    		this.data[byteIndex] = (byte) newValue;
    	}
    	return value;
    }
    public int atomicCompareAndSwapLength(int expectedLength, int newLength) {
        int prevLength = this.length();
        if (prevLength != expectedLength) {
            return prevLength;
        }
        if (prevLength < newLength) {
            List list = new ArrayList(Arrays.asList(this.data));
            list.addAll(Arrays.asList(newLength - prevLength));
            Object[] c = list.toArray();
            System.out.print(Arrays.toString(c));
        }
        if (prevLength > newLength) {
            this.data = Arrays.copyOfRange(this.data,newLength - 1, prevLength - 1);
        }
        return prevLength;
    }
    /*
    Help functions
     */
    public static String fromCharCode(int... codePoints) { // https://stackoverflow.com/a/2946081/6636193
        StringBuilder builder = new StringBuilder(codePoints.length);
        for (int codePoint : codePoints) {
            builder.append(Character.toChars(codePoint));
        }
        return builder.toString();
    }
    private void checkInt(int value, int offset, int ext, int max, int min) {
        this.bytesAvailable();
        if (value > max || value < min) {
            throw new ArrayIndexOutOfBoundsException("Value argument is out of bounds");
        }
        if (offset + ext > this.length()) {
            throw new ArrayIndexOutOfBoundsException("Index argument is out of range");
        }
    }
    /*
    Writing int and uint functions
     */
    public void writeInt8(int v) {
        v = +v;
        this.checkInt(v, this.position, 1, 0x7f, -0x80);
        if (v < 0) v = 0xff + v + 1;
        this.data[this.position++] = (byte) (v & 0xff);
    }
    public void writeInt16(int v) {
        v = +v;
        this.checkInt(v, this.position, 2, 0x7fff, -0x8000);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
        }
    }
    public void writeInt24(int v) {
        v = +v;
        this.checkInt(v, this.position, 3, 0x7fffff, -0x800000);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
        }
    }
    public void writeInt29(int v) {
        if (v <- 0x10000000 || v > 0x0fffffff) {
            throw new IllegalArgumentException("Integer must be between -0x10000000 and 0x0fffffff but got " + v + " instead");
        }
        v += v < 0 ? 0x20000000 : 0;
        if (v > 0x1fffff) {
            v >>= 1;
            this.writeInt8(0x80 | ((v >> 21) & 0xff));
        }
        if (v > 0x3fff) {
            this.writeInt8(0x80 | ((v >> 14) & 0xff));
        }
        if (v > 0x7f) {
            this.writeInt8(0x80 | ((v >> 7) & 0xff));
        }
        if (v > 0x1fffff) {
            this.writeInt8(v & 0xff);
        } else {
            this.writeInt8(v & 0x7f);
        }
    }
    public void writeInt32(int v) {
        v = +v;
        this.checkInt(v, this.position, 4, 0x7fffffff, -0x80000000);
        if (this.endian) {
            if (v < 0) v = 0xffffffff + v + 1;
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
        }
    }
    public void writeInt40(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
        }
    }
    public void writeInt48(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 40);
        }
    }
    public void writeInt56(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 48);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 48);
        }
    }
    public void writeInt64(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >> 56);
            this.data[this.position++] = (byte) (v >> 48);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >> 8);
            this.data[this.position++] = (byte) (v >> 16);
            this.data[this.position++] = (byte) (v >> 24);
            this.data[this.position++] = (byte) (v >> 32);
            this.data[this.position++] = (byte) (v >> 40);
            this.data[this.position++] = (byte) (v >> 48);
            this.data[this.position++] = (byte) (v >> 56);
        }
    }

    public void writeUInt8(int v) {
        v = +v;
        this.checkInt(v, this.position, 1, 0xff, 0);
        this.data[this.position++] = (byte) (v & 0xff);
    }
    public void writeUInt16(int v) {
        v = +v;
        this.checkInt(v, this.position, 2, 0xffff, 0);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
        }
    }
    public void writeUInt24(int v) {
        v = +v;
        this.checkInt(v, this.position, 3, 0xffffff, 0);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
        }
    }
    public void writeUInt29(int v) {
        if (128 > v) {
            this.writeUInt8(v);
        } else if (16384 > v) {
            this.writeUInt8(v >>> 7 & 127 | 128);
            this.writeUInt8(v & 127);
        } else if (2097152 > v) {
            this.writeUInt8(v >>> 14 & 127 | 128);
            this.writeUInt8(v >>> 7 & 127 | 128);
            this.writeUInt8(v & 127);
        } else if (1073741824 > v) {
            this.writeUInt8(v >>> 22 & 127 | 128);
            this.writeUInt8(v >>> 15 & 127 | 128);
            this.writeUInt8(v >>> 8 & 127 | 128);
            this.writeUInt8(v & 255);
        } else {
            throw new IllegalArgumentException("Integer out of range: " + v);
        }
    }
    public void writeUInt32(int v) {
        v = +v;
        this.checkInt(v, this.position, 4, 0xffffffff, 0);
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
        }
    }
    public void writeUInt40(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
        }
    }
    public void writeUInt48(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 40);
        }
    }
    public void writeUInt56(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 48);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 48);
        }
    }
    public void writeUInt64(long v) {
        v = +v;
        if (this.endian) {
            this.data[this.position++] = (byte) (v >>> 56);
            this.data[this.position++] = (byte) (v >>> 48);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v & 0xff);
        } else {
            this.data[this.position++] = (byte) (v & 0xff);
            this.data[this.position++] = (byte) (v >>> 8);
            this.data[this.position++] = (byte) (v >>> 16);
            this.data[this.position++] = (byte) (v >>> 24);
            this.data[this.position++] = (byte) (v >>> 32);
            this.data[this.position++] = (byte) (v >>> 40);
            this.data[this.position++] = (byte) (v >>> 48);
            this.data[this.position++] = (byte) (v >>> 56);
        }
    }
    /*
    Writing varint and varuint functions
     */
    public void writeSignedVarLong(long value) {
        this.writeUnsignedVarLong((value << 1) ^ (value >> 63));
    }
    public void writeSignedVarInt(int value) {
        this.writeUnsignedVarInt((value << 1) ^ (value >> 31));
    }
    public byte[] writeSignedVarInt(int value) {
        return this.writeUnsignedVarInt((value << 1) ^ (value >> 31));
    }
    public void writeUnsignedVarLong(long value) {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            this.writeInt8(((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }
        this.writeInt8((int) value & 0x7F);
    }
    public void writeUnsignedVarInt(int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            this.writeInt8((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        this.writeInt8(value & 0x7F);
    }
    public byte[] writeUnsignedVarInt(int value) {
        byte[] byteArrayList = new byte[10];
        int i = 0;
        while ((value & 0xFFFFFF80) != 0L) {
            byteArrayList[i++] = ((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        byteArrayList[i] = ((byte) (value & 0x7F));
        byte[] out = new byte[i + 1];
        for (; i >= 0; i--) {
            out[i] = byteArrayList[i];
        }
        return out;
    }
    /*
    Reading varint and varuint functions
     */
    public long readSignedVarLong() {
        long raw = this.readUnsignedVarLong();
        long temp = (((raw << 63) >> 63) ^ raw) >> 1;
        return temp ^ (raw & (1L << 63));
    }
    public int readSignedVarInt() {
        int raw = this.readUnsignedVarInt();
        int temp = (((raw << 31) >> 31) ^ raw) >> 1;
        return temp ^ (raw & (1 << 31));
    }
    public int readSignedVarInt() {
        int raw = this.readUnsignedVarInt();
        int temp = (((raw << 31) >> 31) ^ raw) >> 1;
        return temp ^ (raw & (1 << 31));
    }
    public long readUnsignedVarLong() {
        long value = 0L;
        int i = 0;
        long b;
        while (((b = this.readInt8()) & 0x80L) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 63) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (b << i);
    }
    public int readUnsignedVarInt() {
        int value = 0;
        int i = 0;
        int b;
        while (((b = this.readByte()) & 0x80) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 35) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (b << i);
    }
    public int readUnsignedVarInt() {
        int value = 0;
        int i = 0;
        byte rb = Byte.MIN_VALUE;
        for (byte b : bytes) {
            rb = b;
            if ((b & 0x80) == 0) {
                break;
            }
            value |= (b & 0x7f) << i;
            i += 7;
            if (i > 35) {
                throw new IllegalArgumentException("Variable length quantity is too long");
            }
        }
        return value | (rb << i);
    }
    /*
    Read int and uint functions
     */
    public int readInt8() {
        return this.data[this.position++];
    }
    public int readInt16() {
        if (this.endian) {
            return this.data[this.position++] << 8 | this.data[this.position++];
        } else {
            return this.data[this.position++] | this.data[this.position++] << 8;
        }
    }
    /*
    Write IEEE 754 single-precision (32-bit) and IEEE 754 double-precision (64-bit) functions
     */
    public void writeFloat(float v) {
        this.writeInt32(Float.floatToIntBits(v));
    }
    public void writeDouble(double v) {
        this.writeInt64(Double.doubleToLongBits(v));
    }
    /*
    Reads IEEE 754 single-precision (32-bit) and IEEE 754 double-precision (64-bit) functions
     */
    public double readDouble() {
		return Double.longBitsToDouble(this.readInt64());
	}
	public float readFloat() {
		return Float.intBitsToFloat(this.readInt32());
	}
    /*
    Extra write functions
     */
    public void writeUTF(String s) throws UTFDataFormatException {
        int utfLength = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch > 0 && ch < 0x80) {
                utfLength++;
            } else if (ch == 0 || (ch >= 0x80 && ch < 0x800)) {
                utfLength += 2;
            } else {
                utfLength += 3;
            }
        }
        if (utfLength > 65535) {
            throw new UTFDataFormatException();
        }
        this.writeInt16(utfLength);
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            if (ch > 0 && ch < 0x80) {
                this.writeInt8(ch);
            } else if (ch == 0 || (ch >= 0x80 && ch < 0x800)) {
                this.writeInt8(0xc0 | (0x1f & (ch >> 6)));
                this.writeInt8(0x80 | (0x3f & ch));
            } else {
                this.writeInt8(0xe0 | (0x0f & (ch >> 12)));
                this.writeInt8(0x80 | (0x3f & (ch >> 6)));
                this.writeInt8(0x80 | (0x3f & ch));
            }
        }
    }
    public void writeMultiByte(String v, String charset) {
        Charset cs = Charset.forName(charset);
        if (this.endian) {
            if (charset.equals("UTF-16LE") || charset.equals("UTF-32LE")) {
                throw new IllegalArgumentException("Unmatched charset for current endian"); // Using Big endian but trying to use Little endian charset
            }
        } else {
            if (charset.equals("UTF-16BE") || charset.equals("UTF-32BE")) {
                throw new IllegalArgumentException("Unmatched charset for current endian"); // Using Little endian but trying to use Big endian charset
            }
        }
        this.writeBytes(v.getBytes(cs)); // Converts the string into bytes
    }
    public void writeBytes(byte[] v) {
        int var3 = v.length;
        for(int var4 = 0; var4 < var3; ++var4) {
            byte element$iv = v[var4];
            this.writeInt8(element$iv);
        }
    }
    /*
    Extra read functions
     */
    public String readUTF() throws UTFDataFormatException {
        int utfLength = this.readInt16() & 0xffff;
        int goalPosition = this.position + utfLength;
        StringBuffer string = new StringBuffer(utfLength);
        while (this.position < goalPosition) {
            int a = this.readInt8() & 0xff;
            if ((a & 0x80) == 0) {
                string.append((char)a);
            }
            else {
                int b = this.readInt8() & 0xff;
                if ((b & 0xc0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                if ((a & 0xe0) == 0xc0) {
                    char ch = (char)(((a & 0x1f) << 6) | (b & 0x3f));
                    string.append(ch);
                }
                else if ((a & 0xf0) == 0xe0) {
                    int c = this.readInt8() & 0xff;
                    if ((c & 0xc0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    char ch = (char)(((a & 0x0f) << 12) | ((b & 0x3f) << 6) | (c & 0x3f));
                    string.append(ch);
                }
                else {
                    throw new UTFDataFormatException();
                }
            }
        }
        return string.toString();
    }
    public List readMultiByte(int length) {
        return this.readBytes(length);
    }
    public List readBytes(int length) {
        List array = new ArrayList();
        int nullbyte = 0;
        for (int i = 0; i < length; i++) {
            if (this.data[i] == 0) {
                nullbyte++;
            }
            array.add((char)this.data[i]);
        }
        System.out.print("Nullbytes: " + nullbyte + "\r\n"); // 100% accurate only when writeMultiByte is used.
        return array;
    }

    public static void main(String[] args) throws UTFDataFormatException {
        ByteArrayJava wba = new ByteArrayJava();
        wba.writeInt8(5);
        System.out.print(wba.arrayToString());
    }
}