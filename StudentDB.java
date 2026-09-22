import java.sql.*;
import java.util.Scanner;

public class StudentDB {

    // ==========================================
    // XAMPP MYSQL DATABASE DETAILS
    // ==========================================

    static final String URL =
            "jdbc:mysql://localhost:3306/admission";

    static final String USER = "root";

    // XAMPP MySQL default password is usually empty
    static final String PASSWORD = "";

    static Connection con;
    static Scanner sc = new Scanner(System.in);


    // ==========================================
    // CONNECT TO XAMPP MYSQL
    // ==========================================

    public static void connectDatabase() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            System.out.println();
            System.out.println(
                    "======================================"
            );
            System.out.println(
                    " XAMPP MYSQL DATABASE CONNECTED!"
            );
            System.out.println(
                    " Database : admission"
            );
            System.out.println(
                    " Table    : student"
            );
            System.out.println(
                    "======================================"
            );

        }
        catch (ClassNotFoundException e) {

            System.out.println(
                    "MySQL JDBC Driver not found!"
            );

            System.out.println(e.getMessage());
        }
        catch (SQLException e) {

            System.out.println(
                    "Database Connection Failed!"
            );

            System.out.println(e.getMessage());
        }
    }


    // ==========================================
    // INSERT / WRITE
    // ==========================================

    public static void insertStudent() {

        try {

            System.out.println();
            System.out.println("----- INSERT STUDENT -----");

            System.out.print("Enter Register No: ");
            int regno = sc.nextInt();

            sc.nextLine();

            System.out.print("Enter Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Age: ");
            int age = sc.nextInt();

            sc.nextLine();

            System.out.print("Enter Course: ");
            String course = sc.nextLine();


            String sql =
                    "INSERT INTO student " +
                    "(regno, name, age, course) " +
                    "VALUES (?, ?, ?, ?)";


            PreparedStatement ps =
                    con.prepareStatement(sql);


            ps.setInt(1, regno);
            ps.setString(2, name);
            ps.setInt(3, age);
            ps.setString(4, course);


            int rows = ps.executeUpdate();


            if (rows > 0) {

                System.out.println(
                        "Student inserted successfully!"
                );

            }

            ps.close();

        }
        catch (SQLException e) {

            System.out.println(
                    "Insert Error: " + e.getMessage()
            );
        }
    }


    // ==========================================
    // SELECT / READ ALL STUDENTS
    // ==========================================

    public static void selectStudents() {

        try {

            System.out.println();
            System.out.println("----- STUDENT RECORDS -----");


            String sql =
                    "SELECT * FROM student";


            Statement st =
                    con.createStatement();


            ResultSet rs =
                    st.executeQuery(sql);


            System.out.println(
                    "------------------------------------------------"
            );

            System.out.printf(
                    "%-10s %-15s %-10s %-15s%n",
                    "REGNO",
                    "NAME",
                    "AGE",
                    "COURSE"
            );

            System.out.println(
                    "------------------------------------------------"
            );


            while (rs.next()) {

                int regno =
                        rs.getInt("regno");

                String name =
                        rs.getString("name");

                int age =
                        rs.getInt("age");

                String course =
                        rs.getString("course");


                System.out.printf(
                        "%-10d %-15s %-10d %-15s%n",
                        regno,
                        name,
                        age,
                        course
                );
            }


            System.out.println(
                    "------------------------------------------------"
            );


            rs.close();
            st.close();

        }
        catch (SQLException e) {

            System.out.println(
                    "Select Error: " + e.getMessage()
            );
        }
    }


    // ==========================================
    // SEARCH STUDENT
    // ==========================================

    public static void searchStudent() {

        try {

            System.out.println();
            System.out.println("----- SEARCH STUDENT -----");

            System.out.print(
                    "Enter Register No to search: "
            );

            int regno = sc.nextInt();


            String sql =
                    "SELECT * FROM student WHERE regno = ?";


            PreparedStatement ps =
                    con.prepareStatement(sql);


            ps.setInt(1, regno);


            ResultSet rs =
                    ps.executeQuery();


            if (rs.next()) {

                System.out.println();
                System.out.println("Student Found!");

                System.out.println(
                        "Register No : " +
                        rs.getInt("regno")
                );

                System.out.println(
                        "Name        : " +
                        rs.getString("name")
                );

                System.out.println(
                        "Age         : " +
                        rs.getInt("age")
                );

                System.out.println(
                        "Course      : " +
                        rs.getString("course")
                );

            }
            else {

                System.out.println(
                        "Student not found!"
                );
            }


            rs.close();
            ps.close();

        }
        catch (SQLException e) {

            System.out.println(
                    "Search Error: " +
                    e.getMessage()
            );
        }
    }


    // ==========================================
    // UPDATE STUDENT
    // ==========================================

    public static void updateStudent() {

        try {

            System.out.println();
            System.out.println("----- UPDATE STUDENT -----");

            System.out.print(
                    "Enter Register No to update: "
            );

            int regno = sc.nextInt();

            sc.nextLine();


            System.out.print(
                    "Enter New Name: "
            );

            String name = sc.nextLine();


            System.out.print(
                    "Enter New Age: "
            );

            int age = sc.nextInt();

            sc.nextLine();


            System.out.print(
                    "Enter New Course: "
            );

            String course = sc.nextLine();


            String sql =
                    "UPDATE student " +
                    "SET name = ?, age = ?, course = ? " +
                    "WHERE regno = ?";


            PreparedStatement ps =
                    con.prepareStatement(sql);


            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, course);
            ps.setInt(4, regno);


            int rows =
                    ps.executeUpdate();


            if (rows > 0) {

                System.out.println(
                        "Student updated successfully!"
                );

            }
            else {

                System.out.println(
                        "Register No not found!"
                );
            }


            ps.close();

        }
        catch (SQLException e) {

            System.out.println(
                    "Update Error: " +
                    e.getMessage()
            );
        }
    }


    // ==========================================
    // DELETE STUDENT
    // ==========================================

    public static void deleteStudent() {

        try {

            System.out.println();
            System.out.println("----- DELETE STUDENT -----");

            System.out.print(
                    "Enter Register No to delete: "
            );

            int regno = sc.nextInt();


            String sql =
                    "DELETE FROM student WHERE regno = ?";


            PreparedStatement ps =
                    con.prepareStatement(sql);


            ps.setInt(1, regno);


            int rows =
                    ps.executeUpdate();


            if (rows > 0) {

                System.out.println(
                        "Student deleted successfully!"
                );

            }
            else {

                System.out.println(
                        "Register No not found!"
                );
            }


            ps.close();

        }
        catch (SQLException e) {

            System.out.println(
                    "Delete Error: " +
                    e.getMessage()
            );
        }
    }


    // ==========================================
    // MAIN METHOD
    // ==========================================

    public static void main(String[] args) {

        // Connect to XAMPP MySQL
        connectDatabase();


        // Check database connection
        if (con == null) {

            System.out.println(
                    "Program stopped!"
            );

            return;
        }


        int choice;


        do {

            System.out.println();
            System.out.println(
                    "================================="
            );

            System.out.println(
                    "       STUDENT DATABASE"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "1. Write / Insert"
            );

            System.out.println(
                    "2. Select / Display"
            );

            System.out.println(
                    "3. Search"
            );

            System.out.println(
                    "4. Update"
            );

            System.out.println(
                    "5. Delete"
            );

            System.out.println(
                    "6. Exit"
            );

            System.out.println(
                    "================================="
            );


            System.out.print(
                    "Enter your choice: "
            );

            choice = sc.nextInt();


            switch (choice) {

                case 1:
                    insertStudent();
                    break;

                case 2:
                    selectStudents();
                    break;

                case 3:
                    searchStudent();
                    break;

                case 4:
                    updateStudent();
                    break;

                case 5:
                    deleteStudent();
                    break;

                case 6:

                    System.out.println(
                            "Program terminated."
                    );

                    break;

                default:

                    System.out.println(
                            "Invalid choice!"
                    );
            }


        } while (choice != 6);


        // Close database connection
        try {

            con.close();

            System.out.println(
                    "Database connection closed."
            );

        }
        catch (SQLException e) {

            System.out.println(
                    "Error closing database."
            );
        }


        sc.close();
    }
}