package org.extract;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.extract.Data.TableRows;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This program cycles through all available employees present on Paycom and scrapes the webpage
 * to transcribe all of the data on their present checklists and stores the data for each employee in
 * .csv files at the designated download destination.
 *
 * @author Anthony Prestia
 */
public class Main {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Main.class);

    private static final Path employeesFolder = Paths.get("./Employees"); // temporary folder to store all info before exporting in final version

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        //System.setProperty("webdriveer.chrome.driver", "/usr/local/bin/chromedriver");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please list the directory you want to save the checklist and employee photos: ");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy'\n'hh:mm:ss a");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Path downloadDirPath = null;
        try {
            downloadDirPath = Paths.get(scanner.nextLine()).toFile().getCanonicalFile().toPath();
        } catch (IOException canonical) {
            logger.fatal("Unable to get canonical path", canonical);
            System.exit(1);
        }
        if (!downloadDirPath.toFile().exists()) {
            try {
                Files.createDirectories(downloadDirPath);
            } catch (IOException download) {
                logger.fatal("Unable to create download directory", download);
                System.exit(1);
            }
        }
        try {
            Files.createDirectories(employeesFolder);
        } catch (IOException e) {
            logger.fatal("Unable to create download directory", e);
            System.exit(1);
        }

        String download_dir = downloadDirPath.toString();
        ChromeOptions chromeOptions = new ChromeOptions();
        JSONObject settings = new JSONObject(
                "{\n" +
                        "   \"recentDestinations\": [\n" +
                        "       {\n" +
                        "           \"id\": \"Save as PDF\",\n" +
                        "           \"origin\": \"local\",\n" +
                        "           \"account\": \"\",\n" +
                        "       }\n" +
                        "   ],\n" +
                        "   \"selectedDestinationId\": \"Save as PDF\",\n" +
                        "   \"version\": 2\n" +
                        "}");
        JSONObject prefs = new JSONObject(
                "{\n" +
                        "   \"plugins.plugins_list\":\n" +
                        "       [\n" +
                        "           {\n" +
                        "               \"enabled\": False,\n" +
                        "               \"name\": \"Chrome PDF Viewer\"\n" +
                        "          }\n" +
                        "       ],\n" +
                        "   \"download.extensions_to_open\": \"applications/pdf\"\n" +
                        "}")
                .put("printing.print_preview_sticky_settings.appState", settings)
                .put("download.default_directory", download_dir);
        chromeOptions.setExperimentalOption("prefs", prefs);
        String url = "https://www.paycomonline.net/v4/cl/cl-login.php";
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get(url);
        driver.manage().window().maximize();
        System.out.println("Please enter your client code: ");
        String clientCode = scanner.nextLine();
        System.out.println("Please enter your username: ");
        String userName = scanner.nextLine();
        System.out.println("Please enter your password: ");
        String pwd = scanner.nextLine();
        driver.findElement(By.id("clientcode")).sendKeys(clientCode);
        driver.findElement(By.id("txtlogin")).sendKeys(userName);
        driver.findElement(By.id("password")).sendKeys(pwd);
        driver.findElement(By.id("btnSubmit")).click();
        waitForLoad(driver);
        System.out.println(driver.findElement(By.xpath("//*[@id=\"firstSecurityQuestion-row\"]/label")).getText());
        String firstQ = scanner.nextLine();
        driver.findElement(By.xpath("//*[@id=\"firstSecurityQuestion-row\"]/div/div/input")).sendKeys(firstQ);
        System.out.println(driver.findElement(By.xpath("//*[@id=\"secondSecurityQuestion-row\"]/label")).getText());
        String secQ = scanner.nextLine();
        driver.findElement(By.xpath("//*[@id=\"secondSecurityQuestion-row\"]/div/div/input")).sendKeys(secQ);
        driver.findElement(By.xpath("//button[@name='continue']")).click();
        waitForLoad(driver);
        try {
            driver.findElement(By.id("HumanResources"));
        } catch (NoSuchElementException e) {
            logger.fatal("Wrong answers to your questions", e);
            driver.close();
            System.exit(1);
        }
        WebElement elementToHoverOver = driver.findElement(By.id("HumanResources"));
        Actions hover = new Actions(driver).moveToElement(elementToHoverOver);
        hover.perform();
        waitUntilClickable(driver, By.id("DocumentsandChecklists"));
        waitUntilClickable(driver,
                By.xpath("/html/body/div[3]/div/div[2]/div[1]/div[2]/div/div/div[2]/ul/li[2]/a/div[1]")
        );
        List<WebElement> options = driver.findElements(By.xpath(
                "/html/body/div[4]/div/form/div/div/div[1]/div/div[2]/div[1]/div[3]/div/div[2]/div/label/select/option"));
        waitForLoad(driver);
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase("500")) {
                waitUntilClickable(driver, option);
                break;
            }
        }
        waitUntilClickable(driver,
                By.xpath(
                        "/html/body/div[4]/div/form/div/div/div[1]/div/div[2]/div[3]/table/tbody/tr[1]/td[2]/a")
        );
        waitUntilClickable(driver, By.xpath("/html/body/div[3]/div/div[2]/div[1]/div/div[2]/div[3]/a"));

        options = driver.findElements(By.xpath(
                "/html/body/div[4]/div/form/div/div[3]/div[1]/div[3]/div/div[2]/div/label/select/option"));
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase("500")) {
                waitUntilClickable(driver, option);
                break;
            }
        }
        waitForLoad(driver);
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "/html/body/div[4]/div/form/div/div[3]/div[3]/table/tbody")));

        // Figure out how many employees are listed
        String rowCountString = driver.findElement(By.xpath("//span[@id='row-count']")).getText();
        int numOfEmployees = Integer.parseInt(rowCountString.split("\\(|\\)")[1]);

        waitUntilClickable(driver, By.xpath("//*[@id=\"make-employee-changes-table\"]/tbody/tr[1]/td[2]/a"));

        waitUntilClickable(driver, By.xpath("/html/body/div[3]/div/div[4]/div/div[2]/div[5]/div[2]/a"));
        // We are on the checklist page
        /*
         * https://www.w3schools.com/xml/xpath_syntax.asp
         *
         * Table id = "Checklist_Table"
         *      //table[@id='Checklist_Table']/tbody/tr
         * Data-row-id = "row_x" where x >= 0
         *      if data row is empty it has class="noData"
         *
         * First we gotta figure out how many rows are in the checklist page.
         * Secondly we loop through for each checklist
         *      Hyperlink for actual checklist is ".../tr[x]/td[4]/a"
         * Thirdly we get the info from each checklist and store it somewhere (need more info from Denise)
         * Lastly when there are no additional rows to look at we click the next button
         *      Hyperlink with class="cdNextLink"
         */

        for (int empCount = 0; empCount < numOfEmployees; empCount++) {
            List<WebElement> paycomChecklist = driver.findElements(By.xpath("//table[@id='Checklist_Table']/tbody/tr"));
            WebElement empSelectContainer = driver.findElement(By.xpath("//div[@class='empSelect_Container']"));
            String employeeString = empSelectContainer.findElement(By.xpath("./div/div/div[1]/input")).getAttribute("value");
            String eeCode = employeeString.split("\\(|\\)")[1];

            // Create a directory for each employee code
            /*
            Path employeeFolder = employeesFolder.resolve(eeCode);
            try {
                Files.createDirectories(employeeFolder);
            } catch (IOException e) {
                logger.fatal("Unable to create download directory", e);
                System.exit(1);
            }
             */
            // Empty workbook to work in.
            XSSFWorkbook workbook = new XSSFWorkbook();
            // If there is a row with empty data we move onto the next person
            if (paycomChecklist.size() == 1) {
                if (paycomChecklist.get(0).findElements(By.xpath("./td[@class='noData']")).size() > 0) {
                    // Go to the next person
                    waitUntilClickable(driver, By.xpath("//a[@class='cdNextLink']"));
                    continue;
                }
            }
            // Loop through each checklist
            for (int i = 0; i < paycomChecklist.size(); i++) {
                WebElement row = driver.findElement(By.xpath("//table[@id='Checklist_Table']/tbody/tr[" + (i+1) + "]"));
                waitUntilClickable(driver, row.findElement(By.xpath("./td[4]/a")));
                // We clicked on the hyper-link
                List<TableRows> tableRows = new ArrayList<>();
                String tableName = driver.findElement(By.xpath("//*[@id=\"chklist\"]/div/div[9]/div/h2")).getText();
                //String tableId = tableName.split(":|\\)")[1].strip();
                String tempChecklistName = tableName.split("\\(")[0].strip();
                List<WebElement> paycomSpecificChecklist = driver.findElements(By.xpath("//div[@class='table-responsive tableContainer  ']/table/tbody/tr"));
                // Access a worksheet for each checklist
                XSSFSheet worksheet;
                worksheet = workbook.createSheet(tempChecklistName);

                for (WebElement checklistRow : paycomSpecificChecklist) {
                    if (checklistRow.getAttribute("data-row-id") == null) {
                        break;
                    }
                    boolean completed;  // 1
                    boolean startTask;  // 9
                    String eeTaskId = checklistRow.findElement(By.xpath("./td[2]")).getText();      // 2
                    String taskId = checklistRow.findElement(By.xpath("./td[3]")).getText();        // 3
                    String description = checklistRow.findElement(By.xpath("./td[4]")).getText();   // 4
                    String taskType = checklistRow.findElement(By.xpath("./td[5]")).getText();      // 5
                    String taskFor = checklistRow.findElement(By.xpath("./td[6]")).getText();       // 6
                    String completedBy = checklistRow.findElement(By.xpath("./td[7]")).getText();   // 7
                    String timeCompleted = checklistRow.findElement(By.xpath("./td[8]")).getText(); // 8

                    startTask = false;
                    completed = true;
                    if (checklistRow.findElements(By.xpath("./td[9]/a/img")).size() > 0) {
                        if (checklistRow.findElement(By.xpath("./td[9]/a/img")).getAttribute("src")
                                .equals("https://www.paycomonline.net/v4/ee/images/green_checkmark.png")) {
                            startTask = true;
                        }
                    }
                    if (completedBy.equals("")) {
                        completed = false;
                    }
                    tableRows.add(new TableRows(completed, eeTaskId, taskId, description, taskType, taskFor, completedBy, timeCompleted, startTask));

                }

                int rowCount = 0;
                XSSFRow rowElement = worksheet.createRow(rowCount);
                rowElement.createCell(0).setCellValue("Complete");
                rowElement.createCell(1).setCellValue("EE Task ID");
                rowElement.createCell(2).setCellValue("Task ID");
                rowElement.createCell(3).setCellValue("Task Description");
                rowElement.createCell(4).setCellValue("Task Type");
                rowElement.createCell(5).setCellValue("Task For");
                rowElement.createCell(6).setCellValue("Completed By");
                rowElement.createCell(7).setCellValue("Time Completed");
                rowElement.createCell(8).setCellValue("Start Task");
                rowCount++;

                for (TableRows tableRow : tableRows) {
                    rowElement = worksheet.createRow(rowCount);
                    String[] rowElements = tableRow.toString().split(",");
                    for (int cellNum = 0; cellNum < rowElements.length; cellNum++) {
                        rowElement.createCell(cellNum).setCellValue(rowElements[cellNum]);
                    }
                    rowCount++;
                }
                /*
                try {
                    FileWriter writer = new FileWriter(employeeFolder.resolve(tableId + ".csv").toFile());
                    writer.write("Complete,EE Task ID,Task ID,Task Description,Task Type,Task For,Completed By,Time Completed,Start Task\n");
                    for (TableRows checklistRow : tableRows) { //**
                        writer.write(checklistRow.toString());
                    }
                    writer.close();
                } catch (IOException e) {
                    logger.fatal("An error occurred creating the FileWriter");
                }
                 */

                // Click the cancel button
                waitUntilClickable(driver, By.xpath("//a[@id='butcancel']"));
            }
            // We are finished collecting the data from tables for an employee


            try {
                FileOutputStream outputStream = new FileOutputStream(employeesFolder.resolve(eeCode + ".xlsx").toString());
                workbook.write(outputStream);
            } catch (IOException e) {
                logger.fatal("An error occurred while writing excel files.");
            }

            // Go to the next person
            waitUntilClickable(driver, By.xpath("//a[@class='cdNextLink']"));
        }

        // End of main function
    }





    public static void populateSheet(XSSFSheet worksheet, List<TableRows> tableRows) {
        int rowCount = 0;
        XSSFRow row = worksheet.createRow(rowCount);
        row.createCell(0).setCellValue("Complete");
        row.createCell(1).setCellValue("EE Task ID");
        row.createCell(2).setCellValue("Task ID");
        row.createCell(3).setCellValue("Task Description");
        row.createCell(4).setCellValue("Task Type");
        row.createCell(5).setCellValue("Task For");
        row.createCell(6).setCellValue("Completed By");
        row.createCell(7).setCellValue("Time Completed");
        row.createCell(8).setCellValue("Start Task");
        rowCount++;

        for (TableRows tableRow : tableRows) {
            row = worksheet.createRow(rowCount);
            String[] rowElements = tableRow.toString().split(",");
            for (int i = 0; i < rowElements.length; i++) {
                row.createCell(i).setCellValue(rowElements[i]);
            }
            rowCount++;
        }
    }


    public static void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = driver1 -> ((JavascriptExecutor) driver1).executeScript(
                "return document.readyState").equals("complete");
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

    public static void waitUntilClickable(WebDriver driver, By by) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        driverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
        waitForLoad(driver);
    }

    public static void waitUntilClickable(WebDriver driver, WebElement element) {
        WebDriverWait driverWait = new WebDriverWait(driver, 30);
        driverWait.until(ExpectedConditions.elementToBeClickable(element)).click();
        waitForLoad(driver);
    }

    //end of class
}
