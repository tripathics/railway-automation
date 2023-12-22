import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebDriver;

public class App {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/home/shyam/Downloads/chromedriver-linux64/chromedriver");
        WebDriver driver = new ChromeDriver();

        // inputs
        String toCity = "DELHI";
        String fromCity = "LUCKNOW";
        String travelClass = "3A";
        LocalDate travelDate = LocalDate.of(2024, 2, 2);

        // Validations for date and travel class
        LocalDate today = LocalDate.now();
        // check if travelDate is after today and before 120 days from today
        if (travelDate.isBefore(today) || travelDate.isAfter(today.plusDays(120))) {
            System.out.println("Invalid travel date (must be after today and before 120 days from today)");
            driver.close();
            return;
        }
        String[] validTravelClasses = { "1A", "2A", "3A", "3E", "SL", "CC", "2S", "ALL" };
        if (!Arrays.asList(validTravelClasses).contains(travelClass)) {
            System.out.println("Invalid travel class");
            driver.close();
            return;
        }

        // run the automation
        searchTrains(driver, fromCity, toCity, travelDate, travelClass);
    }

    public static void searchTrains(WebDriver driver, String fromCity, String toCity, LocalDate travelDate,
            String travelClass) {
        driver.get("https://www.makemytrip.com/railways/");

        // resize the window to full screen so that all elements are visible to the driver
        driver.manage().window().maximize();

        /**
         * 1. Check if landed on the correct page
         */
        if (!driver.getTitle().equals("Book Train Ticket Online From IRCTC Authorized Partner - MakeMyTrip")) {
            System.out.println("Landed on the wrong page");
            return;
        }

        /**
         * 2. Print the URL and title of the page
         */
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());

        /**
         * 3. Fill the form
         */
        try {

            /**
             * 3.1. Fill the from city
             */
            // get elements of FROM input field with id "fromCity"
            WebElement fromInputBtn = driver.findElement(By.id("fromCity"));

            // click on fromInputBtn to reveal the input field with class
            fromInputBtn.click();
            Thread.sleep(1000); // wait for the input field to be visible
            WebElement fromInputField = driver.findElement(By.className("react-autosuggest__input"));
            fromInputField.sendKeys(fromCity);
            Thread.sleep(1000); // wait for dropdown to refresh with query

            // click on the first option that appears
            driver.findElement(By.id("react-autowhatever-1-section-0-item-0")).click();
            Thread.sleep(1000); // toCity input will be visible after selecting from the dropdown

            /**
             * 3.2. Fill the to city
             */
            WebElement toInputField = driver.findElement(By.className("react-autosuggest__input"));
            toInputField.sendKeys(toCity);
            Thread.sleep(1000); // wait for dropdown to refresh with query

            // click on the first option that appears
            driver.findElement(By.id("react-autowhatever-1-section-0-item-0")).click();
            Thread.sleep(1000); // date input will be visible after selecting from the dropdown

            /**
             * 3.3. Fill the date
             */
            // contains two months (current and next) from which we need to select the date
            List<WebElement> dateFieldMonths = driver.findElements(By.className("DayPicker-Month"));

            // get current month name
            String currentMonthCaption = dateFieldMonths.get(0).findElement(By.cssSelector(".DayPicker-Caption > div"))
                    .getText();
            int currentMonth = getMonth(currentMonthCaption.split(" ")[0]); // current month value

            // scroll through the months until we find the month of travelDate
            int numberOfMonthsToScroll = getMonthDifference(currentMonth, travelDate.getMonthValue()) - 1;
            for (int i = 0; i < numberOfMonthsToScroll; i++) {
                // next month button
                driver.findElement(By.cssSelector(".DayPicker-NavButton.DayPicker-NavButton--next")).click();
                Thread.sleep(1000);
            }

            // again get the months because the DOM has changed
            dateFieldMonths = driver.findElements(By.className("DayPicker-Month"));

            // select the date
            int dateFieldPickerIndex = (travelDate.getMonthValue() == currentMonth) ? 0 : 1;

            // date contains aria-label like aria-label="Thu Mar 21 2024"
            String targetAriaLabel = getTargetAriaLabel(travelDate);
            dateFieldMonths.get(dateFieldPickerIndex)
                    .findElement(By.cssSelector("div[aria-label='" + targetAriaLabel + "']")).click();

            /**
             * 3.4. Fill the travel class
             */
            // travel class select menu will be visible after selecting the date
            Thread.sleep(1000);
            driver.findElement(By.cssSelector("li[data-cy='" + travelClass + "']")).click();

            /**
             * 3.5. Click on search button
             */
            // click on search button
            driver.findElement(By.cssSelector("a[data-cy='submit']")).click();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Helper functions
    static int getMonth(String monthName) {
        switch (monthName) {
            case "January":
                return 1;
            case "February":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "August":
                return 8;
            case "September":
                return 9;
            case "October":
                return 10;
            case "November":
                return 11;
            case "December":
                return 12;
            default:
                break;
        }
        // return new exception that monthName is invalid
        throw new IllegalArgumentException("Invalid month name");
    }

    static int getMonthDifference(int fromMonth, int toMonth) {
        if (fromMonth < 1 || fromMonth > 12 || toMonth < 1 || toMonth > 12) {
            throw new IllegalArgumentException("Invalid month");
        }
        int diff = (toMonth - 1) - (fromMonth - 1);
        if (diff < 0) {
            diff += 12;
        }
        return diff;
    }

    static String getTargetAriaLabel(LocalDate travelDate) {
        String week = travelDate.getDayOfWeek().toString().substring(0, 3);
        week = week.substring(0, 1).toUpperCase() + week.substring(1).toLowerCase();

        String month = travelDate.getMonth().toString().substring(0, 3);
        month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        if (travelDate.getDayOfMonth() < 10) {
            return week + " " + month + " 0" + travelDate.getDayOfMonth() + " " + travelDate.getYear();
        }

        return week + " " + month + " " + travelDate.getDayOfMonth() + " " + travelDate.getYear();
    }
}