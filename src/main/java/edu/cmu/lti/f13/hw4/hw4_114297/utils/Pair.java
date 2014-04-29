package edu.cmu.lti.f13.hw4.hw4_114297.utils;

import java.util.Arrays;
import java.util.Comparator;

public class Pair<T1 extends Comparable<T1>, T2 extends Comparable<T2>> {
	protected T1 t1;
	protected T2 t2;
	
	public Pair() {}
	
	public Pair(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public T1 getT1() {
		return t1;
	}

	public void setT1(T1 t1) {
		this.t1 = t1;
	}

	public T2 getT2() {
		return t2;
	}

	public void setT2(T2 t2) {
		this.t2 = t2;
	}

	public  Comparator<Pair<T1, T2>> getComparatorT1() {
		return new Comparator<Pair<T1, T2>>() {
			@Override
			public int compare(Pair<T1, T2> o1, Pair<T1, T2> o2) {
				// TODO Auto-generated method stub
				return o1.t1.compareTo(o2.t1);
			}
		};
	}
	
	public Comparator<Pair<T1, T2>> getComparatorT2() {
		return new Comparator<Pair<T1, T2>>() {
			@Override public int compare(Pair<T1, T2> o1, Pair<T1, T2> o2) {
				return o1.t2.compareTo(o2.t2);
			}
		};
	}
	
	public  Comparator<Pair<T1, T2>> getComparatorT1Inverted() {
		return new Comparator<Pair<T1, T2>>() {
			@Override public int compare(Pair<T1, T2> o1, Pair<T1, T2> o2) {
				return o2.t1.compareTo(o1.t1);
			}
		};
	}
	
	public Comparator<Pair<T1, T2>> getComparatorT2Inverted() {
		return new Comparator<Pair<T1, T2>>() {
			@Override public int compare(Pair<T1, T2> o1, Pair<T1, T2> o2) {
				return o2.t2.compareTo(o1.t2);
			}
		};
	}

	@Override
	public String toString() {
		return "Pair [t1=" + t1 + ", t2=" + t2 + "]";
	}
}
