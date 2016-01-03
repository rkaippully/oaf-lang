package org.oaflang.inference.util;

public class Pair<A, B> {

	private final A fst;
	private final B snd;

	public Pair(A fst, B snd) {
		this.fst = fst;
		this.snd = snd;
	}

	public A fst() {
		return fst;
	}

	public B snd() {
		return snd;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", fst, snd);
	}
}
