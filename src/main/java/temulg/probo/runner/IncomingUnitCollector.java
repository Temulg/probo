/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedTransferQueue;

class IncomingUnitCollector {
	void collectFrom(String pathSpec) {
		pendingTasks.offer(ForkJoinPool.commonPool().submit(
			new PathSpecScanner(pathSpec, this)
		));
	}

	void collectFile(Path p) {
		if (!classPathMatcher.matches(p.getFileName()))
			return;

		pendingTasks.offer(ForkJoinPool.commonPool().submit(
			new Unit.Loader(p, this)
		));
	}

	void addUnit(Unit u) {
		units.computeIfAbsent(u.s, s -> {
			return u;
		});
	}

	void prepareUnits() {
		while (true) {
			var t = pendingTasks.poll();
			if (t == null)
				break;

			try {
				t.get();
			} catch (InterruptedException | ExecutionException e) {
				Throwables.propagate(e);
			}
		}

		units.forEach((k, v) -> {
			System.out.format("-- %s\n", k);
		});
	}

	final ClassLoader loader = this.getClass().getClassLoader();
	final FileSystem fs = FileSystems.getDefault();
	final PathMatcher classPathMatcher = fs.getPathMatcher(
		"glob:*.class"
	);

	private final ConcurrentHashMap<
		String, Unit
	> units = new ConcurrentHashMap<>();
	private final LinkedTransferQueue<
		ForkJoinTask<?>
	> pendingTasks = new LinkedTransferQueue<>();
}
