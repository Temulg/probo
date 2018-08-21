/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

import java.util.ArrayList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Application {
	public static void main(String... args) {
		var app = new Application();
		var jc = JCommander.newBuilder().addObject(
			app
		).build();

		jc.parse(args);

		int rc = 0;
		if (!app.help)
			rc = app.run();
		else
			jc.usage();

		System.exit(rc);
	}

	int run() {
		var uc = new IncomingUnitCollector();

		for (var p: paths)
			uc.collectFrom(p);

		uc.prepareUnits();

		return 0;
	}

	@Parameter(
		names = {"-h", "--help"}, help = true,
		description = "display this help and exit"
	)
	boolean help;

	@Parameter(description = "[test class paths]...")
	ArrayList<String> paths = new ArrayList<>();
}
