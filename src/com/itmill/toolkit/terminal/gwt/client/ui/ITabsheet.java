package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.UIDL;

public class ITabsheet extends TabPanel implements Paintable {
	
	public static final String CLASSNAME = "i-tabsheet";

	String id;

	ApplicationConnection client;

	ArrayList tabKeys = new ArrayList();

	ArrayList captions = new ArrayList();

	int activeTabIndex = 0;
	
	private Element deco;

	TabListener tl = new TabListener() {

		public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
			ITabsheet.this.client.updateVariable(id, "selected", tabIndex,
					true);
		}

		public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
			return true;
		}

	};

	public ITabsheet() {
		setStyleName(CLASSNAME);
		
		addTabListener(new TabListener() {

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
				if (client != null && activeTabIndex != tabIndex)
					ITabsheet.this.client.updateVariable(id, "selected", ""
							+ tabKeys.get(tabIndex), true);
			}

			public boolean onBeforeTabSelected(SourcesTabEvents sender,
					int tabIndex) {
				return true;
			}

		});
		
	}

	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		this.client = client;
		id = uidl.getId();
		
		DeckPanel dp = getDeckPanel();
		dp.setStyleName(CLASSNAME+"-content");
		
		TabBar tb = getTabBar();
		tb.setStyleName(CLASSNAME+"-tabs");

		// Add a decoration element for shadow
		// TODO refactor tabsheet with plain DIV-implementation
		/*if(!DOM.compare(deco, null)) {
			DOM.removeChild(DOM.getParent(getElement()), deco);
			deco = null;
		}
		deco = DOM.createDiv();
		DOM.setElementProperty(deco, "className", CLASSNAME+"-deco");
		DOM.appendChild(DOM.getParent(getElement()), deco);
		*/
		// Adjust width and height
		String h = uidl.hasAttribute("height")? uidl.getStringAttribute("height") : "";
		String w = uidl.hasAttribute("width")? uidl.getStringAttribute("width") : "";
		setWidth(w!=""?w:"auto");
		//DOM.setStyleAttribute(deco, "width", w!=""?w:"auto");
		dp.setHeight(h!=""?h:"auto");

		UIDL tabs = uidl.getChildUIDL(0);
		boolean keepCurrentTabs = tabKeys.size() == tabs.getNumberOfChildren();
		for (int i = 0; keepCurrentTabs && i < tabKeys.size(); i++)
			keepCurrentTabs = tabKeys.get(i).equals(
					tabs.getChildUIDL(i).getStringAttribute("key"))
					&& captions.get(i).equals(
							tabs.getChildUIDL(i).getStringAttribute("caption"));
		if (keepCurrentTabs) {
			int index = 0;
			for (Iterator it = tabs.getChildIterator(); it.hasNext();) {
				UIDL tab = (UIDL) it.next();
				if (tab.getBooleanAttribute("selected")) {
					activeTabIndex = index;
					Widget content = client.getWidget(tab
							.getChildUIDL(0));
					tb.selectTab(index);
					remove(index);
					insert(content, (String) captions.get(index), index);
					this.selectTab(index);
					((Paintable)content).updateFromUIDL(tab
							.getChildUIDL(0), client);
				}
				index++;
			}
		} else {
			tabKeys.clear();
			captions.clear();
			clear();
			int index = 0;
			for (Iterator it = tabs.getChildIterator(); it.hasNext();) {
				UIDL tab = (UIDL) it.next();
				String key = tab.getStringAttribute("key");
				String caption = tab.getStringAttribute("caption");
				captions.add(caption);
				tabKeys.add(key);
				if (tab.getBooleanAttribute("selected")) {
					activeTabIndex = index;
					Widget content = client.getWidget(tab
							.getChildUIDL(0));
					this.add(content, caption);
					this.selectTab(this.getWidgetIndex(content));
					((Paintable)content).updateFromUIDL(tab
							.getChildUIDL(0), client);
				} else {
					this.add(new Label(), caption);
				}
				index++;
			}
		}

	}
}
