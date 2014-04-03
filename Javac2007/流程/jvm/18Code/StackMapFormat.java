    public enum StackMapFormat {
		NONE,
		CLDC {
			Name getAttributeName(Name.Table names) {
				return names.StackMap;
			}   
		}, 
		JSR202 {
			Name getAttributeName(Name.Table names) {
				return names.StackMapTable;
			}   
		};
		Name getAttributeName(Name.Table names) {
				return names.empty;
		}        
    }