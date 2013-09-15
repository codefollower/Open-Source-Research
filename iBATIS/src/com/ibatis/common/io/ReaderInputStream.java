/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE ABOVE NAMED AUTHORS "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS, THEIR
 * PUBLISHER OR THEIR EMPLOYERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ibatis.common.io;

import java.io.*;

/**
 * An InputStream backed by a Reader
 */
public class ReaderInputStream extends InputStream {
    protected Reader reader;
    protected ByteArrayOutputStream byteArrayOut;
    protected Writer writer;
    protected char[] chars;
    protected byte[] buffer;
    protected int index, length;

    /**
     * Constructor to supply a Reader
     *
     * @param reader - the Reader used by the InputStream
     */
    public ReaderInputStream(Reader reader) {
        this.reader = reader;
        byteArrayOut = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(byteArrayOut);
        chars = new char[1024];
    }

    /**
     * Constructor to supply a Reader and an encoding
     *
     * @param reader   - the Reader used by the InputStream
     * @param encoding - the encoding to use for the InputStream
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    public ReaderInputStream(Reader reader, String encoding) throws UnsupportedEncodingException {
        this.reader = reader;
        byteArrayOut = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(byteArrayOut, encoding);
        chars = new char[1024];
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (index >= length)
            fillBuffer();
        if (index >= length)
            return -1;
        return 0xff & buffer[index++];
    }

    protected void fillBuffer() throws IOException {
        if (length < 0)
            return;
        int numChars = reader.read(chars);
        if (numChars < 0) {
            length = -1;
        } else {
            byteArrayOut.reset();
            writer.write(chars, 0, numChars);
            writer.flush();
            buffer = byteArrayOut.toByteArray();
            length = buffer.length;
            index = 0;
        }
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] data, int off, int len) throws IOException {
        if (index >= length)
            fillBuffer();
        if (index >= length)
            return -1;
        int amount = Math.min(len, length - index);
        System.arraycopy(buffer, index, data, off, amount);
        index += amount;
        return amount;
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        return (index < length) ? length - index : ((length >= 0) && reader.ready()) ? 1 : 0;
    }

    /**
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        reader.close();
    }
}
