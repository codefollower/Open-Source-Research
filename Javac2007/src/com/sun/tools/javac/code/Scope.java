/*
 * @(#)Scope.java	1.43 07/03/21
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *  
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javac.code;

import com.sun.tools.javac.util.*;
import java.util.Iterator;

/** A scope represents an area of visibility in a Java program. The
 *  Scope class is a container for symbols which provides
 *  efficient access to symbols given their names. Scopes are implemented
 *  as hash tables. Scopes can be nested; the next field of a scope points
 *  to its next outer scope. Nested scopes can share their hash tables.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Scope.java	1.43 07/03/21")
public class Scope {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Scope);//我加上的

    /** The number of scopes that share this scope's hash table.
     */
    private int shared;

    /** Next enclosing scope (with whom this scope may share a hashtable)
     */
    public Scope next;

    /** The scope's owner.
     */
    public Symbol owner;

    /** A hash table for the scope's entries.
     */
    public Entry[] table;

    /** Mask for hash codes, always equal to (table.length - 1).
     */
    int hashMask;

    /** A linear list that also contains all entries in
     *  reverse order of appearance (i.e later entries are pushed on top).
     */
    public Entry elems;//总是指向最新加进来的Entry

    /** The number of elements in this scope.
     */
	//记录enter到scope中的Entry的个数,如果Entry被删除了，
	//nelems不会减少，也就是说nelems总是往前递增的
    public int nelems = 0;

    /** Every hash bucket is a list of Entry's which ends in sentinel.
     */
    private static final Entry sentinel = new Entry(null, null, null, null);

    /** The hash table's initial size.
     */
    private static final int INITIAL_SIZE = 0x10;

    /** A value for the empty scope.
     */
    public static final Scope emptyScope = new Scope(null, null, new Entry[]{});

    /** Construct a new scope, within scope next, with given owner, using
     *  given table. The table's length must be an exponent of 2.
     */
    Scope(Scope next, Symbol owner, Entry[] table) {
        this.next = next;
        //emptyScope == null是否有必要???有
        //因为当执行emptyScope = new Scope(null, null, new Entry[]{})时
        //emptyScope为null
        assert emptyScope == null || owner != null;
        this.owner = owner;
        this.table = table;
        this.hashMask = table.length - 1;
        this.elems = null;
        this.nelems = 0;
        this.shared = 0;
    }

    /** Construct a new scope, within scope next, with given owner,
     *  using a fresh table of length INITIAL_SIZE.
     */
    public Scope(Symbol owner) {
        this(null, owner, new Entry[INITIAL_SIZE]);
		for (int i = 0; i < INITIAL_SIZE; i++) table[i] = sentinel;
    }

    /** Construct a fresh scope within this scope, with same owner,
     *  which shares its table with the outer scope. Used in connection with
     *  method leave if scope access is stack-like in order to avoid allocation
     *  of fresh tables.
     */
    public Scope dup() {
        Scope result = new Scope(this, this.owner, this.table);
		shared++;
		// System.out.println("====> duping scope " + this.hashCode() + " owned by " + this.owner + " to " + result.hashCode());
		// new Error().printStackTrace(System.out);
		return result;
    }

    /** Construct a fresh scope within this scope, with new owner,
     *  which shares its table with the outer scope. Used in connection with
     *  method leave if scope access is stack-like in order to avoid allocation
     *  of fresh tables.
     */
    public Scope dup(Symbol newOwner) {
        Scope result = new Scope(this, newOwner, this.table);
		shared++;
		// System.out.println("====> duping scope " + this.hashCode() + " owned by " + newOwner + " to " + result.hashCode());
		// new Error().printStackTrace(System.out);
		return result;
    }

    /** Construct a fresh scope within this scope, with same owner,
     *  with a new hash table, whose contents initially are those of
     *  the table of its outer scope.
     */
    public Scope dupUnshared() {
		return new Scope(this, this.owner, this.table.clone());
    }

    /** Remove all entries of this scope from its table, if shared
     *  with next.
     */
    
    /*对于Scope A，如果通过调用dupUnshared()产生了Scope B,
    那么Scope B的next指向Scope A，Scope B的table是Scope A的table的克隆(clone)，
    也就是Scope B的table不等于Scope A的table，调用Scope B的enter方法加进
    的新entry不影响Scope A的table，这时调用Scope B的leave()方法将
    直接返回Scope A；
    
    如果通过调用Scope A的dup()或dup(Symbol newOwner)产生了Scope B,
    那么Scope B的next指向Scope A，Scope B的table也是Scope A的table，
    这时调用Scope B的leave()方法将删除原本不在Scope A的table中的entry。
    
    比如：在没产生Scope B前，Scope A的table中只有a,b,c这三个entry，
    在生成Scope B后，可能调用Scope B的enter方法加进了d,e这两个entry，因为
    Scope B的table也是Scope A的table，为了还原到最初状态，
    调用Scope B的leave()方法将删除d,e这两个entry，然后再返回Scope A。

	也就是说由Scope B的elems打头的sibling链中的所有entry都可以直接由table[hash]引用，
	或者通过sibling链头先后逐个删除排在前头的entry后能够被table[hash]引用
    */
    public Scope leave() {
		try {
    	DEBUG.P(this,"leave()");
		DEBUG.P("shared="+shared);

		assert shared == 0;//最新dup得到的Scope的shared总是0
	
		//next.table没有共享的情况

		DEBUG.P("(table != next.table)="+(table != next.table));
		if (table != next.table) return next;

		DEBUG.P("elems="+elems);
		while (elems != null) {
            int hash = elems.sym.name.index & hashMask;
			Entry e = table[hash];
			assert e == elems : elems.sym;
			table[hash] = elems.shadowed;
			elems = elems.sibling;
        }

		DEBUG.P("next.shared="+next.shared);
		assert next.shared > 0;
		next.shared--;
		// System.out.println("====> leaving scope " + this.hashCode() + " owned by " + this.owner + " to " + next.hashCode());
		// new Error().printStackTrace(System.out);
		return next;

		} finally {
    	DEBUG.P(0,this,"leave()");
    	}
    }

    /** Double size of hash table.
     */
    private void dble() {
		assert shared == 0;//只有当前Scope的table没有共享时才能扩大table的数目
		Entry[] oldtable = table;
		Entry[] newtable = new Entry[oldtable.length * 2];
		for (Scope s = this; s != null; s = s.next) {
			if (s.table == oldtable) {
				assert s == this || s.shared != 0;
				s.table = newtable;
				s.hashMask = newtable.length - 1;
			}
		}
		for (int i = 0; i < newtable.length; i++) newtable[i] = sentinel;
		for (int i = 0; i < oldtable.length; i++) copy(oldtable[i]);
    }

    /** Copy the given entry and all entries shadowed by it to table
     */
    //从hash链头走到链尾，以链尾结点开始重建hash链，但sibling保持不变
    private void copy(Entry e) {
		if (e.sym != null) {
			copy(e.shadowed);
			int hash = e.sym.name.index & hashMask;
			e.shadowed = table[hash];
			table[hash] = e;
		}
    }

    /** Enter symbol sym in this scope.
     */
    public void enter(Symbol sym) {
		assert shared == 0;
		enter(sym, this);
    }

    public void enter(Symbol sym, Scope s) {
		enter(sym, s, s);
    }

    /**
     * Enter symbol sym in this scope, but mark that it comes from
     * given scope `s' accessed through `origin'.  The last two
     * arguments are only used in import scopes.
     */
	//例如:如果sym是ClassA中的一个成员，ClassB继承了ClassA，并且ClassB也得到了这个sym
	//那么在把ClassB对应的Scope所包含的所有成员enter到当前的Scope.table时，
	//在调用makeEntry生成一个entry时，这个entry.scope对应参数Scope s(也就是ClassA对应的Scope)
	//而entry.origin(当entry是ImportEntry的实例时)对应参数Scope origin(也就是ClassB对应的Scope)
    //简言之Scope s是sym所在的最初位置，而Scope origin只是sym在继承树上的一个中间位置
	//这里用origin名明能把人搞糊涂，具体调用例子看MemberEnter类中的importStaticAll方法
	public void enter(Symbol sym, Scope s, Scope origin) {
		assert shared == 0;
		// Temporarily disabled (bug 6460352):
		// if (nelems * 3 >= hashMask * 2) dble();
		//因hashMask=table.length-1,所以hash的值肯定<table.length
		int hash = sym.name.index & hashMask;
		/*
		my.L.o("table.length="+hashMask+1);
		my.L.o("sym.name.index="+sym.name.index);
		my.L.o("hash="+hash);
		my.L.o("nelems="+nelems);
		*/
		
		//hash值相同的Entry，是用Entry类的shadowed连在一起的,后进的排在最前面
		//另外Entry类的sibling把所有新增的Entry连在一起，也是后进的排在最前面，
		//elems总是指向最新增加的Entry
		Entry e = makeEntry(sym, table[hash], elems, s, origin);//值得注意,技巧性很强
		table[hash] = e;//table[hash]总是指向最新加进的具有相同hash值的Entry
		elems = e;
		nelems++;
    }

    Entry makeEntry(Symbol sym, Entry shadowed, Entry sibling, Scope scope, Scope origin) {
		return new Entry(sym, shadowed, sibling, scope);
    }

    /** Remove symbol from this scope.  Used when an inner class
     *  attribute tells us that the class isn't a package member.
     */
    //删除一个entry(也相当于删除一个sym)后必需同时调整hash链(shadowed链)与sibling链
    public void remove(Symbol sym) {
		assert shared == 0;
		Entry e = lookup(sym.name);
		//在lookup后只是说明在hash链中找到了sym.name相同的Symbol，但并不代表
		//找到的Symbol就是当前要删除的Symbol，因为在同一个Scope中可能有两
		//个Symbol引用，它们的name都是相同的，但它们并不是指向同一个Symbol实例.
		//如对于两个同名同返回值但是不同参数的两个方法就是这种情况，还有
		//字段与方法名相同时也同样需要条件e.sym != sym判断一下才能确认是否是真正
		//要删除的Symbol。
		while (e.scope == this && e.sym != sym) e = e.next();
		if (e.scope == null) return;

		// remove e from table and shadowed list;
		Entry te = table[sym.name.index & hashMask];
		//如果要删除的sym正好是hash链头，直接把hash链头调到e.shadowed
		//否则从hash链头往下查找
		if (te == e)
			table[sym.name.index & hashMask] = e.shadowed;
		else while (true) {
			if (te.shadowed == e) {
				te.shadowed = e.shadowed;
				break;
			}
			te = te.shadowed;
		}

		// remove e from elems and sibling list
		te = elems;
		if (te == e)
			elems = e.sibling;
		else while (true) {
			if (te.sibling == e) {
				te.sibling = e.sibling;
				break;
			}
			te = te.sibling;
		}
    }

    /** Enter symbol sym in this scope if not already there.
     */
    public void enterIfAbsent(Symbol sym) {
		assert shared == 0;
		Entry e = lookup(sym.name);
		//这里的e.sym.kind != sym.kind与上面的e.sym != sym有差异
		//e.sym.kind != sym.kind:如果有两个方法同名，只插入一个?????
		while (e.scope == this && e.sym.kind != sym.kind) e = e.next();
		if (e.scope != this) enter(sym);
    }

    /** Given a class, is there already a class with same fully
     *  qualified name in this (import) scope?
     */
    public boolean includes(Symbol c) {
		/*
		//对于e.scope == this这个条件是跟据下列两点判断的(第2点有疑惑？？？):
		//1:当lookup(c.name)找不到c.name时，返回sentinel，而sentinel.scope=null
		//2:当Scope.table是共享时，加进table中的entry的scope字段是否是这个table所在的scope
		//3:第2点不对，参见上面的enter方法，e.scope并不一定是this

		//当this.table里头有一个shadowed链，这个shadowed链有两个entry：entryA与entryB
		//entryA.sym.name=entryB.sym.name，entryA.scope!=null也!=this，
		//而entryB.scope=this，且entryB.sym == c，但因为entryA排在entryB前面，
		//当调用lookup(c.name)时首先返回entryA，由entryA.scope!=this，
		//for循环因为e.scope == this这个条件变为false，从而不再执行e = e.next()，
		也就是还没取出entryB就返回false了，这显然是不对的
		*/
		for (Scope.Entry e = lookup(c.name);
			 e.scope == this;
			 e = e.next()) {
			//当lookup(c.name)找到了c.name时，
			//这个Symbol c可以代表同名同返回值但不同参数的方法，也可能代表同名的字段和方法
			//所以还得判断(e.sym == c)，如果判断结果为false则通过e.next()取得同name的下一个entry
			if (e.sym == c) return true;
		}
		return false;
    }

    /** Return the entry associated with given name, starting in
     *  this scope and proceeding outwards. If no entry was found,
     *  return the sentinel, which is characterized by having a null in
     *  both its scope and sym fields, whereas both fields are non-null
     *  for regular entries.
     */
     
    /*
    前题条件:table[].size>0
    (不过作者没有判断:如Scope.emptyScope.lookup(Name name)这样的调用就会ArrayIndexOutOfBoundsException)
    
    先跟据name.index & hashMask计算hash值,如果table[hash].name
    不是要找的name,再根据shadowed往下找
    
    如果scope没加进任何entry,但由于调用Scope(Symbol owner)时已有
    INITIAL_SIZE个Entry sentinel = new Entry(null, null, null, null);
    所以e.scope==null,马上返回一个Entry sentinel且sentinel.sym==null
    这对于ClassReader.includeClassFile()方法中的如下代码非常有用：
            ClassSymbol c = isPkgInfo
            ? p.package_info
            : (ClassSymbol) p.members_field.lookup(classname).sym;
            
    如果c=null,说明classname所代表的ClassSymbol没有加进p.members_field
    */
    public Entry lookup(Name name) { //如果找不到name，则返回sentinel
		Entry e = table[name.index & hashMask];
		while (e.scope != null && e.sym.name != name)
			e = e.shadowed;
		return e;
    }
    
    public Iterable<Symbol> getElements() {
		return new Iterable<Symbol>() {
			public Iterator<Symbol> iterator() {
				return new Iterator<Symbol>() {
					private Scope currScope = Scope.this;
					private Scope.Entry currEntry = elems;
                    { 
                        update();
                    }
                    
					public boolean hasNext() {
						return currEntry != null;
					}

					public Symbol next() {
						Symbol sym = (currEntry == null ? null : currEntry.sym);
						currEntry = currEntry.sibling;
						update();
						return sym;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
							
                    private void update() {
                        while (currEntry == null && currScope.next != null) {
                            currScope = currScope.next;
                            currEntry = currScope.elems;
                        }
                    }
				};
			}
		};
    }
    /*
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Scope[");
        for (Scope s = this; s != null ; s = s.next) {
            if (s != this) result.append(" | ");
            for (Entry e = s.elems; e != null; e = e.sibling) {
                if (e != s.elems) result.append(", ");
                result.append(e.sym);
            }
        }
        result.append("]");
        return result.toString();
    }
    */
	/*
	//下面是我对toString()的改进，调试用途
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Scope[");
        for (Scope s = this; s != null ; s = s.next) {
        	if (s != this) result.append(" | ");//要知道为什么？请看com.sun.tools.javac.comp.MemberEnter.methodEnv()

        		result.append("(nelems=").append(s.nelems).append(" owner=");
            	result.append(s.owner.name);
            	if(s.owner.kind==Kinds.MTH) result.append("()");
            	result.append(")");
			//因为nelems并不是代表Scope中当前的entry总数，
			//它只是记录了到目录为止曾经有多少个entry被enter进Scope里，
			//但是Scope里的entry有可能被删除了，
			//所以用下面的变量entries记录Scope里头实际存在的entry总个数
            int entries=0;
            for (Entry e = s.elems; e != null; e = e.sibling) {
				entries++;
                if (e != s.elems) result.append(", ");
                //result.append(e.sym);
                result.append(e.sym.name); //我加上的，避免对sym进行不必要的complete()
                if(e.sym.kind==Kinds.MTH) result.append("()"); //我加上的
            }
        }
        result.append("]");
        return result.toString();
    }
	*/
    //下面是我对toString()的改进，调试用途
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Scope[");
        for (Scope s = this; s != null ; s = s.next) {
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
        	if (s != this) result.append(" | ");//要知道为什么？请看com.sun.tools.javac.comp.MemberEnter.methodEnv()

			//因为nelems并不是代表Scope中当前的entry总数，
			//它只是记录了到目录为止曾经有多少个entry被enter进Scope里，
			//但是Scope里的entry有可能被删除了，
			//所以用下面的变量entries记录Scope里头实际存在的entry总个数
            int entries=0;
            for (Entry e = s.elems; e != null; e = e.sibling) {
				entries++;
                if (e != s.elems) sb1.append(", ");
                //sb1.append(e.sym);
                sb1.append(e.sym.name); //我加上的，避免对sym进行不必要的complete()
                if(e.sym.kind==Kinds.MTH) sb1.append("()"); //我加上的
            }

			sb2.append("(entries=").append(entries);
			sb2.append(" nelems=").append(s.nelems);
			sb2.append(" owner=").append(s.owner.name);
			if(s.owner.kind==Kinds.MTH) sb2.append("()");
			sb2.append(")");

			result.append(sb2.toString()).append(sb1.toString());
        }
        result.append("]");
        return result.toString();
    }

    /** A class for scope entries.
     */
    public static class Entry {

		/** The referenced symbol.
		 *  sym == null   iff   this == sentinel
		 */
		public Symbol sym;

		/** An entry with the same hash code, or sentinel.
		 */
		private Entry shadowed;

			/** Next entry in same scope.
		 */
		public Entry sibling;
		
		/** The entry's scope.
		 *  scope == null   iff   this == sentinel
		 *  for an entry in an import scope, this is the scope
		 *  where the entry came from (i.e. was imported from).
		 */
		public Scope scope;

		public Entry(Symbol sym, Entry shadowed, Entry sibling, Scope scope) {
			this.sym = sym;
			this.shadowed = shadowed;
			this.sibling = sibling;
			this.scope = scope;
		}

        /** Return next entry with the same name as this entry, proceeding
		 *  outwards if not found in this scope.
		 */
		public Entry next() {
			//在shadowed链中返回与当前entry.sym.name相同的entry，如果没有则返回sentinel
			Entry e = shadowed;
			//从判断e.sym.name != sym.name这个条件可以看出，
			//在shadowed链中存在重复的entry，并且这些重复的entry的sym.name相同
			//当e.scope == null时就表示到了最后一个entry，这个entry就是sentinel，
			//Entry[] table在初始化时，每个table[i]代表一个同hash值的shadowed链，
			//每个shadowed链都以sentinel结束
			while (e.scope != null && e.sym.name != sym.name)
				e = e.shadowed;
			return e;
		}

		public Scope getOrigin() {
			// The origin is only recorded for import scopes.  For all
			// other scope entries, the "enclosing" type is available
			// from other sources.  See Attr.visitSelect and
			// Attr.visitIdent.  Rather than throwing an assertion
			// error, we return scope which will be the same as origin
			// in many cases.
			return scope;
		}
    }

    public static class ImportScope extends Scope {

		public ImportScope(Symbol owner) {
			super(owner);
		}

		@Override
		Entry makeEntry(Symbol sym, Entry shadowed, Entry sibling, Scope scope, Scope origin) {
			return new ImportEntry(sym, shadowed, sibling, scope, origin);
		}

		public Entry lookup(Name name) {
			Entry e = table[name.index & hashMask];
			while (e.scope != null &&
			   (e.sym.name != name ||
				/* Since an inner class will show up in package and
				 * import scopes until its inner class attribute has
				 * been processed, we have to weed it out here.  This
				 * is done by comparing the owners of the entry's
				 * scope and symbol fields.  The scope field's owner
				 * points to where the class originally was imported
				 * from.  The symbol field's owner points to where the
				 * class is situated now.  This can change when an
				 * inner class is read (see ClassReader.enterClass).
				 * By comparing the two fields we make sure that we do
				 * not accidentally import an inner class that started
				 * life as a flat class in a package. */
				e.sym.owner != e.scope.owner))
			e = e.shadowed;
			return e;
		}

		static class ImportEntry extends Entry {
			private Scope origin;

			ImportEntry(Symbol sym, Entry shadowed, Entry sibling, Scope scope, Scope origin) {
				super(sym, shadowed, sibling, scope);
				this.origin = origin;
			}
			public Entry next() {
				Entry e = super.shadowed;
				while (e.scope != null &&
					   (e.sym.name != sym.name ||
					e.sym.owner != e.scope.owner)) // see lookup()
					e = e.shadowed;
				return e;
			}

			@Override
			public Scope getOrigin() { return origin; }
		}
    }

    /** An empty scope, into which you can't place anything.  Used for
     *  the scope for a variable initializer.
     */
    public static class DelegatedScope extends Scope {
		Scope delegatee;
		public static final Entry[] emptyTable = new Entry[0];

		public DelegatedScope(Scope outer) {
			super(outer, outer.owner, emptyTable);
			delegatee = outer;
		}
		public Scope dup() {
			return new DelegatedScope(next);
		}
		public Scope dupUnshared() {
			return new DelegatedScope(next);
		}
		public Scope leave() {
			return next;
		}
		public void enter(Symbol sym) {
			// only anonymous classes could be put here
		}
		public void enter(Symbol sym, Scope s) {
			// only anonymous classes could be put here
		}
		public void remove(Symbol sym) {
			throw new AssertionError(sym);
		}
		public Entry lookup(Name name) {
			return delegatee.lookup(name);
		}
    }

    /** An error scope, for which the owner should be an error symbol. */
    public static class ErrorScope extends Scope {
		ErrorScope(Scope next, Symbol errSymbol, Entry[] table) {
			super(next, /*owner=*/errSymbol, table);
		}
		public ErrorScope(Symbol errSymbol) {
			super(errSymbol);
		}
		public Scope dup() {
			return new ErrorScope(this, owner, table);
		}
		public Scope dupUnshared() {
			return new ErrorScope(this, owner, table.clone());
		}
		public Entry lookup(Name name) {
			Entry e = super.lookup(name);
			if (e.scope == null)
				return new Entry(owner, null, null, null);
			else
				return e;
		}
    }
}
