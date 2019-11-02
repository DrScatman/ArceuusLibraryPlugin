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

import main.KourendLibraryPlugin;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.movement.position.ScreenPosition;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.types.RenderEvent;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class KourendLibraryOverlay {
    private final static int MAXIMUM_DISTANCE = 24;
    private final Library library;
    //private final KourendLibraryConfig config;
    private final KourendLibraryPlugin plugin;

    private boolean hidden;

    private KourendLibraryOverlay(Library library, KourendLibraryPlugin plugin) {
        this.library = library;
        //this.config = config;
        this.plugin = plugin;
/*		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);*/
    }

    private static KourendLibraryOverlay overlay;
    public static KourendLibraryOverlay getInstance(Library library, KourendLibraryPlugin plugin) {
    	if (overlay == null) {
    		overlay = new KourendLibraryOverlay(library, plugin);
		}
    	return overlay;
	}

    public void notify(RenderEvent render) {
        Graphics g = render.getSource();
        if (hidden) {
            return;
        }

        Player player = Players.getLocal();
        if (player == null) {
            return;
        }
        Position playerLoc = player.getPosition();

        if (playerLoc.getRegionId() != KourendLibraryPlugin.REGION) {
            return;
        }

        List<Bookcase> allBookcases = library.getBookcasesOnLevel(Game.getClient().getFloorLevel());

        for (Bookcase bookcase : allBookcases) {
            // AABB
            Position caseLoc = bookcase.getLocation();
            if (Math.abs(playerLoc.getX() - caseLoc.getX()) > MAXIMUM_DISTANCE
                    || Math.abs(playerLoc.getY() - caseLoc.getY()) > MAXIMUM_DISTANCE) {
                continue;
            }

            SceneObject localBookcase = SceneObjects.getFirstAt(caseLoc);
            if (localBookcase == null || !caseLoc.isLoaded()) {
                continue;
            }

            ScreenPosition screenBookcase = localBookcase.getPosition().toScreen();
            if (screenBookcase != null && Game.getCanvas().contains(screenBookcase.toPoint())) {
                boolean bookIsKnown = bookcase.isBookSet();
                Book book = bookcase.getBook();
                Set<Book> possible = bookcase.getPossibleBooks();
                if (bookIsKnown && book == null) {
                    for (Book b : possible) {
                        if (b != null && b.isDarkManuscript()) {
                            book = b;
                            break;
                        }
                    }
                }

                if (!bookIsKnown && possible.size() == 1) {
                    book = possible.iterator().next();
                    bookIsKnown = true;
                }
                Color color = bookIsKnown ? Color.ORANGE : Color.WHITE;

                // Render the poly on the floor
                if (!(bookIsKnown && book == null) && (library.getState() == SolvedState.NO_DATA || book != null || !possible.isEmpty()) && !shouldHideOverlayIfDuplicateBook(book)) {
                    Position poly = localBookcase.getPosition();
                    if (poly != null) {
                        g.setColor(color);
                        poly.outline(g);
                    }
                }

                int height = 0;
                // If the book is singled out, render the text and the book's icon
                if (bookIsKnown) {
                    if (book != null && !shouldHideOverlayIfDuplicateBook(book)) {
                        FontMetrics fm = g.getFontMetrics();
                        Rectangle2D bounds = fm.getStringBounds(book.getShortName(), g);
                        height = (int) bounds.getHeight();
                        Point textLoc = new Point(
                                (int) (screenBookcase.getX() - (bounds.getWidth() / 2)),
                                screenBookcase.getY() - (height / 2) + (int) bounds.getHeight()
                        );
                        g.setColor(color);
                        //TODO: Remove?
                        plugin.setBookPosition(localBookcase.getPosition());
                        g.drawString(book.getShortName(), textLoc.x, textLoc.y);
							/*g.drawImage(
									book.getIcon(),
									screenBookcase.getX() - (book.getIcon().getWidth() / 2),
									screenBookcase.getY() + (height / 2) - book.getIcon().getHeight(),
									null
							);*/
                    }
                } else {
                    // otherwise render up to 9 icons of the possible books in the bookcase in a square
                    final int BOOK_ICON_SIZE = 32;
                    Book[] books = possible.stream()
                            .filter(Objects::nonNull)
                            .limit(9)
                            .toArray(Book[]::new);
                    if (books.length > 1 && books.length <= 9) {
                        int cols = (int) Math.ceil(Math.sqrt(books.length));
                        int rows = (int) Math.ceil((double) books.length / cols);
                        height = rows * BOOK_ICON_SIZE;
                        int xbase = screenBookcase.getX() - ((cols * BOOK_ICON_SIZE) / 2);
                        int ybase = screenBookcase.getY() - rows * BOOK_ICON_SIZE / 2;

                        for (int i = 0; i < books.length; i++) {
                            int col = i % cols;
                            int row = i / cols;
                            int x = col * BOOK_ICON_SIZE;
                            int y = row * BOOK_ICON_SIZE;
                            if (row == rows - 1) {
                                x += (BOOK_ICON_SIZE * (books.length % rows)) / 2;
                            }
                            //plugin.setBookPosition(localBookcase.getPosition());
                            g.drawString(books[i].getShortName(), xbase + x, ybase + y);
                        }
                    }
                }

                // Draw the bookcase's ID on top
                if (KourendLibraryPlugin.debug) {
                    FontMetrics fm = g.getFontMetrics();
                    String str = bookcase.getIndex().stream().map(Object::toString).collect(Collectors.joining(", "));
                    Rectangle2D bounds = fm.getStringBounds(str, g);
                    Point textLoc = new Point((int) (screenBookcase.getX() - (bounds.getWidth() / 2)), screenBookcase.getY() - (height / 2));
                    g.setColor(Color.WHITE);
                    g.drawString(str, textLoc.x, textLoc.y);
                }
            }
        }

        // Render the customer's wanted book on their head and a poly under their feet
        LibraryCustomer customer = library.getCustomer();
        if (customer != null) {
            Npcs.newQuery()
                    .filter(n -> n.getId() == customer.getId())
                    .results()
                    .forEach(n ->
                    {
                        Book b = library.getCustomerBook();
                        boolean doesPlayerContainBook = b != null && plugin.doesPlayerContainBook(b);
                        Position local = n.getPosition();
                        if (local.isLoaded()) {
                            g.setColor(doesPlayerContainBook ? Color.GREEN : Color.WHITE);
                            local.outline(g);
                        }
                        //Point screen = Perspective.localToCanvas(client, local, client.getPlane(), n.getLogicalHeight());
                        if (b != null) {
                            g.drawString(b.getShortName(), n.getX(), n.getY());
                        }
                    });
        }

        return;
    }

    private boolean shouldHideOverlayIfDuplicateBook(Book book) {
        return KourendLibraryConfig.hideDuplicateBook()
                && book != null
                && !book.isDarkManuscript()
                && plugin.doesPlayerContainBook(book);
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
