/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package kourendlibrary;

import java.util.HashMap;
import java.util.Map;

public enum LibraryCustomer
{
	VILLIA(7047, "Villia"),
	PROFESSOR_GRACKLEBONE(7048, "Prof. Gracklebone"),
	SAM(7049, "Sam");

	private final int id;
	private final String name;

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	private static final Map<Integer, LibraryCustomer> byId = buildIdMap();

	LibraryCustomer(int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public static LibraryCustomer getById(int id)
	{
		return byId.get(id);
	}

	private static Map<Integer, LibraryCustomer> buildIdMap()
	{
		Map<Integer, LibraryCustomer> byId = new HashMap<>();
		for (LibraryCustomer c : values())
		{
			byId.put(c.id, c);
		}
		return byId;
	}
}
