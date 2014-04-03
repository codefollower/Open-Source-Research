public boolean isCastable(Type t, Type s, Warner warn)
测试类型t能否转换成类型s
假定t和s满足(t.isPrimitive() != s.isPrimitive())，
意思就是说不存在这样的情况:t是原始类型而s不是原始类型，或者s是原始类型而t不是原始类型
如果t是
BYTE
CHAR
SHORT
INT
LONG
FLOAT
DOUBLE
那么S可以是上面所列7个原始类型中的任意一个，
也就是说，上面的7个原始类型两两之间可以相互转换
如:
double d=10.22;
int i = (int)d;
此时t代表DOUBLE，s代表INT，isCastable将返回true，因为DOUBLE可以转换成INT

如果t是BOOLEAN，那么s必须是BOOLEAN

如果t是VOID，那么t不能转换成其他类型
如:
void m();
int i=(int)m();

如果t是null(BOT)，
只要s是BOT或CLASS或ARRAY或TYPEVAR，那么就可以转换，
因为null(BOT)是BOT、CLASS、ARRAY、TYPEVAR的子类