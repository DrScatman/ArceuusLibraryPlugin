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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Book
{
	DARK_MANUSCRIPT_13514(13514),
	DARK_MANUSCRIPT_13515(13515),
	DARK_MANUSCRIPT_13516(13516),
	DARK_MANUSCRIPT_13517(13517),
	DARK_MANUSCRIPT_13518(13518),
	DARK_MANUSCRIPT_13519(13519),
	DARK_MANUSCRIPT_13520(13520),
	DARK_MANUSCRIPT_13521(13521),
	DARK_MANUSCRIPT_13522(13522),
	DARK_MANUSCRIPT_13523(13523),

	RADAS_CENSUS(13524, "Rada's Census", "Census of King Rada III, by Matthias Vorseth."),
	RICKTORS_DIARY_7(13525, "Ricktor's Diary 7", "Diary of Steklan Ricktor, volume 7."),
	EATHRAM_RADA_EXTRACT(13526, "Eathram & Rada extract", "An extract from Eathram & Rada, by Anonymous."),
	KILLING_OF_A_KING(13527, "Killing of a King", "Killing of a King, by Griselle."),
	HOSIDIUS_LETTER(13528, "Hosidius Letter", "A letter from Lord Hosidius to the Council of Elders."),
	WINTERTODT_PARABLE(13529, "Wintertodt Parable", "The Parable of the Wintertodt, by Anonymous."),
	TWILL_ACCORD(13530, "Twill Accord", "The Royal Accord of Twill."),
	BYRNES_CORONATION_SPEECH(13531, "Byrnes Coronation Speech", "Speech of King Byrne I, on the occasion of his coronation."),
	IDEOLOGY_OF_DARKNESS(13532, "The Ideology of Darkness", "The Ideology of Darkness, by Philophaire."),
	RADAS_JOURNEY(13533, "Rada's Journey", "The Journey of Rada, by Griselle."),
	TRANSVERGENCE_THEORY(13534, "Transvergence Theory", "The Theory of Transvergence, by Amon Ducot."),
	TRISTESSAS_TRAGEDY(13535, "Tristessa's Tragedy", "The Tragedy of Tristessa."),
	TREACHERY_OF_ROYALTY(13536, "The Treachery of Royalty", "The Treachery of Royalty, by Professor Answith."),
	TRANSPORTATION_INCANTATIONS(13537, "Transportation Incantations", "Transportation Incantations, by Amon Ducot."),
	SOUL_JOURNEY(19637, "Soul Journey", "The Journey of Souls, by Aretha."),
	VARLAMORE_ENVOY(21756, "Varlamore Envoy", "The Envoy to Varlamore, by Deryk Paulson.");

	private static final Map<Integer, Book> BY_ID = buildById();

	private static final Map<String, Book> BY_NAME = buildByName();

	private static Map<Integer, Book> buildById()
	{
		HashMap<Integer, Book> byId = new HashMap<>();
		for (Book b : Book.values())
		{
			byId.put(b.item, b);
		}
		return Collections.unmodifiableMap(byId);
	}

	private static Map<String, Book> buildByName()
	{
		HashMap<String, Book> byName = new HashMap<>();
		for (Book b : Book.values())
		{
			if (!b.isDarkManuscript)
			{
				byName.put(b.name, b);
			}
		}
		return Collections.unmodifiableMap(byName);
	}

	public static Book byId(int id)
	{
		return BY_ID.get(id);
	}
	public static Book byName(String name)
	{
		return BY_NAME.get(name);
	}

	private final int item;
	private final String name;
	private final String shortName;
	//private AsyncBufferedImage icon;
	private final boolean isDarkManuscript;

	Book(int id, String shortName, String name)
	{
		this.item = id;
		this.isDarkManuscript = false;
		this.shortName = shortName;
		this.name = name;
	}

	Book(int id)
	{
		this.item = id;
		this.isDarkManuscript = true;
		this.name = "Dark Manuscript";
		this.shortName = "Dark Manuscript";
	}

	public static void fillImages(/*ItemManager itemManager*/)
	{
		for (Book b : values())
		{
			//TODO: add images... maybe
			//b.icon = itemManager.getImage(b.item);
		}
	}

	public int getItem() {
		return item;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	/*public AsyncBufferedImage getIcon() {
		return icon;
	}*/

	public boolean isDarkManuscript() {
		return isDarkManuscript;
	}
}