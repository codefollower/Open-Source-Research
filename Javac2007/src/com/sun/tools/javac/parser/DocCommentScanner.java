/*
 * @(#)DocCommentScanner.java	1.11 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.parser;

import java.io.*;
import java.nio.*;

import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.util.LayoutCharacters.*;

/** An extension to the base lexical analyzer that captures
 *  and processes the contents of doc comments.  It does so by
 *  translating Unicode escape sequences and by stripping the
 *  leading whitespace and starts from each line of the comment.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class DocCommentScanner extends Scanner {
    private static my.Debug DEBUG=new my.Debug(my.Debug.DocCommentScanner);//我加上的

    /** A factory for creating scanners. */
    public static class Factory extends Scanner.Factory {

	public static void preRegister(final Context context) {
            DEBUG.P(DocCommentScanner.Factory.class,"preRegister(1)");
		
	    context.put(scannerFactoryKey, new Context.Factory<Scanner.Factory>() {
		public Factory make() {
                    try {
                    DEBUG.P(this,"make()");
                    
		    return new Factory(context);
                    
                    } finally {
                    DEBUG.P(0,this,"make()");    
                    }
		}
	    });
	    
	    DEBUG.P(0,DocCommentScanner.Factory.class,"preRegister(1)");
	}

	/** Create a new scanner factory. */
	protected Factory(Context context) {
	    super(context);
	}

        @Override
        public Scanner newScanner(CharSequence input) {
            try {//我加上的
            DEBUG.P(this,"newScanner(1)");
            
            if (input instanceof CharBuffer) {
                return new DocCommentScanner(this, (CharBuffer)input);
            } else {
                char[] array = input.toString().toCharArray();
                return newScanner(array, array.length);
            }
            
            }finally{//我加上的
            DEBUG.P(0,this,"newScanner(1)");
            }
        }
	
        @Override
        public Scanner newScanner(char[] input, int inputLength) {
            try {//我加上的
            DEBUG.P(this,"newScanner(2)");
            
            return new DocCommentScanner(this, input, inputLength);
            
            }finally{//我加上的
            DEBUG.P(0,this,"newScanner(2)");
            }
        }
    }
    
    
    /** Create a scanner from the input buffer.  buffer must implement
     *  array() and compact(), and remaining() must be less than limit().
     */
    protected DocCommentScanner(Factory fac, CharBuffer buffer) {
	super(fac, buffer);
    }
    
    /** Create a scanner from the input array.  The array must have at
     *  least a single character of extra space.
     */
    protected DocCommentScanner(Factory fac, char[] input, int inputLength) {
	super(fac, input, inputLength);
    }
    
    //注意下面的字段都是private，父类Scanner中也有相同的private字段
    
    /** Starting position of the comment in original source
     */
    private int pos;

    /** The comment input buffer, index of next chacter to be read,
     *  index of one past last character in buffer.
     */
    private char[] buf;
    private int bp;
    private int buflen;

    /** The current character.
     */
    private char ch;

    /** The column number position of the current character.
     */
    private int col;

    /** The buffer index of the last converted Unicode character
     */
    private int unicodeConversionBp = 0;

    /**
     * Buffer for doc comment.
     */
    private char[] docCommentBuffer = new char[1024];

    /**
     * Number of characters in doc comment buffer.
     */
    private int docCommentCount;

    /**
     * Translated and stripped contents of doc comment
     */
    private String docComment = null;


    /** Unconditionally expand the comment buffer.
     */
    private void expandCommentBuffer() {
	char[] newBuffer = new char[docCommentBuffer.length * 2];
	System.arraycopy(docCommentBuffer, 0, newBuffer,
			 0, docCommentBuffer.length);
	docCommentBuffer = newBuffer;
    }

    /** Convert an ASCII digit from its base (8, 10, or 16)
     *  to its value.
     */
    private int digit(int base) {
	char c = ch;
	int result = Character.digit(c, base);
	if (result >= 0 && c > 0x7f) {
	    ch = "0123456789abcdef".charAt(result);
	}
	return result;
    }

    /** Convert Unicode escape; bp points to initial '\' character
     *  (Spec 3.3).
     */
    private void convertUnicode() {
	if (ch == '\\' && unicodeConversionBp != bp) {
	    bp++; ch = buf[bp]; col++;
	    if (ch == 'u') {
		do {
		    bp++; ch = buf[bp]; col++;
		} while (ch == 'u');
		int limit = bp + 3;
		if (limit < buflen) {
		    int d = digit(16);
		    int code = d;
		    while (bp < limit && d >= 0) {
			bp++; ch = buf[bp]; col++;
			d = digit(16);
			code = (code << 4) + d;
		    }
		    if (d >= 0) {
			ch = (char)code;
			unicodeConversionBp = bp;
			return;
		    }
		}
		// "illegal.Unicode.esc", reported by base scanner
	    } else {
		bp--;
		ch = '\\';
		col--;
	    }
	}
    }


    /** Read next character.
     */
    //注意：虽然超类Scanner中也有同名的方法，但是因为是private，
    //所以当在调用超类的nextToken方法时，nextToken方法内部调用的scanChar()是Scanner中定
    //义的，而不是这里的scanChar()  (其他同名方法与这里所说的情况类似)
    private void scanChar() {
        //try {//我加上的
	//DEBUG.P(this,"scanChar()");
        
	bp++;
	ch = buf[bp];
	switch (ch) {
	case '\r': // return
	    col = 0;
	    break;
	case '\n': // newline
	    if (bp == 0 || buf[bp-1] != '\r') {
		col = 0;
	    }
	    break;
	case '\t': // tab
	    col = (col / TabInc * TabInc) + TabInc;
	    break;
	case '\\': // possible Unicode
	    col++;
	    convertUnicode();
	    break;
	default:
	    col++;
	    break;
	}
        
        //}finally{//我加上的
        //DEBUG.P(0,this,"scanChar()");
        //}
    }

    /**
     * Read next character in doc comment, skipping over double '\' characters.
     * If a double '\' is skipped, put in the buffer and update buffer count.
     */
    private void scanDocCommentChar() {
	scanChar();
	if (ch == '\\') {
	    if (buf[bp+1] == '\\' && unicodeConversionBp != bp) {
		if (docCommentCount == docCommentBuffer.length)
		    expandCommentBuffer();
	        docCommentBuffer[docCommentCount++] = ch;
		bp++; col++;
	    } else {
		convertUnicode();
	    }
	}
    }

    /* Reset doc comment before reading each new token
     */
    public void nextToken() {
        docComment = null;
	super.nextToken();
        
        /*
        try {//我加上的
	DEBUG.P(this,"nextToken()");
        
	docComment = null;
	super.nextToken();
        
        }finally{//我加上的
        DEBUG.P("docComment="+docComment);
        DEBUG.P(0,this,"nextToken()");
        }*/
    }

    /**
     * Returns the documentation string of the current token.
     */
    public String docComment() {
        return docComment;
    }

    /**
     * Process a doc comment and make the string content available.
     * Strips leading whitespace and stars.
     */
    @SuppressWarnings("fallthrough")
    protected void processComment(CommentStyle style) {
    try {//我加上的
	DEBUG.P(this,"processComment(1)");
	DEBUG.P("style="+style);

	if (style != CommentStyle.JAVADOC) {
            super.processComment(style);//我加上的，方便调试
	    return;
	}

	pos = pos();
	buf = getRawCharacters(pos, endPos());
	buflen = buf.length;
	bp = 0;
	col = 0;
	
	DEBUG.P("pos="+pos());
	DEBUG.P("endPos="+endPos());
	DEBUG.P("buflen="+buflen);
	//DEBUG.P("buf="+new String(buf));

	docCommentCount = 0;

	boolean firstLine = true;
	
	// Skip over first slash
	scanDocCommentChar();
	// Skip over first star
	scanDocCommentChar();

	// consume any number of stars
	while (bp < buflen && ch == '*') {
	    scanDocCommentChar();
	}
	// is the comment in the form /**/, /***/, /****/, etc. ?
	if (bp < buflen && ch == '/') {
	    docComment = "";
	    return;
	}

	// skip a newline on the first line of the comment.
	if (bp < buflen) {
	    if (ch == LF) {
		scanDocCommentChar();
                firstLine = false;
	    } else if (ch == CR) {
		scanDocCommentChar();
		if (ch == LF) {
		    scanDocCommentChar();
                    firstLine = false;
		}
	    }
	}

    outerLoop:

	// The outerLoop processes the doc comment, looping once
	// for each line.  For each line, it first strips off
	// whitespace, then it consumes any stars, then it
	// puts the rest of the line into our buffer.
	while (bp < buflen) {

	    // The wsLoop consumes whitespace from the beginning
	    // of each line.
	wsLoop:

	    while (bp < buflen) {
		switch(ch) {
		case ' ':
		    scanDocCommentChar();
		    break;
		case '\t':
		    col = ((col - 1) / TabInc * TabInc) + TabInc;
		    scanDocCommentChar();
		    break;
 		case FF:
 		    col = 0;
 		    scanDocCommentChar();
 		    break;
// Treat newline at beginning of line (blank line, no star)
// as comment text.  Old Javadoc compatibility requires this.
/*---------------------------------*
 		case CR: // (Spec 3.4)
 		    scanDocCommentChar();
 		    if (ch == LF) {
 			col = 0;
 			scanDocCommentChar();
 		    }
 		    break;
 		case LF: // (Spec 3.4)
 		    scanDocCommentChar();
 		    break;
*---------------------------------*/
		default:
		    // we've seen something that isn't whitespace;
		    // jump out.
		    break wsLoop;
		}
	    }

	    // Are there stars here?  If so, consume them all
	    // and check for the end of comment.
	    if (ch == '*') {
		// skip all of the stars
		do {
		    scanDocCommentChar();
		} while (ch == '*');
		
		// check for the closing slash.
		if (ch == '/') {
		    // We're done with the doc comment
		    // scanChar() and breakout.
		    break outerLoop;
		}
	    } else if (! firstLine) {
                //The current line does not begin with a '*' so we will indent it.
                for (int i = 1; i < col; i++) {
                    if (docCommentCount == docCommentBuffer.length)
			expandCommentBuffer();
                    docCommentBuffer[docCommentCount++] = ' ';
                }
            }
	
	    // The textLoop processes the rest of the characters
	    // on the line, adding them to our buffer.
	textLoop:
	    while (bp < buflen) {
		switch (ch) {
		case '*':
		    // Is this just a star?  Or is this the
		    // end of a comment?
		    scanDocCommentChar();
		    if (ch == '/') {
			// This is the end of the comment,
			// set ch and return our buffer.
			break outerLoop;
		    }
		    // This is just an ordinary star.  Add it to
		    // the buffer.
		    if (docCommentCount == docCommentBuffer.length)
			expandCommentBuffer();
		    docCommentBuffer[docCommentCount++] = '*';
		    break;
		case ' ':
		case '\t':
		    if (docCommentCount == docCommentBuffer.length)
			expandCommentBuffer();
		    docCommentBuffer[docCommentCount++] = ch;
		    scanDocCommentChar();
		    break;
 		case FF:
 		    scanDocCommentChar();
 		    break textLoop; // treat as end of line
 		case CR: // (Spec 3.4)
 		    scanDocCommentChar();
 		    if (ch != LF) {
		        // Canonicalize CR-only line terminator to LF
		        if (docCommentCount == docCommentBuffer.length)
			    expandCommentBuffer();
			docCommentBuffer[docCommentCount++] = (char)LF;
			break textLoop;
		    }
		    /* fall through to LF case */
		case LF: // (Spec 3.4)
		    // We've seen a newline.  Add it to our
		    // buffer and break out of this loop,
		    // starting fresh on a new line.
		    if (docCommentCount == docCommentBuffer.length)
			expandCommentBuffer();
		    docCommentBuffer[docCommentCount++] = ch;
  		    scanDocCommentChar();
  		    break textLoop;
		default:
		    // Add the character to our buffer.
		    if (docCommentCount == docCommentBuffer.length)
			expandCommentBuffer();
		    docCommentBuffer[docCommentCount++] = ch;
		    scanDocCommentChar();
		}
	    } // end textLoop
            firstLine = false;
	} // end outerLoop

	if (docCommentCount > 0) {
	    int i = docCommentCount - 1;
	trailLoop:
	    while (i > -1) {
		switch (docCommentBuffer[i]) {
		case '*':
		    i--;
		    break;
		default:
		    break trailLoop;
		}
	    }
	    docCommentCount = i + 1;
	
	    // Store the text of the doc comment
	    docComment = new String(docCommentBuffer, 0 , docCommentCount);
	} else {
	    docComment = "";
	}
	
	}finally{//我加上的
	DEBUG.P("docComment:");
        DEBUG.P("----------------------------------");
        DEBUG.P(docComment);
        DEBUG.P("----------------------------------");
	DEBUG.P(0,this,"processComment(1)");
	}
	
    }

    /** Build a map for translating between line numbers and
     * positions in the input.
     *
     * @return a LineMap */
    public Position.LineMap getLineMap() {
	//注意这里的buf是Scanner实例的buf数组的拷贝,
	//而在Scanner类中对应的getLineMap()方法里头传给makeLineMap的是同一个buf
        char[] buf = getRawCharacters();
	return Position.makeLineMap(buf, buf.length, true);
    }
}
