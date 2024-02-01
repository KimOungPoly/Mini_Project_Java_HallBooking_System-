import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

public class Main {
    static int rows; //variable create array 2D
    static int columns; //variable create array 2D
    static int[][] morningHall; // Hall for morning showtime
    static int[][] afternoonHall; // Hall for afternoon showtime
    static int[][] eveningHall; // Hall for evening showtime
    static BookingRecord[] bookingHistory = new BookingRecord[100]; // Array to store booking history
    static int bookingCount = 0; // for booking records

    //  Regex Pattern
     static  Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
     static  Pattern STRING_PATTERN = Pattern.compile("[A-Za-z]-\\d+(,[A-Za-z]-\\d+)*");

    public static int[][] createRowColumn(int rows, int columns) {
        return new int[rows][columns];
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        //call setup hall
        setUpHall(scanner);

        pressEnterToContinue(scanner);

        char choice;
        do {
            System.out.println("====== SYSTEM MENU ======");
            System.out.println("<A> Booking ");
            System.out.println("<B> Hall  ");
            System.out.println("<C> Showtime  ");
            System.out.println("<D> Reboot Hall  ");
            System.out.println("<E> Booking History  ");
            System.out.println("<F> Exit");
            System.out.println("============================");
            System.out.print("Please select MENU No (A-F): ");
            choice = scanner.next().toUpperCase().charAt(0);

            switch (choice) {
                case 'A' -> booking(scanner);
                case 'B' -> showHall();
                case 'C' -> {
                    System.out.println("Daily Showtime of Poly Hall: ");
                    showtime();
                }
                case 'D' -> rebootShowtime(scanner);
                case 'E' -> displayBookingHistory();
                case 'F' -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice. Please try again.");
            }

        } while (choice != 'F');

        scanner.close();
    }

