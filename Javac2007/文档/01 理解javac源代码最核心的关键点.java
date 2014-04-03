javac分成以下这几个先后的处理阶段:

命令行选项处理-->词法分析-->语法分析-->Annotation处理-->Enter-->MemberEnter
-->Attr-->Flow-->Lower-->Gem-->ClassWriter

关键的数据结构是AST(抽象语法树)，上面这些阶段都是在围绕这个AST做事情，每个阶段都会往这个AST上补充数据，
词法分析: 负责把源代码中的字符流转化成Token，同时把标识符转成utf-8字节存放到一个Names数组中。
语法分析: 负责生成最简单的AST

具体每个阶段是如何完善AST的，请看知章节的介绍。