package test.attr;
//AttrTests09
//测试Attr中的几个continue
import test.attr.Aclass.f;
import static test.attr.Aclass.f;
class Aclass {
	int f;
	/*
	{
		continue aa;
		continue;
		break;
		return;
	}
	static {
		return;
	}
	*/
	
	
	/*
	{
		int i=0;
		switch(i) {
			default:
			;
		default:
			;
		}

		Aenum aenum;
		switch(aenum) {
			case Aenum.A:
			;
			case Aenum.B:
			;
		}
			//labelA:while(true) labelA: break;
	}

	enum Aenum{
		A,B;
	}
	*/
}