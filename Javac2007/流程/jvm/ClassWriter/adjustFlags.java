	int adjustFlags(final long flags) {
    	DEBUG.P(this,"adjustFlags(1)");
		DEBUG.P("flags ="+Flags.toString(flags));

        int result = (int)flags;//从32bit开始的标志位将被丢弃
		//DEBUG.P("result ="+Flags.toString(result));
		//DEBUG.P("result ="+Flags.toString(0xff));
        if ((flags & SYNTHETIC) != 0  && !target.useSyntheticFlag())
            result &= ~SYNTHETIC;
        if ((flags & ENUM) != 0  && !target.useEnumFlag())
            result &= ~ENUM;
        if ((flags & ANNOTATION) != 0  && !target.useAnnotationFlag())
            result &= ~ANNOTATION;

        if ((flags & BRIDGE) != 0  && target.useBridgeFlag())
            result |= ACC_BRIDGE;
        if ((flags & VARARGS) != 0  && target.useVarargsFlag())
            result |= ACC_VARARGS;
        
        
        //DEBUG.P("result="+Flags.toString(result));
        //当int的最高位是1时，转换成long时最高位1向左扩展32位
		DEBUG.P("result="+Flags.toString((long)result&0x00000000ffffffff));
        DEBUG.P(0,this,"adjustFlags(1)");
		
        return result;
    }