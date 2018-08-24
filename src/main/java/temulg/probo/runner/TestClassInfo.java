/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import temulg.probo.runner.units.TestClassInstance;
import temulg.probo.runner.units.TestMethodInstance;

class TestClassInfo {
	TestClassInfo(Class<?> cls_) {
		cls = cls_;
	}

	void analyze(MethodHandles.Lookup l) {
		classInstance = TestClassInstance.make(cls, l);
		for (var m: cls.getMethods()) {
			var ti = TestMethodInstance.make(m, l);
			if (ti != null) {
				methodInstances.add(ti);
				continue;
			}
		}
	}

	static class Loader extends RecursiveAction {
		Loader(Path path_, IncomingUnitCollector uc_) {
			path = path_;
			uc = uc_;
		}
	
		@Override
		protected void compute() {
			try {
				uc.addClassInfo(Files.readAllBytes(path));
			} catch (IOException e) {
				Throwables.propagate(e);
			}
		}

		private static final long serialVersionUID
		= 0x9b6a35928dd6b69dL;

		private final Path path;
		private final IncomingUnitCollector uc;
	}
	
	static private final Logger LOGGER = LogManager.getLogger();

	final Class<?> cls;
	private TestClassInstance classInstance;
	private final ArrayList<
		TestMethodInstance
	> methodInstances = new ArrayList<>();
}
