/*
 * Copyright (c) 2018 Alex Dubov <oakad@yahoo.com>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package temulg.probo.runner;

public class Throwables {
	private Throwables() {}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> RuntimeException propagate(
		Throwable t
	) throws T {
		throw (T)t;
	}
}