package org.oaflang.inference.util;

import java.util.ArrayList;
import java.util.List;

public class Lists {

	/**
	 * Returns a list that is the intersection of l1 and l2.
	 */
	public static <T> List<T> intersect(List<? extends T> l1,
			List<? extends T> l2) {
		List<T> result = new ArrayList<>();
		for (T val : l1)
			if (l2.contains(val))
				result.add(val);
		return result;
	}

	public static <T> void addAllIfAbsent(List<T> target,
			List<? extends T> source) {
		for (T val : source) {
			if (!target.contains(val))
				target.add(val);
		}
	}

	public static <T> List<T> list(T e1) {
		ArrayList<T> result = new ArrayList<>(1);
		result.add(e1);
		return result;
	}

	public static <T> List<T> list(T e1, T e2) {
		ArrayList<T> result = new ArrayList<>(2);
		result.add(e1);
		result.add(e2);
		return result;
	}
}
