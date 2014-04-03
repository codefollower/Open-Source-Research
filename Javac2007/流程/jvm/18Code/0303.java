0<init>() 0{
    super();
    field = 10;
    0{
        field = 20;
    }
    0InterfaceTest interfaceTest = this;
    interfaceTest.interfaceMethod(10);
    myStaticMethod();
    myPrivateMethod();
    myMethod(12, "str", new int[]{100, 101, 102});
},
private void myPrivateMethod() 0{
},
private static void myStaticMethod() 0{
},
public void interfaceMethod(0int arg) 0{
},
public void myMethod(0int i, 0String s, 0int... ii) 0{
    final int myMethodInt;
    0{
    }
    0int bbb = 10;
    bbb--;
    0int ccc = 10;
    ccc++;
    0int iii = 10;
    if (iii > 5) iii++; else iii--;
    0boolean myBoolean = false;
    if (i < 0) myBoolean = !myBoolean;
    if (i + 1 / 2 * 3 - 4 > 5) i++; else i--;
    0int ddd = 10;
    for (0int[] arr$ = ii, len$ = arr$.length, i$ = 0; i$ < len$; ++i$) 0{
        final int dd = arr$[i$];
        0{
            0int eee = 3 * 6 * 7;
            if (dd > 0) 0{
                continue;
            } else ddd--;
            if (dd > 10) 0{
                break;
            }
            ddd--;
            0int fff;
        }
    }
    for (; i < 10; i++, i += 2) ;
    for (0int n = 0, n2 = {10, 20}; n < 10; n++) ;
    while (i < 10) ;
    do i++;     while (i < 10);
    try 0{
        i++;
    } catch (0RuntimeException e) 0{
    } catch (0Exception e) 0{
    } finally 0{
        i = 0;
    }
    switch (i) {
    case 0: 
        i++;
        0int fff;
        break;
    
    default: 
        i--;
    
    }
    synchronized (s) 0{
    }
    if (!$assertionsDisabled && !(i < 10)) throw new AssertionError("message");
    myLable: 0{
        0int bb;
    }
    0int[] myIntArray = new int[10];
    myIntArray[1] = 10;
    0int[] myIntArray2 = {1, 2};
    i++;
    0int myInt = 0;
    myInt <<= (int)2L;
},
static void <clinit>() 0{
    $assertionsDisabled = !Test.class.desiredAssertionStatus();
    fieldStatic = 10;
    static {
        fieldStatic = 20;
    }
}