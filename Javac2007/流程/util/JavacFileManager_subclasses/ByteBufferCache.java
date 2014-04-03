    /**
     * A single-element cache of direct byte buffers.
     */
    private static class ByteBufferCache {
        private ByteBuffer cached;
        ByteBuffer get(int capacity) {
            if (capacity < 20480) capacity = 20480;
            ByteBuffer result =
                (cached != null && cached.capacity() >= capacity)
                ? (ByteBuffer)cached.clear()
                : ByteBuffer.allocate(capacity + capacity>>1);
            cached = null;
            return result;
        }
        void put(ByteBuffer x) {
            cached = x;
        }
    }
