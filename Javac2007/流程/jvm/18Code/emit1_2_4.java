    /** Emit a byte of code.
     */
    private  void emit1(int od) {
        if (!alive) return;
		if (cp == code.length) {
			byte[] newcode = new byte[cp * 2];
			//假设code.length=100，对应code数组索引号从0到99，
			//因cp从0开始计数，当code数组索引号从0到99都己有数据时，
			//cp的值也变成了100，所以arraycopy要copy 100个数据到newcode
			System.arraycopy(code, 0, newcode, 0, cp);
			code = newcode;
		}
		code[cp++] = (byte)od;
    }

    /** Emit two bytes of code.
     */
    private void emit2(int od) {
        if (!alive) return;
		/*
		int od是4字节的(也就是一个int占32 bit)，但emit2(int od)完
		成的功能是要在code数组中放入两个字节(2 byte=16 bit)，且这两个
		字节是int od的低16位，code数组是一个字节数组，所以得分两次把这
		两个字节放入code数组，首先按15--8bit位构成一字节，下面代码
		中的“(byte)(od >> 8)”把od向右移动8位，这相当于把原来的15--8bit位
		变成7--0bit位，最后把int数值强制转换成byte时，默认取int数值的低8位，
		这低8位也就是原来的15--8bit位。执行完“(byte)(od >> 8)”后也就把第一个
		高位字节加入了code数组中，但此时od的值没变，接着(byte)od就是取低8位，也
		就是第二个低位字节
		*/
		if (cp + 2 > code.length) {//这里不用>=，因为emit1方法中已有==
			
			emit1(od >> 8);//高位在前(也就是高8位在code数组中的下标比低8位小)
			emit1(od);
		} else {
			code[cp++] = (byte)(od >> 8);
			code[cp++] = (byte)od;
		}
    }

    /** Emit four bytes of code.
     */
    public void emit4(int od) {
        if (!alive) return;
		//参考上面emit2(int od)的注释，只不过这里的唯一差别是四个字节
		if (cp + 4 > code.length) {
			emit1(od >> 24);
			emit1(od >> 16);
			emit1(od >> 8);
			emit1(od);
		} else {
			code[cp++] = (byte)(od >> 24);
			code[cp++] = (byte)(od >> 16);
			code[cp++] = (byte)(od >> 8);
			code[cp++] = (byte)od;
		}
    }