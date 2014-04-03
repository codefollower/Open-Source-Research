/*
 * @(#)OptionName.java	1.4 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.main;

import com.sun.tools.javac.util.Version;

/**
 * TODO: describe com.sun.tools.javac.main.OptionName
 *
 * <p><b>This is NOT part of any API supported by Sun Microsystems.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice.</b></p>
 */
@Version("@(#)OptionName.java	1.4 07/03/21")
public enum OptionName {
/*
用法：javac <选项> <源文件>
其中，可能的选项包括：
 -g                           生成所有调试信息
 -g:none                      不生成任何调试信息
 -g:{lines,vars,source}       只生成某些调试信息
 -nowarn                      不生成任何警告
 -verbose                     输出有关编译器正在执行的操作的消息
 -deprecation                 输出使用已过时的 API 的源位置
 -classpath <路径>            指定查找用户类文件和注释处理程序的位置
 -cp <路径>                   指定查找用户类文件和注释处理程序的位置
 -sourcepath <路径>           指定查找输入源文件的位置
 -bootclasspath <路径>        覆盖引导类文件的位置
 -extdirs <目录>              覆盖安装的扩展目录的位置
 -endorseddirs <目录>         覆盖签名的标准路径的位置
 -proc:{none, only}           控制是否执行注释处理和/或编译。
 -processor <class1>[,<class2>,<class3>...]要运行的注释处理程序的名称；绕过默认的搜索进程
 -processorpath <路径>        指定查找注释处理程序的位置
 -d <目录>                    指定存放生成的类文件的位置
 -s <目录>                    指定存放生成的源文件的位置
 -encoding <编码>             指定源文件使用的字符编码
 -source <版本>               提供与指定版本的源兼容性
 -target <版本>               生成特定 VM 版本的类文件
 -version                     版本信息
 -help                        输出标准选项的提要
 -Akey[=value]                传递给注释处理程序的选项
 -X                           输出非标准选项的提要
 -J<标志>                     直接将 <标志> 传递给运行时系统


 -Xlint                       启用建议的警告
 -Xlint:{all,cast,deprecation,divzero,empty,unchecked,fallthrough,path,serial,f
         nally,overrides,-cast,-deprecation,-divzero,-empty,-unchecked,-fallthrough,-path
         ,-serial,-finally,-overrides,none}启用或禁用特定的警告
 -Xbootclasspath/p:<路径>     置于引导类路径之前
 -Xbootclasspath/a:<路径>     置于引导类路径之后
 -Xbootclasspath:<路径>       覆盖引导类文件的位置
 -Djava.ext.dirs=<目录>       覆盖安装的扩展目录的位置
 -Djava.endorsed.dirs=<目录>  覆盖签名的标准路径的位置
 -Xmaxerrs <编号>             设置要输出的错误的最大数目
 -Xmaxwarns <编号>            设置要输出的警告的最大数目
 -Xstdout <文件名>            重定向标准输出
 -Xprint                      输出指定类型的文本表示
 -XprintRounds                输出有关注释处理循环的信息
 -XprintProcessorInfo         输出有关请求处理程序处理哪些注释的信息

这些选项都是非标准选项，如有更改，恕不另行通知。
*/
    //总共52项
    G("-g"),
    G_NONE("-g:none"),
    G_CUSTOM("-g:{lines,vars,source}"),
    XLINT("-Xlint"),
    XLINT_CUSTOM("-Xlint:{"
                 + "all,"
                 + "cast,deprecation,divzero,empty,unchecked,fallthrough,path,serial,finally,overrides,"
                 + "-cast,-deprecation,-divzero,-empty,-unchecked,-fallthrough,-path,-serial,-finally,-overrides,"
                 + "none}"),
    NOWARN("-nowarn"),
    VERBOSE("-verbose"),
    DEPRECATION("-deprecation"),
    CLASSPATH("-classpath"),
    CP("-cp"),
    SOURCEPATH("-sourcepath"),
    BOOTCLASSPATH("-bootclasspath"),
    XBOOTCLASSPATH_PREPEND("-Xbootclasspath/p:"),
    XBOOTCLASSPATH_APPEND("-Xbootclasspath/a:"),
    XBOOTCLASSPATH("-Xbootclasspath:"),
    EXTDIRS("-extdirs"),
    DJAVA_EXT_DIRS("-Djava.ext.dirs="),
    ENDORSEDDIRS("-endorseddirs"),
    DJAVA_ENDORSED_DIRS("-Djava.endorsed.dirs="),
    PROC_CUSTOM("-proc:{none,only}"),
    PROCESSOR("-processor"),
    PROCESSORPATH("-processorpath"),
    D("-d"),
    S("-s"),
    IMPLICIT("-implicit:{none,class}"),//1.7新增标准选项，指定是否为隐式引用文件生成类文件
    ENCODING("-encoding"),
    SOURCE("-source"),
    TARGET("-target"),
    VERSION("-version"),
    FULLVERSION("-fullversion"),//隐藏选项(内部使用，不会显示)
    HELP("-help"),
    A("-A"),
    X("-X"),
    J("-J"),
    MOREINFO("-moreinfo"),//隐藏选项(内部使用，不会显示)
    WERROR("-Werror"),//隐藏选项(内部使用，不会显示)
    COMPLEXINFERENCE("-complexinference"),//隐藏选项(内部使用，不会显示)
    PROMPT("-prompt"),//隐藏选项(内部使用，不会显示)
    DOE("-doe"),//隐藏选项(内部使用，不会显示)
    PRINTSOURCE("-printsource"),//隐藏选项(内部使用，不会显示)
    WARNUNCHECKED("-warnunchecked"),//隐藏选项(内部使用，不会显示)
    XMAXERRS("-Xmaxerrs"),
    XMAXWARNS("-Xmaxwarns"),
    XSTDOUT("-Xstdout"),
    XPRINT("-Xprint"),
    XPRINTROUNDS("-XprintRounds"),
    XPRINTPROCESSORINFO("-XprintProcessorInfo"),
    XPREFER("-Xprefer:{source,newer}"),//1.7新增扩展选项，指定读取文件，当同时找到隐式编译类的源文件和类文件时
    O("-O"),//隐藏选项(内部使用，不会显示)
    XJCOV("-Xjcov"),//隐藏选项(内部使用，不会显示)
    XD("-XD"),//隐藏选项(内部使用，不会显示)
    SOURCEFILE("sourcefile");//隐藏选项(内部使用，不会显示)

    public final String optionName;

    OptionName(String optionName) {
        this.optionName = optionName;
    }

    @Override
    public String toString() {
        return optionName;
    }

}
