/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner.units;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

import temulg.probo.Context;
import temulg.probo.runner.Throwables;

public interface TestClassInstance {
	void create(Context cxt);

	void dispose();

	Object get();

	public static TestClassInstance make(
		Class<?> cls, MethodHandles.Lookup l
	) {
		Constructor<?> defCons = null, ctxCons = null;

		for (var c: cls.getConstructors()) {
			switch (c.getParameterCount()) {
			case 1:
				if (
					c.getParameters()[0].getType()
					== Context.class
				)
					ctxCons = c;
				break;
			case 0:
				defCons = c;
				break;
			}
		}

		var closeable = AutoCloseable.class.isAssignableFrom(cls);

		if (ctxCons != null) {
			try {
				var h = l.unreflectConstructor(ctxCons);
				return closeable
					? new Impl3(h)
					: new Impl2(h);
			} catch (IllegalAccessException e) {
			}
		}

		if (defCons != null) {
			try {
				var h = l.unreflectConstructor(defCons);
				return closeable
					? new Impl1(h)
					: new Impl0(h);
			} catch (IllegalAccessException e) {
			}
		}

		throw Throwables.propagate(new IllegalAccessException(
			"No suitable constructors for class " + cls
		));
	}

	static class Impl0 implements TestClassInstance {
		private Impl0(MethodHandle consHandle_) {
			consHandle = consHandle_;
		}

		@Override
		public void create(Context cxt) {
			try {
				value = consHandle.invoke();
			} catch (Throwable t) {
				Throwables.propagate(t);
			}
		}

		@Override
		public void dispose() {
			value = null;
		}

		@Override
		public Object get() {
			return value;
		}

		private final MethodHandle consHandle;
		private volatile Object value;
	}

	static class Impl1 implements TestClassInstance {
		private Impl1(MethodHandle consHandle_) {
			consHandle = consHandle_;
		}

		@Override
		public void create(Context cxt) {
			try {
				value = (AutoCloseable)consHandle.invoke();
			} catch (Throwable t) {
				Throwables.propagate(t);
			}
		}

		@Override
		public void dispose() {
			var value_ = value;
			value = null;
			try {
				value_.close();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}

		@Override
		public Object get() {
			return value;
		}

		private final MethodHandle consHandle;
		private volatile AutoCloseable value;
	}

	static class Impl2 implements TestClassInstance {
		private Impl2(MethodHandle consHandle_) {
			consHandle = consHandle_;
		}

		@Override
		public void create(Context ctx) {
			try {
				value = consHandle.invoke(ctx);
			} catch (Throwable t) {
				Throwables.propagate(t);
			}
		}

		@Override
		public void dispose() {
			value = null;
		}

		@Override
		public Object get() {
			return value;
		}

		private final MethodHandle consHandle;
		private volatile Object value;
	}

	static class Impl3 implements TestClassInstance {
		private Impl3(MethodHandle consHandle_) {
			consHandle = consHandle_;
		}

		@Override
		public void create(Context ctx) {
			try {
				value = (AutoCloseable)consHandle.invoke(ctx);
			} catch (Throwable t) {
				Throwables.propagate(t);
			}
		}

		@Override
		public void dispose() {
			var value_ = value;
			value = null;
			try {
				value_.close();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}

		@Override
		public Object get() {
			return value;
		}

		private final MethodHandle consHandle;
		private volatile AutoCloseable value;
	}
}
