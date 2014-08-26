/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.components.grid.basicfeatures.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.components.grid.basicfeatures.GridBasicFeaturesTest;

public class GridStructureTest extends GridBasicFeaturesTest {

    @Test
    public void testHidingColumn() throws Exception {
        openTestURL();

        // Column 0 should be visible
        List<TestBenchElement> cells = getGridHeaderRowCells();
        assertEquals("Column 0", cells.get(0).getText());

        // Hide column 0
        selectMenuPath("Component", "Columns", "Column 0", "Visible");

        // Column 1 should now be the first cell
        cells = getGridHeaderRowCells();
        assertEquals("Column 1", cells.get(0).getText());
    }

    @Test
    public void testRemovingColumn() throws Exception {
        openTestURL();

        // Column 0 should be visible
        List<TestBenchElement> cells = getGridHeaderRowCells();
        assertEquals("Column 0", cells.get(0).getText());

        // Hide column 0
        selectMenuPath("Component", "Columns", "Column 0", "Remove");

        // Column 1 should now be the first cell
        cells = getGridHeaderRowCells();
        assertEquals("Column 1", cells.get(0).getText());
    }

    @Test
    public void testDataLoadingAfterRowRemoval() throws Exception {
        openTestURL();

        // Remove columns 2,3,4
        selectMenuPath("Component", "Columns", "Column 2", "Remove");
        selectMenuPath("Component", "Columns", "Column 3", "Remove");
        selectMenuPath("Component", "Columns", "Column 4", "Remove");

        // Scroll so new data is lazy loaded
        scrollGridVerticallyTo(1000);

        // Let lazy loading do its job
        sleep(1000);

        // Check that row is loaded
        assertThat(getGridElement().getCell(11, 0).getText(), not("..."));
    }

    @Test
    public void testFreezingColumn() throws Exception {
        openTestURL();

        // Freeze column 2
        selectMenuPath("Component", "Columns", "Column 2", "Freeze");

        WebElement cell = getGridElement().getCell(0, 0);
        assertTrue(cell.getAttribute("class").contains("frozen"));

        cell = getGridElement().getCell(0, 1);
        assertTrue(cell.getAttribute("class").contains("frozen"));
    }

    @Test
    public void testInitialColumnWidths() throws Exception {
        openTestURL();

        WebElement cell = getGridElement().getCell(0, 0);
        assertEquals(100, cell.getSize().getWidth());

        cell = getGridElement().getCell(0, 1);
        assertEquals(150, cell.getSize().getWidth());

        cell = getGridElement().getCell(0, 2);
        assertEquals(200, cell.getSize().getWidth());
    }

    @Test
    public void testColumnWidths() throws Exception {
        openTestURL();

        // Default column width is 100px
        WebElement cell = getGridElement().getCell(0, 0);
        assertEquals(100, cell.getSize().getWidth());

        // Set first column to be 200px wide
        selectMenuPath("Component", "Columns", "Column 0", "Column 0 Width",
                "200px");

        cell = getGridElement().getCell(0, 0);
        assertEquals(200, cell.getSize().getWidth());

        // Set second column to be 150px wide
        selectMenuPath("Component", "Columns", "Column 1", "Column 1 Width",
                "150px");
        cell = getGridElement().getCell(0, 1);
        assertEquals(150, cell.getSize().getWidth());

        // Set first column to be auto sized (defaults to 100px currently)
        selectMenuPath("Component", "Columns", "Column 0", "Column 0 Width",
                "Auto");

        cell = getGridElement().getCell(0, 0);
        assertEquals(100, cell.getSize().getWidth());
    }

    @Test
    public void testPrimaryStyleNames() throws Exception {
        openTestURL();

        // v-grid is default primary style namea
        assertPrimaryStylename("v-grid");

        selectMenuPath("Component", "State", "Primary style name",
                "v-escalator");
        assertPrimaryStylename("v-escalator");

        selectMenuPath("Component", "State", "Primary style name", "my-grid");
        assertPrimaryStylename("my-grid");

        selectMenuPath("Component", "State", "Primary style name", "v-grid");
        assertPrimaryStylename("v-grid");
    }

    /**
     * Test that the current view is updated when a server-side container change
     * occurs (without scrolling back and forth)
     */
    @Test
    public void testItemSetChangeEvent() throws Exception {
        openTestURL();

        final By newRow = By.xpath("//td[text()='newcell: 0']");

        assertTrue("Unexpected initial state", !isElementPresent(newRow));

        selectMenuPath("Component", "Body rows", "Add first row");
        assertTrue("Add row failed", isElementPresent(newRow));

        selectMenuPath("Component", "Body rows", "Remove first row");
        assertTrue("Remove row failed", !isElementPresent(newRow));
    }

    /**
     * Test that the current view is updated when a property's value is reflect
     * to the client, when the value is modified server-side.
     */
    @Test
    public void testPropertyValueChangeEvent() throws Exception {
        openTestURL();

        assertEquals("Unexpected cell initial state", "(0, 0)",
                getGridElement().getCell(0, 0).getText());

        selectMenuPath("Component", "Body rows",
                "Modify first row (getItemProperty)");
        assertEquals("(First) modification with getItemProperty failed",
                "modified: 0", getGridElement().getCell(0, 0).getText());

        selectMenuPath("Component", "Body rows",
                "Modify first row (getContainerProperty)");
        assertEquals("(Second) modification with getItemProperty failed",
                "modified: Column 0", getGridElement().getCell(0, 0).getText());
    }

    @Test
    public void testRemovingAllItems() throws Exception {
        openTestURL();

        selectMenuPath("Component", "Body rows", "Remove all rows");

        assertEquals(0, getGridElement().findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr")).size());
    }

    @Test
    public void testVerticalScrollBarVisibilityWhenEnoughRows()
            throws Exception {
        openTestURL();

        assertTrue(verticalScrollbarIsPresent());

        selectMenuPath("Component", "Body rows", "Remove all rows");
        assertFalse(verticalScrollbarIsPresent());

        selectMenuPath("Component", "Body rows", "Add 18 rows");
        assertFalse(verticalScrollbarIsPresent());

        selectMenuPath("Component", "Body rows", "Add first row");
        assertTrue(verticalScrollbarIsPresent());
    }

    private boolean verticalScrollbarIsPresent() {
        return "scroll".equals(getGridVerticalScrollbar().getCssValue(
                "overflow-y"));
    }

    private void assertPrimaryStylename(String stylename) {
        assertTrue(getGridElement().getAttribute("class").contains(stylename));

        String tableWrapperStyleName = getGridElement().getTableWrapper()
                .getAttribute("class");
        assertTrue(tableWrapperStyleName.contains(stylename + "-tablewrapper"));

        String hscrollStyleName = getGridElement().getHorizontalScroller()
                .getAttribute("class");
        assertTrue(hscrollStyleName.contains(stylename + "-scroller"));
        assertTrue(hscrollStyleName
                .contains(stylename + "-scroller-horizontal"));

        String vscrollStyleName = getGridElement().getVerticalScroller()
                .getAttribute("class");
        assertTrue(vscrollStyleName.contains(stylename + "-scroller"));
        assertTrue(vscrollStyleName.contains(stylename + "-scroller-vertical"));
    }
}
