/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedTransferQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class IncomingUnitCollector extends ClassLoader {
	void collectFrom(String pathSpec) {
		pendingTasks.offer(ForkJoinPool.commonPool().submit(
			new PathSpecScanner(pathSpec, this)
		));
	}

	void collectFile(Path p) {
		if (!classPathMatcher.matches(p.getFileName()))
			return;

		pendingTasks.offer(ForkJoinPool.commonPool().submit(
			new TestClassInfo.Loader(p, this)
		));
	}

	void addClassInfo(byte[] data) {
		var info = new TestClassInfo(defineClass(
			null, data, 0, data.length
		));

		var prev = classInfo.putIfAbsent(
			info.cls.getCanonicalName(), info
		);

		if (prev == null)
			info.analyze(l);
		else {
		}
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

		classInfo.forEach((k, v) -> {
			System.out.format("-- %s, %s\n", k, v.cls);
		});
	}

	private static final Logger LOGGER = LogManager.getLogger();

	final MethodHandles.Lookup l = MethodHandles.lookup();
	final FileSystem fs = FileSystems.getDefault();
	final PathMatcher classPathMatcher = fs.getPathMatcher(
		"glob:*.class"
	);

	private final ConcurrentHashMap<
		String, TestClassInfo
	> classInfo = new ConcurrentHashMap<>();
	private final LinkedTransferQueue<
		ForkJoinTask<?>
	> pendingTasks = new LinkedTransferQueue<>();
}
