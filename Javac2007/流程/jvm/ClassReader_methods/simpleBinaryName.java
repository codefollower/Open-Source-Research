    // See java.lang.Class
    private Name simpleBinaryName(Name self, Name enclosing) {
        String simpleBinaryName = self.toString().substring(enclosing.toString().length());
        if (simpleBinaryName.length() < 1 || simpleBinaryName.charAt(0) != '$')
            throw badClassFile("bad.enclosing.method", self);
        int index = 1;
        while (index < simpleBinaryName.length() &&
               isAsciiDigit(simpleBinaryName.charAt(index)))
            index++;
        return names.fromString(simpleBinaryName.substring(index));
    }