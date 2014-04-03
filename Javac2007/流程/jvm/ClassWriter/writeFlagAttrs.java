    /** Write flag attributes; return number of attributes written.
     */
    int writeFlagAttrs(long flags) {
		DEBUG.P(this,"writeFlagAttrs(long flags)");
		DEBUG.P("flags="+Flags.toString(flags));

        int acount = 0;
        if ((flags & DEPRECATED) != 0) {
            int alenIdx = writeAttr(names.Deprecated);
            endAttr(alenIdx);
            acount++;
        }

		//<=1.4版本的编译器得用属性来标识新的字段标志位
		//参考Target类
        if ((flags & ENUM) != 0 && !target.useEnumFlag()) {
            int alenIdx = writeAttr(names.Enum);
            endAttr(alenIdx);
            acount++;
        }
        if ((flags & SYNTHETIC) != 0 && !target.useSyntheticFlag()) {
            int alenIdx = writeAttr(names.Synthetic);
            endAttr(alenIdx);
            acount++;
        }
        if ((flags & BRIDGE) != 0 && !target.useBridgeFlag()) {
            int alenIdx = writeAttr(names.Bridge);
            endAttr(alenIdx);
            acount++;
        }
        if ((flags & VARARGS) != 0 && !target.useVarargsFlag()) {
            int alenIdx = writeAttr(names.Varargs);
            endAttr(alenIdx);
            acount++;
        }
        if ((flags & ANNOTATION) != 0 && !target.useAnnotationFlag()) {
            int alenIdx = writeAttr(names.Annotation);
            endAttr(alenIdx);
            acount++;
        }

		DEBUG.P("acount="+acount);
		DEBUG.P(0,this,"writeFlagAttrs(long flags)");
		
        return acount;
    }