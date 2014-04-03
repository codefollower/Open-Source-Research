    void checkDeprecatedAnnotation(DiagnosticPosition pos, Symbol s) {
		/*
		当在javac命令行中启用“-Xlint:dep-ann”选项时，
		如果javadoc文档中有@deprecated，
		但是没有加“@Deprecated ”这个注释标记时，编译器就会发出警告

		注意是:“-Xlint:dep-ann”选项，而不是-Xlint:deprecation
		*/
		DEBUG.P(this,"checkDeprecatedAnnotation(2)");
		if (allowAnnotations &&
			lint.isEnabled(Lint.LintCategory.DEP_ANN) &&
			(s.flags() & DEPRECATED) != 0 &&
			!syms.deprecatedType.isErroneous() &&
			s.attribute(syms.deprecatedType.tsym) == null) {
			log.warning(pos, "missing.deprecated.annotation");
		}
		DEBUG.P(0,this,"checkDeprecatedAnnotation(2)");
    }