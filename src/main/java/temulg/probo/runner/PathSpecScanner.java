/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.RecursiveAction;

class PathSpecScanner extends RecursiveAction {
	PathSpecScanner(String pathSpec_, IncomingUnitCollector uc_) {
		pathSpec = pathSpec_;
		uc = uc_;
	}

	@Override
	protected void compute() {
		var p = uc.fs.getPath(pathSpec);

		try {
			Files.walkFileTree(p, fileVisitor);
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}

	private static final long serialVersionUID = 0x659b664b575e6d5bL;

	private final String pathSpec;
	private final IncomingUnitCollector uc;
	private final SimpleFileVisitor<
		Path
	> fileVisitor = new SimpleFileVisitor<Path>() {
		@Override
		public FileVisitResult visitFile(
			Path p, BasicFileAttributes attrs
		) throws IOException {
			if (!attrs.isRegularFile())
				return FileVisitResult.CONTINUE;

			uc.collectFile(p);

			return FileVisitResult.CONTINUE;	
		}
	};
}
