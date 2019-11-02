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
package main;

import kourendlibrary.*;
import kourendlibrary.data.NavigationButton;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.ItemTables;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.*;
import org.rspeer.runetek.event.types.*;
import org.rspeer.runetek.providers.RSItemTable;
import org.rspeer.runetek.providers.subclass.GameCanvas;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*@PluginDescriptor(
	name = "Kourend Library",
	description = "Show where the books are found in the Kourend Library",
	tags = {"main", "magic", "runecrafting", "overlay", "panel"}*/
//)
//@Slf4j
@ScriptMeta(name = "Arceuus Library", desc = "Gets arceuus", developer = "DrScatman")
public class KourendLibraryPlugin extends Script
		implements AnimationListener, MenuActionListener, ChatMessageListener, ItemTableListener, TickListener, RenderListener
{
	private static final Pattern BOOK_EXTRACTOR = Pattern.compile("'<col=0000ff>(.*)</col>'");
	private static final Pattern TAG_MATCHER = Pattern.compile("(<[^>]*>)");
	public final static int REGION = 6459;

	public final static boolean debug = true;
	//private ClientToolbar clientToolbar;
	private Library library;
	//private OverlayManager overlayManager;
	private KourendLibraryOverlay overlay;
	//private KourendLibraryConfig config;
	//TODO: Add images... maybe
	//private ItemManager itemManager;

	private KourendLibraryPanel panel;
	private NavigationButton navButton;
	private boolean buttonAttached = false;
	private Position lastBookcaseClick = null;
	private Position lastBookcaseAnimatedOn = null;
	private EnumSet<Book> playerBooks = null;

	/*@Provides
	KourendLibraryConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(KourendLibraryConfig.class);
	}*/
	private int floorLevel = 0;
	private Area centerArea, neArea, nwArea, swArea;

	private void setAreas(int level) {
		centerArea = Area.rectangular(1622, 3817, 1641, 3799, level);
		neArea = Area.rectangular(1638, 3832, 1659, 3813, level);
		nwArea = Area.rectangular(1606, 3832, 1627, 3813, level);
		swArea = Area.rectangular(1606, 3802, 1627, 3782, level);
	}

	private Position bookPosition = null;

	public void setBookPosition(Position position) {
		bookPosition = position;
	}

	@Override
	public int loop() {
		setAreas(floorLevel);

		if (!playerAtLibrary()) {
			Movement.walkTo(centerArea.getCenter());
		}

		Book book = library.getCustomerBook();

		if (bookPosition == null && book != null) {
			bookPosition = findBookPosition(bCase -> bCase.getBook() == book || bCase.getPossibleBooks().contains(book));

			if (bookPosition == null) {
				bookPosition = findBookPosition(bCase -> bCase.getBook() != null || bCase.getPossibleBooks().stream().anyMatch(Objects::nonNull));
			}
		} else {
			Movement.walkTo(bookPosition);
		}

		return 1000;
	}

	private Position findBookPosition(Predicate<Bookcase> predicate) {
		List<Bookcase> byLevel =  library.getBookcasesOnLevel(floorLevel);
		for (Bookcase bCase : byLevel) {
			if (predicate.test(bCase)) {
				Log.fine("Book Found in Loop: " + bCase.getLocationString());
				return bCase.getLocation();
			}
		}

		List<Bookcase> all =  library.getBookcases();
		for (Bookcase bCase : all) {
			if (predicate.test(bCase)) {
				Log.fine("Book Found in Loop: " + bCase.getLocationString());
				return bCase.getLocation();
			}
		}
		return null;
	}

	private boolean playerAtLibrary() {
		Player me = Players.getLocal();
		for (int i = 0; i < 3; i ++) {
			setAreas(i);
			if (centerArea.contains(me) || neArea.contains(me)
					|| nwArea.contains(me) || swArea.contains(me)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onStart()
	{
		if (!GameCanvas.isInputEnabled()) {
			GameCanvas.setInputEnabled(true);
		}
		library = Library.getInstance();
		overlay = KourendLibraryOverlay.getInstance(library, this);
		Book.fillImages(/*itemManager*/);
		panel = KourendLibraryPanel.getInstance();
		panel.init();

		//overlayManager.add(overlay);

		updatePlayerBooks();

		//config.hideButton();//clientToolbar.addNavigation(navButton);
	}

	@Override
	public void onStop()
	{
		overlay.setHidden(true);
		/*overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);*/
		buttonAttached = false;
		lastBookcaseClick = null;
		lastBookcaseAnimatedOn = null;
		playerBooks = null;
	}

	/*@Subscribe
	public void onConfigChanged(ConfigChanged ev)
	{
		if (!KourendLibraryConfig.GROUP_KEY.equals(ev.getGroup()))
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			if (!config.hideButton())
			{
				clientToolbar.addNavigation(navButton);
			}
			else
			{
				Player lp = client.getLocalPlayer();
				boolean inRegion = lp != null && lp.getWorldLocation().getRegionID() == REGION;
				if (inRegion)
				{
					clientToolbar.addNavigation(navButton);
				}
				else
				{
					clientToolbar.removeNavigation(navButton);
				}
			}
		});
	}*/

	@Override
	public void notify(MenuActionEvent menuOpt) {
		if (ActionOpcodes.OBJECT_ACTION_0 == menuOpt.getOpcode() && menuOpt.getTarget().contains("Bookshelf"))
		{
			//TODO: might be wrong position
			lastBookcaseClick = new Position(menuOpt.getPrimaryArg(), menuOpt.getSecondaryArg(), Players.getLocal().getFloorLevel());
			overlay.setHidden(false);
		}
	}

	@Override
	public void notify(AnimationEvent anim) {
		if (anim.getSource().equals(Players.getLocal()) && anim.getCurrent() == AnimationEvent.TYPE_STARTED)
		{
			lastBookcaseAnimatedOn = lastBookcaseClick;
		}
	}

	@Override
	public void notify(ChatMessageEvent event) {
		if (lastBookcaseAnimatedOn != null && event.getType() == ChatMessageType.GAME)
		{
			if (event.getMessage().equals("You don't find anything useful here."))
			{
				library.mark(lastBookcaseAnimatedOn, null);
				panel.update();
				lastBookcaseAnimatedOn = null;
			}
		}
	}

	@Override
	public void notify(TickEvent tickEvent) {
		boolean inRegion = Players.getLocal().getPosition().getRegionId() == REGION;
		if (/*config.hideButton() && */inRegion != buttonAttached)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (inRegion)
				{
					//clientToolbar.addNavigation(navButton);
				}
				else
				{
					//clientToolbar.removeNavigation(navButton);
				}
			});
			buttonAttached = inRegion;
		}

		if (!inRegion)
		{
			return;
		}

		if (lastBookcaseAnimatedOn != null)
		{
			InterfaceComponent find = Interfaces.getComponent(193, 1);
			//Widget find = client.getWidget(WidgetInfo.DIALOG_SPRITE_SPRITE);
			if (find != null)
			{
				Book book = Book.byId(find.getItemId());
				if (book != null)
				{
					library.mark(lastBookcaseAnimatedOn, book);
					panel.update();
					lastBookcaseAnimatedOn = null;
				}
			}
		}

		InterfaceComponent npcHead = Interfaces.getComponent(231, 1);
		//Widget npcHead = client.getWidget(WidgetInfo.DIALOG_NPC_HEAD_MODEL);
		if (npcHead != null && npcHead.isVisible())
		{
			LibraryCustomer cust = LibraryCustomer.getById(npcHead.getModelId());
			if (cust != null) {
				InterfaceComponent textw = Interfaces.getComponent(231, 4);
				if (textw != null && textw.isVisible()) {
					String text = textw.getText();
					Matcher m = BOOK_EXTRACTOR.matcher(text);
					if (m.find()) {
						String bookName = TAG_MATCHER.matcher(m.group(1).replace("<br>", " ")).replaceAll("");
						Book book = Book.byName(bookName);
						if (book == null) {
							Log.severe("Book '{}' is not recognised", bookName);
							return;
						}

						overlay.setHidden(false);
						library.setCustomer(cust, book);
						panel.update();
					} else if (text.contains("You can have this other book") || text.contains("please accept a token of my thanks.") || text.contains("Thanks, I'll get on with reading it.")) {
						library.setCustomer(null, null);
						panel.update();
					}
				}
			}
		}
	}

	@Override
	public void notify(ItemTableEvent itemTableEvent) {
		updatePlayerBooks();
	}

	public boolean doesPlayerContainBook(Book book)
	{
		return playerBooks.contains(book);
	}

	private void updatePlayerBooks()
	{
		RSItemTable itemContainer = ItemTables.lookup(ItemTables.INVENTORY);

		if (itemContainer != null)
		{
			EnumSet<Book> books = EnumSet.noneOf(Book.class);

			for (int itemId : itemContainer.getIds())
			{
				Book book = Book.byId(itemId);

				if (book != null)
				{
					books.add(book);
				}
			}

			playerBooks = books;
		}
	}

	@Override
	public void notify(RenderEvent renderEvent) {
		if (bookPosition != null) {
			Graphics g = renderEvent.getSource();
			g.setColor(Color.GREEN);
			bookPosition.outline(g);
		}
		if (overlay != null) {
			overlay.notify(renderEvent);
		}
	}
}