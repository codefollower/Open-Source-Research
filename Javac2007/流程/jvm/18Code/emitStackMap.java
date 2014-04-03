/**************************************************************************
 * Stack map generation
 *************************************************************************/

    /** An entry in the stack map. */
    static class StackMapFrame {
		int pc;
		Type[] locals;
		Type[] stack;
    }

    /** A buffer of cldc stack map entries. */
    StackMapFrame[] stackMapBuffer = null;
    
    /** A buffer of compressed StackMapTable entries. */
    StackMapTableFrame[] stackMapTableBuffer = null;
    int stackMapBufferSize = 0;

    /** The last PC at which we generated a stack map. */
    int lastStackMapPC = -1;
    
    /** The last stack map frame in StackMapTable. */
    StackMapFrame lastFrame = null;
    
    /** The stack map frame before the last one. */
    StackMapFrame frameBeforeLast = null;

    /** Emit a stack map entry.  */
    public void emitStackMap() {
		try {//我加上的
		DEBUG.P(this,"emitStackMap()");
		DEBUG.P("needStackMap="+needStackMap);

		int pc = curPc();
		if (!needStackMap) return;
			
		DEBUG.P("pc="+pc);
		DEBUG.P("stackMap="+stackMap);
 
        switch (stackMap) {
            case CLDC:
                emitCLDCStackMap(pc, getLocalsSize());
                break;
            case JSR202:
                emitStackMapFrame(pc, getLocalsSize());
                break;
            default:
                throw new AssertionError("Should have chosen a stackmap format");
		}
		// DEBUG code follows
		if (debugCode) state.dump(pc);
		
		}finally{//我加上的
		DEBUG.P(0,this,"emitStackMap()");
		}
    }
    
    private int getLocalsSize() {
        int nextLocal = 0;
		for (int i=max_locals-1; i>=0; i--) {
			if (state.defined.isMember(i) && lvar[i] != null) {
				//如果一个居部变量是long或double类型，它占用两个数组项，
				//但这个居部变量在居部变量数组中实际只存放在索引号较低的那一项中，
				//另一项留空，但不能被其他居部变量占用
				nextLocal = i + width(lvar[i].sym.erasure(types));
				break;
			}
		}
        return nextLocal;
    }
    
    /** Emit a CLDC stack map frame. */
    void emitCLDCStackMap(int pc, int localsSize) {
		if (lastStackMapPC == pc) {
			// drop existing stackmap at this offset
			stackMapBuffer[--stackMapBufferSize] = null;
		}
		lastStackMapPC = pc;

		if (stackMapBuffer == null) {
			stackMapBuffer = new StackMapFrame[20];
		} else if (stackMapBuffer.length == stackMapBufferSize) {
			StackMapFrame[] newStackMapBuffer =
			new StackMapFrame[stackMapBufferSize << 1];
			System.arraycopy(stackMapBuffer, 0, newStackMapBuffer,
					 0, stackMapBufferSize);
			stackMapBuffer = newStackMapBuffer;
		}
		StackMapFrame frame =
			stackMapBuffer[stackMapBufferSize++] = new StackMapFrame();
		frame.pc = pc;
			
		frame.locals = new Type[localsSize];
		for (int i=0; i<localsSize; i++) {
			if (state.defined.isMember(i) && lvar[i] != null) {
				Type vtype = lvar[i].sym.type;
				if (!(vtype instanceof UninitializedType))
					vtype = types.erasure(vtype);
				frame.locals[i] = vtype;
			}
		}
		frame.stack = new Type[state.stacksize];
		for (int i=0; i<state.stacksize; i++)
			frame.stack[i] = state.stack[i];
    }
    
    void emitStackMapFrame(int pc, int localsSize) {
        if (lastFrame == null) {
            // first frame
            lastFrame = getInitialFrame();
        } else if (lastFrame.pc == pc) {
			// drop existing stackmap at this offset
			stackMapTableBuffer[--stackMapBufferSize] = null;
            lastFrame = frameBeforeLast;
            frameBeforeLast = null;
		}
        
        StackMapFrame frame = new StackMapFrame();
        frame.pc = pc;

		int localCount = 0;
		Type[] locals = new Type[localsSize];
        for (int i=0; i<localsSize; i++, localCount++) {
            if (state.defined.isMember(i) && lvar[i] != null) {
                Type vtype = lvar[i].sym.type;
				if (!(vtype instanceof UninitializedType))
					vtype = types.erasure(vtype);
				locals[i] = vtype;
				if (width(vtype) > 1) i++;
            }
		}
		frame.locals = new Type[localCount];
		for (int i=0, j=0; i<localsSize; i++, j++) {
            assert(j < localCount);
			frame.locals[j] = locals[i];
            if (width(locals[i]) > 1) i++;
		}

		int stackCount = 0;
		for (int i=0; i<state.stacksize; i++) {
            if (state.stack[i] != null) {
                stackCount++;
			}
		}
		frame.stack = new Type[stackCount];
		stackCount = 0;
		for (int i=0; i<state.stacksize; i++) {
            if (state.stack[i] != null) {
                frame.stack[stackCount++] = state.stack[i];
			}
		}	
            
        if (stackMapTableBuffer == null) {
			stackMapTableBuffer = new StackMapTableFrame[20];
		} else if (stackMapTableBuffer.length == stackMapBufferSize) {
			StackMapTableFrame[] newStackMapTableBuffer =
			new StackMapTableFrame[stackMapBufferSize << 1];
			System.arraycopy(stackMapTableBuffer, 0, newStackMapTableBuffer,
							0, stackMapBufferSize);
			stackMapTableBuffer = newStackMapTableBuffer;
		}
		stackMapTableBuffer[stackMapBufferSize++] = 
                StackMapTableFrame.getInstance(frame, lastFrame.pc, lastFrame.locals, types);
               
        frameBeforeLast = lastFrame;
        lastFrame = frame;
    }
    
    StackMapFrame getInitialFrame() {
        StackMapFrame frame = new StackMapFrame();
        List<Type> arg_types = ((MethodType)meth.externalType(types)).argtypes;
        int len = arg_types.length();
        int count = 0;
        if (!meth.isStatic()) {
            Type thisType = meth.owner.type;
            frame.locals = new Type[len+1];
            if (meth.isConstructor() && thisType != syms.objectType) {
                frame.locals[count++] = UninitializedType.uninitializedThis(thisType);
            } else {
                frame.locals[count++] = types.erasure(thisType);
            }
        } else {
            frame.locals = new Type[len];
        }
        for (Type arg_type : arg_types) {
            frame.locals[count++] = types.erasure(arg_type);
        }
        frame.pc = -1;
        frame.stack = null;
        return frame;
    }