/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

import java.nio.file.Path;
import java.util.concurrent.RecursiveAction;

class Unit {
	private Unit(Class<?> cls_, String s_) {
		cls = cls_;
		s = s_;
	}

	static class Loader extends RecursiveAction {
		Loader(Path path_, IncomingUnitCollector uc_) {
			path = path_;
			uc = uc_;
		}

		@Override
		protected void compute() {
			uc.addUnit(new Unit(Object.class, path.toString()));
		}

		private static final long serialVersionUID = 0x9b6a35928dd6b69dL;

		private final Path path;
		private final IncomingUnitCollector uc;
	}

	private final Class<?> cls;
	final String s;
}
