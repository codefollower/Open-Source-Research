    /** The number of errors reported so far.
     */
    public int errorCount() {
        if (delegateCompiler != null && delegateCompiler != this)
            return delegateCompiler.errorCount();
        else
            return log.nerrors;
    }
    
    //在编译的每个阶段里都有可能找到错误，如果某一阶段找到了错误导致
    //接下来的阶段任务无法进行，就会先调用stopIfError()方法，如果错误
    //数为0，就继续下一阶段的任务，否则编译不正常结束。
    protected final <T> List<T> stopIfError(ListBuffer<T> listBuffer) {
        if (errorCount() == 0)
            return listBuffer.toList();
        else
            return List.nil();
    }

    protected final <T> List<T> stopIfError(List<T> list) {
        if (errorCount() == 0)
            return list;
        else
            return List.nil();
    }