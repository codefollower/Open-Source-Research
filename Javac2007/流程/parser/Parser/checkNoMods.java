    /** Diagnose a modifier flag from the set, if any. */
    void checkNoMods(long mods) {
    	DEBUG.P(this,"checkNoMods(long mods)");
    	DEBUG.P("mods="+Flags.toString(mods).trim());
    	
        if (mods != 0) {
            /*
            只取mods最底非0位,其他位都清0:
            for(int mods=1;mods<6;mods++) {
                System.out.println("十进制: "+mods+" & -"+mods+" = "+(mods & -mods));
                System.out.println("二进制: "+Integer.toBinaryString(mods)+" & "+Integer.toBinaryString(-mods)+" = "+Integer.toBinaryString(mods & -mods));
                System.out.println();
            }
            输出:(互为相反数的两个数都可按“按位取反加1”的原则得到对方)
            十进制: 1 & -1 = 1
            二进制: 1 & 11111111111111111111111111111111 = 1

            十进制: 2 & -2 = 2
            二进制: 10 & 11111111111111111111111111111110 = 10

            十进制: 3 & -3 = 1
            二进制: 11 & 11111111111111111111111111111101 = 1

            十进制: 4 & -4 = 4
            二进制: 100 & 11111111111111111111111111111100 = 100

            十进制: 5 & -5 = 1
            二进制: 101 & 11111111111111111111111111111011 = 1
            */
            long lowestMod = mods & -mods;
            DEBUG.P("lowestMod="+Flags.toString(lowestMod).trim());
            log.error(S.pos(), "mod.not.allowed.here",
                      Flags.toString(lowestMod).trim());
        }
        DEBUG.P(0,this,"checkNoMods(long mods)");
    }