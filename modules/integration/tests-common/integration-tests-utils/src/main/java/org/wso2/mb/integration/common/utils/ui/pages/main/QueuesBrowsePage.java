/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mb.integration.common.utils.ui.pages.main;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.mb.integration.common.utils.ui.UIElementMapper;

/**
 * This page represents 'Queue-> Browse' page in MB management console.
 */
public class QueuesBrowsePage {

    private WebDriver driver;
    private static final Log log = LogFactory.getLog(QueuesBrowsePage.class);
    
    public QueuesBrowsePage(WebDriver driver) throws IOException {
        this.driver = driver;
        // Check that we're on the right page.
        if (!driver.findElement(By.xpath(UIElementMapper.getInstance()
                .getElement("mb.queue.list.page.header.xpath"))).getText().contains("Queue List")) {
            throw new IllegalStateException("This is not the Queue List page");
        }
    }

    /**
     * Check whether the queue with the given queue name is present in the UI
     * @param queueName queue name
     * @return true if the queue is present, false otherwise
     */
    public boolean isQueuePresent(final String queueName) {
        return getTableRowByQueueName(queueName) != null;
    }

    /**
     * Delete queue from the UI delete option
     * @param queueName queue name
     * @return true if delete successful, false otherwise
     */
    public boolean deleteQueue(final String queueName) {

        boolean isSuccessful = false;
        WebElement row = getTableRowByQueueName(queueName);
        if (row == null) {
            log.warn("unable to find the table row for queue name: " + queueName);
            return false;
        }

        List<WebElement> columnList = row.findElements(By.tagName("td"));
        WebElement deleteButton = columnList.get(5).findElement(By.tagName("a"));
        deleteButton.click();

        // handle delete confirmation popup
        String dialog = driver.getWindowHandle();
        driver.switchTo().window(dialog);

        // find ok button in popup dialog and click it
        List<WebElement> buttonList = driver.findElements(By.tagName("button"));
        for (WebElement okButton : buttonList) {
            if (okButton.getText().compareToIgnoreCase("ok") == 0) {
                okButton.click();
                isSuccessful = !isQueuePresent(queueName);  // if Queue present failure
                break;
            }
        }
        
        return isSuccessful;
    }

    /**
     * Navigates the browser to 'Queue Content' page where user can see messages it contains etc.
     *
     * @param qName name of the Queue to browse
     * @return true if the navigate operation is successful, false otherwise
     */
    public QueueContentPage browseQueue(final String qName) throws IOException {

        WebElement row = getTableRowByQueueName(qName);
        if (row == null) {
            log.warn("unable to find the table row for queue name: " + qName);
            return null;    // can't find the queue.
        }

        List<WebElement> columnList = row.findElements(By.tagName("td"));
        WebElement browseButton = columnList.get(2).findElement(By.tagName("a"));
        if (browseButton != null) {
            browseButton.click();
        }
        return new QueueContentPage(this.driver);
    }

    /**
     * Retrieve the Web Element of the given queue name from available queues table in UI
     * @param queueName queue name
     * @return Web Element row of the given queue name item in UI, null returned if not found
     */
    private WebElement getTableRowByQueueName(final String queueName) {

        // if no queues available return null
        if (driver.findElement(By.id(UIElementMapper.getInstance()
                .getElement("mb.queue.list.page.workarea.id"))).getText()
                .contains("No queues are created")) {

            return null;
        }

        WebElement queueTable = driver.findElement(By.xpath(UIElementMapper.getInstance()
                .getElement("mb.queue.list.table.body.xpath")));
        List<WebElement> rowElementList = queueTable.findElements(By.tagName("tr"));

        // go through table rows and find the queue
        for (WebElement row : rowElementList) {
            List<WebElement> columnList = row.findElements(By.tagName("td"));
            // Assumption: there are six columns. Delete buttons are in the sixth column
            if ((columnList.size() == 6) && columnList.get(0).getText().equals(queueName)) {
                return row;
            }
        }
        return null;
    }
}
