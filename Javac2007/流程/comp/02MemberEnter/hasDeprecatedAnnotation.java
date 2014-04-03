    /**
     * Check if a list of annotations contains a reference to
     * java.lang.Deprecated.
     **/
    private boolean hasDeprecatedAnnotation(List<JCAnnotation> annotations) {
        for (List<JCAnnotation> al = annotations; al.nonEmpty(); al = al.tail) {
            JCAnnotation a = al.head;
			//因为MemberEnter阶段是紧跟在Parser阶段之后的，而在Parser阶段如果
			//@Deprecated带有参数(如:@Deprecated("str"))是正确的，在这里使用了
			//a.args.isEmpty()是为了提前检测一下是否正确使用了@Deprecated，以便
			//为当前ClassSymbol的flags_field加上DEPRECATED(见complete)
            if (a.annotationType.type == syms.deprecatedType && a.args.isEmpty())
                return true;
        }
        return false;
    }