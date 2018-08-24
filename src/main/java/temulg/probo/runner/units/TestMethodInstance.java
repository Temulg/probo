/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner.units;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import temulg.probo.Probe;
import temulg.probo.annotation.Test;

public class TestMethodInstance {
	public static TestMethodInstance make(
		Method m, MethodHandles.Lookup l
	) {
		if (Modifier.isStatic(m.getModifiers()))
			return null;

		var t = m.getAnnotation(Test.class);
		if (t == null)
			return null;

		if (m.getParameterCount() < 1) {
			return null;
		}

		if (m.getParameters()[0].getType() != Probe.class) {
			return null;
		}

		try {
			var h = l.unreflect(m);
			return new TestMethodInstance(
				h, m.getParameterCount() - 1
			);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	private TestMethodInstance(MethodHandle mHandle_, int arity_) {
		mHandle = mHandle_;
		arity = arity_;
	}

	public void invoke(
		TestClassInstance ci, Probe p, Object[] args
	) throws Throwable {
		switch (arity) {
		case 0:
			mHandle.invoke(ci.get(), p);
			break;
		case 1:
			mHandle.invoke(ci.get(), p, args[0]);
			break;
		case 2:
			mHandle.invoke(ci.get(), p, args[0], args[1]);
			break;
		case 3:
			mHandle.invoke(ci.get(), p, args[0], args[1], args[2]);
			break;
		case 4:
			mHandle.invoke(
				ci.get(), p, args[0], args[1], args[2], args[3]
			);
			break;
		default:
			var args_ = new Object[arity + 2];
			args_[0] = ci.get();
			args_[1] = p;
			System.arraycopy(args, 0, args_, 2, arity);
			mHandle.invokeWithArguments(args_);
		}
	}

	private final MethodHandle mHandle;
	private final int arity;
}
