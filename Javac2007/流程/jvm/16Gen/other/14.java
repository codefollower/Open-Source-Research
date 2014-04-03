        /** Will the erasure of this method be considered by the VM to
         *  override the erasure of the other when seen from class `origin'?
         */
        public boolean binaryOverrides(Symbol _other, TypeSymbol origin, Types types) {
            if (isConstructor() || _other.kind != MTH) return false;

            if (this == _other) return true;
            MethodSymbol other = (MethodSymbol)_other;

            // check for a direct implementation
            if (other.isOverridableIn((TypeSymbol)owner) &&
                types.asSuper(owner.type, other.owner) != null &&
                types.isSameType(erasure(types), other.erasure(types)))
                return true;

            // check for an inherited implementation
            return
                (flags() & ABSTRACT) == 0 &&
                other.isOverridableIn(origin) &&
                this.isMemberOf(origin, types) &&
                types.isSameType(erasure(types), other.erasure(types));
        }