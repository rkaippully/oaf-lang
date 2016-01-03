package org.oaflang.inference.typecheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.oaflang.inference.type.Type;

public class Environment {

	private static final ThreadLocal<Environment> envThreadLocal = new ThreadLocal<Environment>() {

		@Override
		protected Environment initialValue() {
			return new Environment();
		};
	};

	public static Environment getCurrent() {
		return envThreadLocal.get();
	}

	private Stack<Map<String, Type>> envs;
	private int NextTypeVariableNumber = 0;

	private Environment() {
		this.envs = new Stack<>();
		this.envs.push(new HashMap<String, Type>());
	}

	public Type lookup(String name) {
		return envs.peek().get(name);
	}

	public void add(String name, Type type) {
		envs.peek().put(name, type);
	}

	public void extend(String name, Type type) {
		Map<String, Type> newMap = new HashMap<>(envs.peek());
		newMap.put(name, type);
		envs.push(newMap);
	}

	public void restore() {
		envs.pop();
	}

	public void clear() {
		envs.clear();
		envs.push(new HashMap<String, Type>());
		NextTypeVariableNumber = 0;
	}

	/**
	 * Get the types mentioned in the environment.
	 */
	public Collection<Type> getTypes() {
		return envs.peek().values();
	}

	@Override
	public String toString() {
		return envs.peek().toString();
	}

	public int getNextTypeVariableNumber() {
		return NextTypeVariableNumber++;
	}
}
