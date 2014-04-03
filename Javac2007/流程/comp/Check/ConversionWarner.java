    private class ConversionWarner extends Warner {
        final String key;
		final Type found;
        final Type expected;
		public ConversionWarner(DiagnosticPosition pos, String key, Type found, Type expected) {
            super(pos);
            this.key = key;
			this.found = found;
			this.expected = expected;
		}

		public void warnUnchecked() {
			boolean warned = this.warned;
			super.warnUnchecked();
			if (warned) return; // suppress redundant diagnostics
			Object problem = JCDiagnostic.fragment(key);
			Check.this.warnUnchecked(pos(), "prob.found.req", problem, found, expected);
		}
    }

    public Warner castWarner(DiagnosticPosition pos, Type found, Type expected) {
		return new ConversionWarner(pos, "unchecked.cast.to.type", found, expected);
    }

    public Warner convertWarner(DiagnosticPosition pos, Type found, Type expected) {
		return new ConversionWarner(pos, "unchecked.assign", found, expected);
    }
