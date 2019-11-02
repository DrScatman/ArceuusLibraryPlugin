/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018 Psikoi
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

import kourendlibrary.data.ImageUtil;
import org.rspeer.ui.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KourendLibraryPanel extends PluginPanel
{
	private static final ImageIcon RESET_ICON;
	private static final ImageIcon RESET_CLICK_ICON;

	private Library library = Library.getInstance();
	private static KourendLibraryPanel panel = null;

	private final HashMap<Book, BookPanel> bookPanels = new HashMap<>();

	private KourendLibraryPanel() { }
	public static KourendLibraryPanel getInstance() {
		if (panel == null) {
			panel = new KourendLibraryPanel();
		}
		return panel;
	}

	static
	{
	    File img = new File(System.getProperty("user.home") + "\\IdeaProjects\\ArceussLibrary\\src\\kourendlibrary\\data\\reset.png");

	    BufferedImage resetIcon = null;
        try {
            resetIcon = ImageIO.read(img);
        } catch (IOException e) {
            Log.severe(e);
            e.printStackTrace();
        }
        RESET_ICON = new ImageIcon(resetIcon);
        RESET_CLICK_ICON = new ImageIcon(ImageUtil.alphaOffset(resetIcon, -100));
	}

	public void init()
	{
		setLayout(new BorderLayout(0, 5));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(Color.DARK_GRAY.darker());

		JPanel books = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		Stream.of(Book.values())
			.filter(b -> !b.isDarkManuscript())
			.sorted(Comparator.comparing(Book::getShortName))
			.forEach(b ->
			{
				BookPanel p = new BookPanel(b);
				bookPanels.put(b, p);
				books.add(p, c);
				c.gridy++;
			});

		JButton reset = new JButton("Reset", RESET_ICON);
		reset.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				reset.setIcon(RESET_CLICK_ICON);
				library.reset();
				update();
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				reset.setIcon(RESET_ICON);
			}
		});

		add(reset, BorderLayout.NORTH);
		add(books, BorderLayout.CENTER);
		update();
	}

	public void update()
	{
		SwingUtilities.invokeLater(() ->
		{
			Book customerBook = library.getCustomerBook();
			for (Map.Entry<Book, BookPanel> b : bookPanels.entrySet())
			{
				b.getValue().setIsTarget(customerBook == b.getKey());
			}

			HashMap<Book, HashSet<String>> bookLocations = new HashMap<>();

			for (Bookcase bookcase : library.getBookcases())
			{
				if (bookcase.getBook() != null)
				{
					bookLocations.computeIfAbsent(bookcase.getBook(), a -> new HashSet<>()).add(bookcase.getLocationString());
				}
				else
				{
					for (Book book : bookcase.getPossibleBooks())
					{
						if (book != null)
						{
							bookLocations.computeIfAbsent(book, a -> new HashSet<>()).add(bookcase.getLocationString());
						}
					}
				}
			}

			for (Map.Entry<Book, BookPanel> e : bookPanels.entrySet())
			{
				HashSet<String> locs = bookLocations.get(e.getKey());
				if (locs == null || locs.size() > 3)
				{
					e.getValue().setLocation("Unknown");
				}
				else
				{
					e.getValue().setLocation("<html>" + locs.stream().collect(Collectors.joining("<br>")) + "</html>");
					Log.info("<html>" + locs.stream().collect(Collectors.joining("<br>")) + "</html>");
				}
			}
		});
	}
}