    // method use for setup hall
    public static void setUpHall(Scanner scanner){
        System.out.println("---------------------------------------------------------");
        System.out.println("======== WELCOME TO POLY HALL BOOKING SYSTEMS ===========");
        System.out.println("---------------------------------------------------------");
        // setup rows
        while (true) {
            System.out.print("---> Config total rows in hall: ");
            String input = scanner.next();
            if (NUMBER_PATTERN.matcher(input).matches()) {
                rows = Integer.parseInt(input);
                if (rows > 0) {
                    break;
                } else {
                    System.out.println("Number of rows must be greater than 0. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
        //  setup seats of row
        while (true) {
            System.out.print("---> Config total seats per row in hall: ");
            String input = scanner.next();
            if (NUMBER_PATTERN.matcher(input).matches()) {
                columns = Integer.parseInt(input);
                if (columns > 0) {
                    break;
                } else {
                    System.out.println("Number of seats per row must be greater than 0. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
        morningHall = createRowColumn(rows, columns); // Initialize hall  for morning
        afternoonHall = createRowColumn(rows, columns); // Initialize hall  for afternoon
        eveningHall = createRowColumn(rows, columns); // Initialize  hall  for evening

        System.out.println("Congratulations, you've successfully set up the hall!");
    }

    //method booking
    private static void booking(Scanner scanner) {
        System.out.println("======================================");
        System.out.println("# Start Booking Process.....");
        System.out.println("======================================");
        System.out.println("# Showtime Information :");
        showtime();
        pressEnterToContinue(scanner);
        char shift = chooseShowtime(scanner);
        System.out.println("# Hall - " + (shift == 'A' ? "Morning" : shift == 'B' ? "Afternoon" : "Evening"));
        display2DArray(shift == 'A' ? morningHall : shift == 'B' ? afternoonHall : eveningHall);
        System.out.println("# INSTRUCTION  ");
        System.out.println("# Single: C-1 ");
        System.out.println("# Multiple ( separate by comma ) : C-1,C-2 ");
        System.out.print("> Please select available seat: ");

        // Validate seat input
        String seatInput;
        while (true) {
            seatInput = scanner.next();
            if (STRING_PATTERN.matcher(seatInput).matches()) {
                if (isSeatAvailable(seatInput, shift)) {
                    break;
                } else {
                    System.out.print("Seat " + seatInput + " is already booked. Please select another seat: ");
                }
            } else {
                System.out.println("Invalid seat format.");
                System.out.print("> Please select available seat: ");
            }
        }

        System.out.print("> please enter student ID: ");
        int studentID;
        while (true) {
            String input = scanner.next();
            if (NUMBER_PATTERN.matcher(input).matches()) {
                studentID = Integer.parseInt(input);
                break;
            } else {
                System.out.println("Invalid seat format, Please Input integer");
                System.out.print("> please enter student ID : ");
            }
        }
        // check reconfirmation
        System.out.println("------------------------------------------------");
        System.out.print("> Are you sure to book ? (Y/n) : ");
        char confirmation;
        while (true) {
            String input = scanner.next().toUpperCase();
            if (input.length() == 1 && (input.charAt(0) == 'Y' || input.charAt(0) == 'N')) {
                confirmation = input.charAt(0);
                break;
            } else {
                System.out.println("Invalid input. Please enter 'Y' for yes or 'N' for no.");
                System.out.print("> Are you sure to book ? (Y/n) : ");
            }
        }
        if (confirmation == 'Y') {
            bookSeat(seatInput, shift, studentID);

            // For Record time user  booking history
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
            String createdAt = now.format(formatter);
            bookingHistory[bookingCount++] = new BookingRecord(shift, seatInput, studentID, createdAt);
        } else {
            System.out.println("Booking canceled.");
        }
    }

    private static boolean isSeatAvailable(String seatInput, char shift) {
        int[][] selectedHall;
        switch (shift) {
            case 'A':
                selectedHall = morningHall;
                break;
            case 'B':
                selectedHall = afternoonHall;
                break;
            case 'C':
                selectedHall = eveningHall;
                break;
            default:
                System.out.println("Invalid showtime.");
                return false;
        }

        String[] seatTokens = seatInput.split(","); // Split  by comma
        for (String seatToken : seatTokens) {
            String[] seatParts = seatToken.trim().split("-");
            if (seatParts.length != 2) {
                System.out.println("Invalid seat format: " + seatToken);
                return false;
            }
            char rowChar = seatParts[0].toUpperCase().charAt(0);
            int column = Integer.parseInt(seatParts[1]);
            // Validate seat
            if (!isValidSeat(rowChar, column)) {
                System.out.println("Seat " + seatToken + " is not valid.");
                return false;
            }
            // Check if the seat is already booked
            int rowIndex = rowChar - 'A'; // Convert row character to index
            int columnIndex = column - 1; // Convert column number to index
            if (selectedHall[rowIndex][columnIndex] != 0) {
                return false;
            }
        }
        return true;
    }

    private static void bookSeat(String seatInput, char shift, int studentID) {
        int[][] selectedHall;
        switch (shift) {
            case 'A':
                selectedHall = morningHall;
                break;
            case 'B':
                selectedHall = afternoonHall;
                break;
            case 'C':
                selectedHall = eveningHall;
                break;
            default:
                System.out.println("Invalid showtime.");
                return;
        }

        String[] seatTokens = seatInput.split(","); // Split  by comma
        for (String seatToken : seatTokens) {
            String[] seatParts = seatToken.trim().split("-");
            if (seatParts.length != 2) {
                System.out.println("Invalid seat format: " + seatToken);
                continue;
            }
            char rowChar = seatParts[0].toUpperCase().charAt(0);
            int column = Integer.parseInt(seatParts[1]);
            // Validate seat
            if (!isValidSeat(rowChar, column)) {
                System.out.println("Seat " + seatToken + " is not valid.");
                continue;
            }
            // Process booking
            int rowIndex = rowChar - 'A'; // Convert row character to index
            int columnIndex = column - 1; // Convert column number to index
            if (selectedHall[rowIndex][columnIndex] == 0) {
                selectedHall[rowIndex][columnIndex] = studentID;
                System.out.println("------------------------------------------------");
                System.out.println("Booking successful for seat: " + seatToken);
                System.out.println("------------------------------------------------");
            } else {
                System.out.println("------------------------------------------------");
                System.out.println("Seat " + seatToken + " is already booked.");
                System.out.println("------------------------------------------------");
            }
        }
    }

    private static boolean isValidSeat(char rowChar, int column) {
        int rowIndex = rowChar - 'A'; // Convert row character to index
        int columnIndex = column - 1; // Convert column number to index
        return rowIndex >= 0 && rowIndex < rows && columnIndex >= 0 && columnIndex < columns;
    }

    // Wait for user to press Enter
    private static void pressEnterToContinue(Scanner scanner) {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
        scanner.nextLine();
    }

    private static void showtime() {
        System.out.println("# A) Morning (10:00AM - 12:30PM) ");
        System.out.println("# B) Afternoon (1:00PM - 3:30PM) ");
        System.out.println("# C) Evening (4:00PM - 6:30PM) ");
    }
    private static char chooseShowtime(Scanner scanner) {
        char shift;
        do {
            System.out.println("======================================");
            System.out.print("Please Select Showtime (A | B | C ) : ");
            shift = scanner.next().toUpperCase().charAt(0);
            if (shift != 'A' && shift != 'B' && shift != 'C') {
                System.out.println("Invalid choice. Please try again.");
            }
        } while (shift != 'A' && shift != 'B' && shift != 'C');
        return shift;
    }
    private static void showHall() {
        System.out.println("# Hall Information ");
        System.out.println("------------------------------------------------");
        System.out.println("# Hall - Morning");
        display2DArray(morningHall);
        System.out.println("------------------------------------------------");
        System.out.println("# Hall - Afternoon");
        display2DArray(afternoonHall);
        System.out.println("------------------------------------------------");
        System.out.println("# Hall - Evening");
        display2DArray(eveningHall);
    }

    private static void display2DArray(int[][] hall) {
        char rowChar = 'A'; // Starting character for row labels
        for (int i = 0; i < hall.length; i++) {
            int columnValue = 1; // Starting value for each column
            for (int j = 0; j < hall[i].length; j++) {
                String status = hall[i][j] == 0 ? "AV" : "BO";
                System.out.print("[" + rowChar + "-" + columnValue + "::" + status + "] ");
                columnValue++;
            }
            System.out.println();
            rowChar++;
        }
    }

    private static void rebootShowtime(Scanner scanner) {
        System.out.println("------------------------------------------------");
        System.out.println(" Rebooted hall successfully..");

        setUpHall(scanner);
        pressEnterToContinue(scanner);
    }
    private static void displayBookingHistory() {
        System.out.println("# Booking History:");
        for (int i = 0; i < bookingCount; i++) {
            System.out.println("------------------------------------------------");
            BookingRecord record = bookingHistory[i];
            String seats = "[" + record.seats + "]";
            String hall = "Hall " + record.showtime;
            String studentID = String.valueOf(record.studentID);
            System.out.printf("# NO : %d%n", i + 1);
            System.out.printf("# Seats : %s%n", seats);
            System.out.printf("# Hall       #STU.ID      #CREATED AT %n   %-10s  %-10s %-20s %n", hall, studentID, record.createdAt);
        }
        System.out.println("------------------------------------------------");
    }
    private static class BookingRecord {
        char showtime;
        String seats;
        int studentID;
        String createdAt;
        BookingRecord(char showtime, String seats, int studentID, String createdAt) {
            this.showtime = showtime;
            this.seats = seats;
            this.studentID = studentID;
            this.createdAt = createdAt;
        }
    }
}
