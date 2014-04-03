/*
当在javac命令行中启用“-Xlint:dep-ann”选项时，
如果javadoc文档中有@deprecated，
但是没有加“@Deprecated ”这个注释标记时，编译器就会发出警告
*/
package test.attr.warning;
public class missing_deprecated_annotation {
	/**
     * @deprecated
     */
	public void methodDeprecated(){}
}

/*
bin\mysrc\my\warning\missing_deprecated_annotation.java:7: 警告：[dep-ann] 未使
用 @Deprecated 对已过时的项目进行注释
        public void methodDeprecated(){}
                    ^
1 警告
*/
