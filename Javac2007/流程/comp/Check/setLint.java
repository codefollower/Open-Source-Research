    Lint setLint(Lint newLint) {
		Lint prev = lint;
		lint = newLint;
		return prev;
    }