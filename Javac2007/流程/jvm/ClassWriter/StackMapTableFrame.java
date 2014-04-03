    /** An entry in the JSR202 StackMapTable */
    abstract static class StackMapTableFrame {
        abstract int getFrameType();
        
        void write(ClassWriter writer) {
            int frameType = getFrameType();
            writer.databuf.appendByte(frameType);
            if (writer.debugstackmap) System.out.print(" frame_type=" + frameType);
        }
        
        static class SameFrame extends StackMapTableFrame {
            final int offsetDelta;
            SameFrame(int offsetDelta) {
                this.offsetDelta = offsetDelta;
            }
            int getFrameType() {
                return (offsetDelta < SAME_FRAME_SIZE) ? offsetDelta : SAME_FRAME_EXTENDED; 
            }
            @Override
            void write(ClassWriter writer) {
                super.write(writer);
                if (getFrameType() == SAME_FRAME_EXTENDED) {
                    writer.databuf.appendChar(offsetDelta);
                    if (writer.debugstackmap){
                        System.out.print(" offset_delta=" + offsetDelta);
                    }
                }
            }
        }
        
        static class SameLocals1StackItemFrame extends StackMapTableFrame {
            final int offsetDelta;
            final Type stack;
            SameLocals1StackItemFrame(int offsetDelta, Type stack) {
                this.offsetDelta = offsetDelta;
                this.stack = stack;
            }
            int getFrameType() {
                return (offsetDelta < SAME_FRAME_SIZE) ? 
                       (SAME_FRAME_SIZE + offsetDelta) :
                       SAME_LOCALS_1_STACK_ITEM_EXTENDED;
            }
            @Override
            void write(ClassWriter writer) {
                super.write(writer);
                if (getFrameType() == SAME_LOCALS_1_STACK_ITEM_EXTENDED) {
                    writer.databuf.appendChar(offsetDelta);
                    if (writer.debugstackmap) {
                        System.out.print(" offset_delta=" + offsetDelta);
                    }
                }
                if (writer.debugstackmap) {
                    System.out.print(" stack[" + 0 + "]=");
                }
                writer.writeStackMapType(stack);
            }
        }

        static class ChopFrame extends StackMapTableFrame {
            final int frameType;
            final int offsetDelta;
            ChopFrame(int frameType, int offsetDelta) {
                this.frameType = frameType;
                this.offsetDelta = offsetDelta;
            }
            int getFrameType() { return frameType; }
            @Override
            void write(ClassWriter writer) {
                super.write(writer);
                writer.databuf.appendChar(offsetDelta);
                if (writer.debugstackmap) {
                    System.out.print(" offset_delta=" + offsetDelta);
                }
            }
        }
        
        static class AppendFrame extends StackMapTableFrame {
            final int frameType;
            final int offsetDelta;
            final Type[] locals;
            AppendFrame(int frameType, int offsetDelta, Type[] locals) {
                this.frameType = frameType;
                this.offsetDelta = offsetDelta;
                this.locals = locals;
            }
            int getFrameType() { return frameType; }
            @Override
            void write(ClassWriter writer) {
                super.write(writer);
                writer.databuf.appendChar(offsetDelta);
                if (writer.debugstackmap) {
                    System.out.print(" offset_delta=" + offsetDelta);
                }
                for (int i=0; i<locals.length; i++) {
                     if (writer.debugstackmap) System.out.print(" locals[" + i + "]=");
                     writer.writeStackMapType(locals[i]);
                }
            }
        }
        
        static class FullFrame extends StackMapTableFrame {
            final int offsetDelta;
            final Type[] locals;
            final Type[] stack;
            FullFrame(int offsetDelta, Type[] locals, Type[] stack) {
                this.offsetDelta = offsetDelta;
                this.locals = locals;
                this.stack = stack;
            }
            int getFrameType() { return FULL_FRAME; }
            @Override
            void write(ClassWriter writer) {
                super.write(writer);
                writer.databuf.appendChar(offsetDelta);
                writer.databuf.appendChar(locals.length);
                if (writer.debugstackmap) {
                    System.out.print(" offset_delta=" + offsetDelta);
                    System.out.print(" nlocals=" + locals.length);
                }
                for (int i=0; i<locals.length; i++) {
                    if (writer.debugstackmap) System.out.print(" locals[" + i + "]=");
                    writer.writeStackMapType(locals[i]);
                }

                writer.databuf.appendChar(stack.length);
                if (writer.debugstackmap) { System.out.print(" nstack=" + stack.length); }
                for (int i=0; i<stack.length; i++) {
                    if (writer.debugstackmap) System.out.print(" stack[" + i + "]=");
                    writer.writeStackMapType(stack[i]);
                }
            }
        }
        
       /** Compare this frame with the previous frame and produce
        *  an entry of compressed stack map frame. */
        static StackMapTableFrame getInstance(Code.StackMapFrame this_frame, 
                                              int prev_pc, 
                                              Type[] prev_locals,
                                              Types types) {
            Type[] locals = this_frame.locals;
            Type[] stack = this_frame.stack;
            int offset_delta = this_frame.pc - prev_pc - 1;
            if (stack.length == 1) {
                if (locals.length == prev_locals.length
                    && compare(prev_locals, locals, types) == 0) {
                    return new SameLocals1StackItemFrame(offset_delta, stack[0]);
                }
            } else if (stack.length == 0) {
                int diff_length = compare(prev_locals, locals, types);
                if (diff_length == 0) {
                    return new SameFrame(offset_delta);
                } else if (-MAX_LOCAL_LENGTH_DIFF < diff_length && diff_length < 0) {
                    // APPEND
                    Type[] local_diff = new Type[-diff_length];
                    for (int i=prev_locals.length, j=0; i<locals.length; i++,j++) {
                        local_diff[j] = locals[i];
                    }
                    return new AppendFrame(SAME_FRAME_EXTENDED - diff_length, 
                                           offset_delta, 
                                           local_diff);
                } else if (0 < diff_length && diff_length < MAX_LOCAL_LENGTH_DIFF) {
                    // CHOP 
                    return new ChopFrame(SAME_FRAME_EXTENDED - diff_length,
                                         offset_delta);
                }
            }
            // FULL_FRAME
            return new FullFrame(offset_delta, locals, stack);
        }
        
        static boolean isInt(Type t) {
            return (t.tag < TypeTags.INT || t.tag == TypeTags.BOOLEAN);
        }

        static boolean isSameType(Type t1, Type t2, Types types) {
			//同时为null返回true，只要一个为null别一个不为null返回false
            if (t1 == null) { return t2 == null; }
            if (t2 == null) { return false; }

            if (isInt(t1) && isInt(t2)) { return true; }

            if (t1.tag == UNINITIALIZED_THIS) {
                return t2.tag == UNINITIALIZED_THIS;
            } else if (t1.tag == UNINITIALIZED_OBJECT) {
                if (t2.tag == UNINITIALIZED_OBJECT) {
                    return ((UninitializedType)t1).offset == ((UninitializedType)t2).offset;
                } else {
                    return false;
                }
            } else if (t2.tag == UNINITIALIZED_THIS || t2.tag == UNINITIALIZED_OBJECT) {
                return false;
            }

            return types.isSameType(t1, t2);
        }

        static int compare(Type[] arr1, Type[] arr2, Types types) {
            int diff_length = arr1.length - arr2.length;
            if (diff_length > MAX_LOCAL_LENGTH_DIFF || diff_length < -MAX_LOCAL_LENGTH_DIFF) {
                return Integer.MAX_VALUE;
            }
            int len = (diff_length > 0) ? arr2.length : arr1.length;
            for (int i=0; i<len; i++) {
                if (!isSameType(arr1[i], arr2[i], types)) {
                    return Integer.MAX_VALUE;
                }
            }
            return diff_length;
        }
    }