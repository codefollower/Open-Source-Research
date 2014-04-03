package test.enter;


enum TestEnum {
    BAR,
    QUX,
    BAZ;
    static String X = "X";
    TestEnum() {
        String y = X;
    }
}

enum TestEnum2 {
    BAR,
    QUX,
    BAZ {
        private final String x = X;
		{
			String y = X;
		}
		static {
			String z = X;
		}
    };
    static String X = "X";

	String x = X;

	{
			String y = X;
		}
		static {
			String z = X;
		}
}
