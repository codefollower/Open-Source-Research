    void writeStackMap(Code code) {
    	DEBUG.P(this,"writeStackMap((1)");
    	
        int nframes = code.stackMapBufferSize;
        if (debugstackmap) System.out.println(" nframes = " + nframes);
        databuf.appendChar(nframes);

        switch (code.stackMap) {
        case CLDC:
            for (int i=0; i<nframes; i++) {
                if (debugstackmap) System.out.print("  " + i + ":");
                Code.StackMapFrame frame = code.stackMapBuffer[i];

                // output PC
                if (debugstackmap) System.out.print(" pc=" + frame.pc);
                databuf.appendChar(frame.pc);

                // output locals
                int localCount = 0;
                for (int j=0; j<frame.locals.length;
                     j += (target.generateEmptyAfterBig() ? 1 : Code.width(frame.locals[j]))) {
                    localCount++;
                }
                if (debugstackmap) System.out.print(" nlocals=" +
                                                    localCount);
                databuf.appendChar(localCount);
                for (int j=0; j<frame.locals.length;
                     j += (target.generateEmptyAfterBig() ? 1 : Code.width(frame.locals[j]))) {
                    if (debugstackmap) System.out.print(" local[" + j + "]=");
                    writeStackMapType(frame.locals[j]);
                }

                // output stack
                int stackCount = 0;
                for (int j=0; j<frame.stack.length;
                     j += (target.generateEmptyAfterBig() ? 1 : Code.width(frame.stack[j]))) {
                    stackCount++;
                }
                if (debugstackmap) System.out.print(" nstack=" +
                                                    stackCount);
                databuf.appendChar(stackCount);
                for (int j=0; j<frame.stack.length;
                     j += (target.generateEmptyAfterBig() ? 1 : Code.width(frame.stack[j]))) {
                    if (debugstackmap) System.out.print(" stack[" + j + "]=");
                    writeStackMapType(frame.stack[j]);
                }
                if (debugstackmap) System.out.println();
            }
            break;
        case JSR202: {
            assert code.stackMapBuffer == null;
            for (int i=0; i<nframes; i++) {
                if (debugstackmap) System.out.print("  " + i + ":");
                StackMapTableFrame frame = code.stackMapTableBuffer[i];
                frame.write(this);
                if (debugstackmap) System.out.println();                
            }
            break;
        }
        default:
            throw new AssertionError("Unexpected stackmap format value");
        }
        
        DEBUG.P(0,this,"writeStackMap((1)");
    }