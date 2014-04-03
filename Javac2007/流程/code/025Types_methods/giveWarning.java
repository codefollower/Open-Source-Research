    private boolean giveWarning(Type from, Type to) {
        // To and from are (possibly different) parameterizations
        // of the same class or interface
        return to.isParameterized() && !containsType(to.getTypeArguments(), from.getTypeArguments());
    }