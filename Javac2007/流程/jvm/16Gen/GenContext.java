/* ************************************************************************
 * Auxiliary classes
 *************************************************************************/
    /** code generation contexts,
     *  to be used as type parameter for environments.
     */
    static class GenContext {

		/** A chain for all unresolved jumps that exit the current environment.
		 */
		Chain exit = null;

		/** A chain for all unresolved jumps that continue in the
		 *  current environment.
		 */
		Chain cont = null;

		/** A closure that generates the finalizer of the current environment.
		 *  Only set for Synchronized and Try contexts.
		 */
		GenFinalizer finalize = null;

		/** Is this a switch statement?  If so, allocate registers
		 * even when the variable declaration is unreachable.
		 */
		boolean isSwitch = false;

			/** A list buffer containing all gaps in the finalizer range,
		 *  where a catch all exception should not apply.
		 */
		ListBuffer<Integer> gaps = null;

		/** Add given chain to exit chain.
		 */
		void addExit(Chain c)  {
			exit = Code.mergeChains(c, exit);
		}

		/** Add given chain to cont chain.
		 */
		void addCont(Chain c) {
			cont = Code.mergeChains(c, cont);
		}
		
		//我加上的
		public String toString() {
			return "GC[gaps="+gaps+", exit="+exit+", cont="+cont+", isSwitch="+isSwitch+", finalize="+finalize+"]";
		}
    }