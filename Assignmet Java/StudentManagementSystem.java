import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class StudentManagementSystem extends JFrame {
    private List<Student> students;
    private List<Lecturer> lecturers;
    private List<Course> courses;
    private List<Programme> programmes;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private Person currentUser;

    public StudentManagementSystem() {
        students = new ArrayList<>();
        lecturers = new ArrayList<>();
        courses = new ArrayList<>();
        programmes = new ArrayList<>();
        
        setTitle("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create and adding panels
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createStudentPanel(), "student");
        mainPanel.add(createLecturerPanel(), "lecturer");
        mainPanel.add(createRegistrationPanel(), "registration");

        add(mainPanel);
        
        // Show login panel initially
        cardLayout.show(mainPanel, "login");
    }

  

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // User type selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("User Type:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Student", "Lecturer"});
        panel.add(typeCombo, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin(usernameField.getText(), 
            new String(passwordField.getPassword()), 
            (String) typeCombo.getSelectedItem()));
        panel.add(loginButton, gbc);

        // Register button
        gbc.gridy = 4;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "registration"));
        panel.add(registerButton, gbc);

        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (currentUser == null || !(currentUser instanceof Student)) {
            panel.add(new JLabel("Please log in as a student to view your information."), BorderLayout.CENTER);
            return panel;
        }
        
        Student currentStudent = (Student) currentUser;
        
        try {
            // Loading students enrolled in  courses from database
            List<Course> enrolledCourses = DataAccess.getStudentEnrolledCourses(currentStudent.getId());
            currentStudent.getEnrolledCourses().clear();
            currentStudent.getEnrolledCourses().addAll(enrolledCourses);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load enrolled courses: " + e.getMessage());
        }
        
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        
        tabbedPane.addTab("Overview", createStudentOverviewPanel(currentStudent));
        
        
        tabbedPane.addTab("Enroll in Courses", createCourseEnrollmentPanel());
        
        
        tabbedPane.addTab("View Grades", createGradesPanel());
        
        // Create button panel for logout and delete account
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Delete account button
        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.setForeground(Color.RED);
        deleteAccountButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete your account? This action cannot be undone.",
                "Confirm Account Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    DataAccess.deleteAccount(currentStudent.getId(), "STUDENT");
                    currentUser = null;
                    JOptionPane.showMessageDialog(this, "Account deleted successfully!");
                    cardLayout.show(mainPanel, "login");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to delete account: " + ex.getMessage());
                }
            }
        });
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "login");
        });
        
        buttonPanel.add(logoutButton);
        buttonPanel.add(deleteAccountButton);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createStudentOverviewPanel(Student student) {
        JPanel panel = new JPanel(new BorderLayout());
        
        try {
            // Load student's enrolled courses from database
            List<Course> enrolledCourses = DataAccess.getStudentEnrolledCourses(student.getId());
            student.getEnrolledCourses().clear();
            student.getEnrolledCourses().addAll(enrolledCourses);
            
            // Load scores for each course
            for (Course course : enrolledCourses) {
                double score = DataAccess.getStudentCourseScore(student.getId(), course.getCourseId());
                student.setCourseScore(course, score);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + e.getMessage());
        }
        
        // Create student info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Student: " + student.getName()), gbc);
        
        gbc.gridy = 1;
        infoPanel.add(new JLabel("ID: " + student.getId()), gbc);
        
        gbc.gridy = 2;
        infoPanel.add(new JLabel("Programme: " + 
            (student.getProgramme() != null ? student.getProgramme().getProgrammeName() : "Not Assigned")), gbc);
        
        gbc.gridy = 3;
        infoPanel.add(new JLabel("Enrolled Courses: " + student.getEnrolledCourses().size()), gbc);
        
        gbc.gridy = 4;
        infoPanel.add(new JLabel("Average Score: " + String.format("%.2f", student.calculateAverageScore())), gbc);
        
        // Create enrolled courses table
        String[] columnNames = {"Course ID", "Course Name", "Credits", "Lecturer", "Score", "Grade"};
        Object[][] data = new Object[student.getEnrolledCourses().size()][6];
        
        for (int i = 0; i < student.getEnrolledCourses().size(); i++) {
            Course course = student.getEnrolledCourses().get(i);
            double score = student.getCourseScore(course);
            String grade = calculateGrade(score);
            
            data[i][0] = course.getCourseId();
            data[i][1] = course.getCourseName();
            data[i][2] = course.getCredits();
            data[i][3] = course.getLecturer() != null ? course.getLecturer().getName() : "Not Assigned";
            data[i][4] = score;
            data[i][5] = grade;
        }
        
        JTable coursesTable = new JTable(new CourseTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Add drop course button
        JButton dropCourseButton = new JButton("Drop Course");
        dropCourseButton.addActionListener(e -> {
            int selectedRow = coursesTable.getSelectedRow();
            if (selectedRow >= 0) {
                Course selectedCourse = student.getEnrolledCourses().get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to drop " + selectedCourse.getCourseName() + "?",
                    "Confirm Drop",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Remove from database
                        DataAccess.deleteStudentEnrollment(student.getId(), selectedCourse.getCourseId());
                        
                        // Update local data
                        student.getEnrolledCourses().remove(selectedCourse);
                        selectedCourse.removeStudent(student);
                        
                        // Refresh the UI
                        mainPanel.removeAll();
                        mainPanel.add(createLoginPanel(), "login");
                        mainPanel.add(createStudentPanel(), "student");
                        mainPanel.add(createLecturerPanel(), "lecturer");
                        mainPanel.add(createRegistrationPanel(), "registration");
                        cardLayout.show(mainPanel, "student");
                        
                        JOptionPane.showMessageDialog(this, "Course dropped successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to drop course: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course to drop");
            }
        });
        
        buttonPanel.add(dropCourseButton);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createLecturerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (currentUser == null || !(currentUser instanceof Lecturer)) {
            panel.add(new JLabel("Please log in as a lecturer to manage courses."), BorderLayout.CENTER);
            return panel;
        }
        
        Lecturer currentLecturer = (Lecturer) currentUser;
        
        try {
            // Load lecturer's courses from database
            List<Course> lecturerCourses = DataAccess.getLecturerCourses(currentLecturer.getId());
            currentLecturer.getAssignedCourses().clear();
            currentLecturer.getAssignedCourses().addAll(lecturerCourses);
            
            // Load enrolled students for each course
            for (Course course : currentLecturer.getAssignedCourses()) {
                List<Student> enrolledStudents = DataAccess.getEnrolledStudents(course.getCourseId());
                course.getEnrolledStudents().clear();
                course.getEnrolledStudents().addAll(enrolledStudents);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + e.getMessage());
        }
        
        // Create tabs for different lecturer functions
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Programme overview tab
        tabbedPane.addTab("Programme Overview", createProgrammeOverviewPanel(currentLecturer));
        
        // Course management tab
        tabbedPane.addTab("Manage Courses", createCourseManagementPanel());
        
        // Grade management tab
        tabbedPane.addTab("Manage Grades", createGradeManagementPanel());
        
        // Create button panel for logout and delete account
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Delete account button
        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.setForeground(Color.RED);
        deleteAccountButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete your account? This action cannot be undone.",
                "Confirm Account Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    DataAccess.deleteAccount(currentLecturer.getId(), "LECTURER");
                    currentUser = null;
                    JOptionPane.showMessageDialog(this, "Account deleted successfully!");
                    cardLayout.show(mainPanel, "login");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to delete account: " + ex.getMessage());
                }
            }
        });
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "login");
        });
        
        buttonPanel.add(logoutButton);
        buttonPanel.add(deleteAccountButton);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createProgrammeOverviewPanel(Lecturer lecturer) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create a table model for programmes and their courses
        String[] columnNames = {"Programme", "Course ID", "Course Name", "Credits", "Enrolled Students"};
        List<Object[]> dataList = new ArrayList<>();
        
        for (Programme programme : programmes) {
            for (Course course : programme.getCourses()) {
                // Only show courses assigned to this lecturer
                if (course.getLecturer() == lecturer) {
                    Object[] row = new Object[5];
                    row[0] = programme.getProgrammeName();
                    row[1] = course.getCourseId();
                    row[2] = course.getCourseName();
                    row[3] = course.getCredits();
                    row[4] = course.getEnrolledStudents().size();
                    dataList.add(row);
                }
            }
        }
        
        Object[][] data = dataList.toArray(new Object[0][]);
        JTable overviewTable = new JTable(new CourseTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(overviewTable);
        
        // Add lecturer info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Lecturer: " + lecturer.getName()), gbc);
        
        gbc.gridy = 1;
        infoPanel.add(new JLabel("Department: " + lecturer.getDepartment()), gbc);
        
        gbc.gridy = 2;
        infoPanel.add(new JLabel("Total Assigned Courses: " + lecturer.getAssignedCourses().size()), gbc);
        
        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createCourseEnrollmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (currentUser == null || !(currentUser instanceof Student)) {
            panel.add(new JLabel("Please log in as a student to enroll in courses."), BorderLayout.CENTER);
            return panel;
        }
        
        Student currentStudent = (Student) currentUser;
        
        try {
            // Load student's enrolled courses from database
            List<Course> enrolledCourses = DataAccess.getStudentEnrolledCourses(currentStudent.getId());
            currentStudent.getEnrolledCourses().clear();
            currentStudent.getEnrolledCourses().addAll(enrolledCourses);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load enrolled courses: " + e.getMessage());
        }
        
        // Create a table model for available courses
        String[] columnNames = {"Course ID", "Course Name", "Credits", "Lecturer"};
        List<Object[]> dataList = new ArrayList<>();
        
        for (Course course : courses) {
            // Only show courses that the student is not already enrolled in
            if (!currentStudent.getEnrolledCourses().contains(course)) {
                Object[] row = new Object[4];
                row[0] = course.getCourseId();
                row[1] = course.getCourseName();
                row[2] = course.getCredits();
                row[3] = course.getLecturer() != null ? course.getLecturer().getName() : "Not Assigned";
                dataList.add(row);
            }
        }
        
        Object[][] data = dataList.toArray(new Object[0][]);
        JTable courseTable = new JTable(new CourseTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(courseTable);
        
        // Create enrollment button
        JButton enrollButton = new JButton("Enroll in Selected Course");
        enrollButton.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow >= 0) {
                String courseId = (String) data[selectedRow][0];
                Course selectedCourse = null;
                
                // Find the selected course
                for (Course course : courses) {
                    if (course.getCourseId().equals(courseId)) {
                        selectedCourse = course;
                        break;
                    }
                }
                
                if (selectedCourse != null) {
                    try {
                        // Check if already enrolled
                        if (currentStudent.getEnrolledCourses().contains(selectedCourse)) {
                            JOptionPane.showMessageDialog(this, "You are already enrolled in this course!");
                            return;
                        }
                        
                        // Enroll in database
                        DataAccess.enrollStudentInCourse(currentStudent.getId(), selectedCourse.getCourseId());
                        
                        // Update local lists
                        currentStudent.enrollInCourse(selectedCourse);
                        
                        JOptionPane.showMessageDialog(this, "Successfully enrolled in " + selectedCourse.getCourseName());
                        
                        // Refresh the student panel
                        mainPanel.removeAll();
                        mainPanel.add(createLoginPanel(), "login");
                        mainPanel.add(createStudentPanel(), "student");
                        mainPanel.add(createLecturerPanel(), "lecturer");
                        mainPanel.add(createRegistrationPanel(), "registration");
                        cardLayout.show(mainPanel, "student");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to enroll in course: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course to enroll in");
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(enrollButton, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (currentUser == null || !(currentUser instanceof Student)) {
            panel.add(new JLabel("Please log in as a student to view grades."), BorderLayout.CENTER);
            return panel;
        }
        
        Student currentStudent = (Student) currentUser;
        
        try {
            // Load student's enrolled courses from database
            List<Course> enrolledCourses = DataAccess.getStudentEnrolledCourses(currentStudent.getId());
            currentStudent.getEnrolledCourses().clear();
            currentStudent.getEnrolledCourses().addAll(enrolledCourses);
            
            // Load scores for each course
            for (Course course : enrolledCourses) {
                double score = DataAccess.getStudentCourseScore(currentStudent.getId(), course.getCourseId());
                currentStudent.setCourseScore(course, score);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + e.getMessage());
        }
        
        // Create a table model for grades
        String[] columnNames = {"Course", "Score", "Grade"};
        Object[][] data = new Object[currentStudent.getEnrolledCourses().size()][3];
        
        for (int i = 0; i < currentStudent.getEnrolledCourses().size(); i++) {
            Course course = currentStudent.getEnrolledCourses().get(i);
            double score = currentStudent.getCourseScore(course);
            String grade = calculateGrade(score);
            
            data[i][0] = course.getCourseName();
            data[i][1] = score >= 0 ? String.format("%.2f", score) : "Not Graded";
            data[i][2] = score >= 0 ? grade : "N/A";
        }
        
        JTable gradesTable = new JTable(new GradesTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        
        // Create average score label
        double averageScore = currentStudent.calculateAverageScore();
        JLabel averageLabel = new JLabel(String.format("Average Score: %.2f", averageScore));
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Grades");
        refreshButton.addActionListener(e -> {
            try {
                // Reload all data
                List<Course> enrolledCourses = DataAccess.getStudentEnrolledCourses(currentStudent.getId());
                currentStudent.getEnrolledCourses().clear();
                currentStudent.getEnrolledCourses().addAll(enrolledCourses);
                
                for (Course course : enrolledCourses) {
                    double score = DataAccess.getStudentCourseScore(currentStudent.getId(), course.getCourseId());
                    currentStudent.setCourseScore(course, score);
                }
                
                // Refresh the UI
                mainPanel.removeAll();
                mainPanel.add(createLoginPanel(), "login");
                mainPanel.add(createStudentPanel(), "student");
                mainPanel.add(createLecturerPanel(), "lecturer");
                mainPanel.add(createRegistrationPanel(), "registration");
                cardLayout.show(mainPanel, "student");
                
                JOptionPane.showMessageDialog(this, "Grades refreshed successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to refresh grades: " + ex.getMessage());
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(averageLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private String calculateGrade(double score) {
        if (score >= 70) return "A";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        if (score >= 40) return "D";
        return "F";
    }

    // Custom table model for grades
    private class GradesTableModel extends javax.swing.table.DefaultTableModel {
        public GradesTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    // Custom table model for courses
    private class CourseTableModel extends javax.swing.table.DefaultTableModel {
        public CourseTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private JPanel createCourseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (currentUser == null || !(currentUser instanceof Lecturer)) {
            panel.add(new JLabel("Please log in as a lecturer to manage courses."), BorderLayout.CENTER);
            return panel;
        }
        
        Lecturer currentLecturer = (Lecturer) currentUser;
        
        try {
            // Load lecturer's courses from database
            List<Course> lecturerCourses = DataAccess.getLecturerCourses(currentLecturer.getId());
            currentLecturer.getAssignedCourses().clear();
            currentLecturer.getAssignedCourses().addAll(lecturerCourses);
            
            // Load enrolled students for each course
            for (Course course : currentLecturer.getAssignedCourses()) {
                List<Student> enrolledStudents = DataAccess.getEnrolledStudents(course.getCourseId());
                course.getEnrolledStudents().clear();
                course.getEnrolledStudents().addAll(enrolledStudents);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + e.getMessage());
        }
        
        // Create a table model for assigned courses
        String[] columnNames = {"Course ID", "Course Name", "Credits", "Enrolled Students"};
        Object[][] data = new Object[currentLecturer.getAssignedCourses().size()][4];
        
        for (int i = 0; i < currentLecturer.getAssignedCourses().size(); i++) {
            Course course = currentLecturer.getAssignedCourses().get(i);
            data[i][0] = course.getCourseId();
            data[i][1] = course.getCourseName();
            data[i][2] = course.getCredits();
            data[i][3] = course.getEnrolledStudents().size();
        }
        
        JTable courseTable = new JTable(new CourseTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(courseTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        
        // View students button
        JButton viewStudentsButton = new JButton("View Students");
        viewStudentsButton.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow >= 0) {
                Course selectedCourse = currentLecturer.getAssignedCourses().get(selectedRow);
                showEnrolledStudents(selectedCourse);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course");
            }
        });
        
        // Add course button
        JButton addCourseButton = new JButton("Add New Course");
        addCourseButton.addActionListener(e -> showAddCourseDialog());
        
        // Delete course button
        JButton deleteCourseButton = new JButton("Delete Course");
        deleteCourseButton.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow >= 0) {
                Course selectedCourse = currentLecturer.getAssignedCourses().get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + selectedCourse.getCourseName() + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        DataAccess.deleteCourse(selectedCourse.getCourseId());
                        courses.remove(selectedCourse);
                        currentLecturer.removeCourse(selectedCourse);
                        
                        // Refresh the UI
                        mainPanel.removeAll();
                        mainPanel.add(createLoginPanel(), "login");
                        mainPanel.add(createStudentPanel(), "student");
                        mainPanel.add(createLecturerPanel(), "lecturer");
                        mainPanel.add(createRegistrationPanel(), "registration");
                        cardLayout.show(mainPanel, "lecturer");
                        
                        JOptionPane.showMessageDialog(this, "Course deleted successfully!");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to delete course: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a course to delete");
            }
        });
        
        buttonPanel.add(viewStudentsButton);
        buttonPanel.add(addCourseButton);
        buttonPanel.add(deleteCourseButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void showEnrolledStudents(Course course) {
        JDialog dialog = new JDialog(this, "Enrolled Students - " + course.getCourseName(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create table model with student information only
        String[] columnNames = {"Student ID", "Name", "Email"};
        Object[][] data = new Object[course.getEnrolledStudents().size()][3];
        
        for (int i = 0; i < course.getEnrolledStudents().size(); i++) {
            Student student = course.getEnrolledStudents().get(i);
            
            data[i][0] = student.getId();
            data[i][1] = student.getName();
            data[i][2] = student.getEmail();
        }
        
        JTable studentTable = new JTable(new StudentTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(studentTable);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showUpdateGradeDialog(Course selectedCourse, Student selectedStudent) {
        JDialog dialog = new JDialog(this, "Update Grade", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField scoreField = new JTextField();
        panel.add(new JLabel("Score (0-100):"));
        panel.add(scoreField);
        
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        
        updateButton.addActionListener(e -> {
            try {
                double score = Double.parseDouble(scoreField.getText().trim());
                if (score < 0 || score > 100) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Score must be between 0 and 100", 
                        "Invalid Score", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Update grade in database
                DataAccess.updateStudentGrade(selectedStudent.getId(), selectedCourse.getCourseId(), score);
                
                // Update local data
                selectedStudent.setCourseScore(selectedCourse, score);
                
                // Update the lecturer's local data
                if (currentUser instanceof Lecturer) {
                    Lecturer lecturer = (Lecturer) currentUser;
                    for (Course course : lecturer.getAssignedCourses()) {
                        if (course.getCourseId().equals(selectedCourse.getCourseId())) {
                            for (Student student : course.getEnrolledStudents()) {
                                if (student.getId().equals(selectedStudent.getId())) {
                                    student.setCourseScore(course, score);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(dialog, 
                    "Grade updated successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                
                // Refresh the UI
                mainPanel.removeAll();
                mainPanel.add(createLoginPanel(), "login");
                mainPanel.add(createStudentPanel(), "student");
                mainPanel.add(createLecturerPanel(), "lecturer");
                mainPanel.add(createRegistrationPanel(), "registration");
                
                // Show appropriate panel based on current user
                if (currentUser instanceof Student) {
                    cardLayout.show(mainPanel, "student");
                } else if (currentUser instanceof Lecturer) {
                    cardLayout.show(mainPanel, "lecturer");
                }
                
                // Force a refresh of the grade management panel
                if (currentUser instanceof Lecturer) {
                    Lecturer lecturer = (Lecturer) currentUser;
                    try {
                        // Reload all data
                        List<Course> lecturerCourses = DataAccess.getLecturerCourses(lecturer.getId());
                        lecturer.getAssignedCourses().clear();
                        lecturer.getAssignedCourses().addAll(lecturerCourses);
                        
                        for (Course course : lecturer.getAssignedCourses()) {
                            List<Student> enrolledStudents = DataAccess.getEnrolledStudents(course.getCourseId());
                            course.getEnrolledStudents().clear();
                            course.getEnrolledStudents().addAll(enrolledStudents);
                            
                            for (Student student : enrolledStudents) {
                                double newScore = DataAccess.getStudentCourseScore(student.getId(), course.getCourseId());
                                student.setCourseScore(course, newScore);
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Failed to refresh grades: " + ex.getMessage());
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a valid number", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error updating grade: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Add New Course", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Course ID field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Course ID:"), gbc);
        
        gbc.gridx = 1;
        JTextField courseIdField = new JTextField(15);
        panel.add(courseIdField, gbc);
        
        // Course Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Course Name:"), gbc);
        
        gbc.gridx = 1;
        JTextField courseNameField = new JTextField(15);
        panel.add(courseNameField, gbc);
        
        // Credits field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Credits:"), gbc);
        
        gbc.gridx = 1;
        JTextField creditsField = new JTextField(15);
        panel.add(creditsField, gbc);
        
        // Add button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton addButton = new JButton("Add Course");
        addButton.addActionListener(e -> {
            try {
                String courseId = courseIdField.getText();
                String courseName = courseNameField.getText();
                int credits = Integer.parseInt(creditsField.getText());
                
                Course newCourse = new Course(courseId, courseName, credits);
                
                // Save to database
                DataAccess.addCourse(newCourse);
                DataAccess.assignCourseToLecturer(courseId, currentUser.getId());
                
                // Update local lists
                courses.add(newCourse);
                ((Lecturer) currentUser).assignCourse(newCourse);
                
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Course added successfully!");
                
                // Refresh the lecturer panel
                mainPanel.removeAll();
                mainPanel.add(createLoginPanel(), "login");
                mainPanel.add(createStudentPanel(), "student");
                mainPanel.add(createLecturerPanel(), "lecturer");
                mainPanel.add(createRegistrationPanel(), "registration");
                cardLayout.show(mainPanel, "lecturer");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number for credits");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to add course: " + ex.getMessage());
            }
        });
        panel.add(addButton, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // Custom table model for students
    private class StudentTableModel extends javax.swing.table.DefaultTableModel {
        public StudentTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private JPanel createGradeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (currentUser == null || !(currentUser instanceof Lecturer)) {
            panel.add(new JLabel("Please log in as a lecturer to manage grades."), BorderLayout.CENTER);
            return panel;
        }
        
        Lecturer currentLecturer = (Lecturer) currentUser;
        
        try {
            // Load lecturer's courses from database
            List<Course> lecturerCourses = DataAccess.getLecturerCourses(currentLecturer.getId());
            currentLecturer.getAssignedCourses().clear();
            currentLecturer.getAssignedCourses().addAll(lecturerCourses);
            
            // Load enrolled students for each course
            for (Course course : currentLecturer.getAssignedCourses()) {
                List<Student> enrolledStudents = DataAccess.getEnrolledStudents(course.getCourseId());
                course.getEnrolledStudents().clear();
                course.getEnrolledStudents().addAll(enrolledStudents);
                
                // Load scores for each student
                for (Student student : enrolledStudents) {
                    double score = DataAccess.getStudentCourseScore(student.getId(), course.getCourseId());
                    student.setCourseScore(course, score);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + e.getMessage());
        }
        
        // Create a table model for courses with students
        String[] columnNames = {"Course", "Student", "Score", "Grade"};
        List<Object[]> dataList = new ArrayList<>();
        
        for (Course course : currentLecturer.getAssignedCourses()) {
            for (Student student : course.getEnrolledStudents()) {
                Object[] row = new Object[4];
                row[0] = course.getCourseName();
                row[1] = student.getName();
                double score = student.getCourseScore(course);
                row[2] = score >= 0 ? String.format("%.2f", score) : "Not Graded";
                row[3] = score >= 0 ? calculateGrade(score) : "N/A";
                dataList.add(row);
            }
        }
        
        Object[][] data = dataList.toArray(new Object[0][]);
        JTable gradesTable = new JTable(new GradesTableModel(data, columnNames));
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        
        // Update grade button
        JButton updateGradeButton = new JButton("Update Grade");
        updateGradeButton.addActionListener(e -> {
            int selectedRow = gradesTable.getSelectedRow();
            if (selectedRow >= 0) {
                String courseName = (String) data[selectedRow][0];
                String studentName = (String) data[selectedRow][1];
                
                Course selectedCourse = null;
                Student selectedStudent = null;
                
                // Find the selected course and student
                for (Course course : currentLecturer.getAssignedCourses()) {
                    if (course.getCourseName().equals(courseName)) {
                        selectedCourse = course;
                        for (Student student : course.getEnrolledStudents()) {
                            if (student.getName().equals(studentName)) {
                                selectedStudent = student;
                                break;
                            }
                        }
                        break;
                    }
                }
                
                if (selectedCourse != null && selectedStudent != null) {
                    showUpdateGradeDialog(selectedCourse, selectedStudent);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a student to update grade");
            }
        });
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Grades");
        refreshButton.addActionListener(e -> {
            try {
                // Reload all data
                List<Course> lecturerCourses = DataAccess.getLecturerCourses(currentLecturer.getId());
                currentLecturer.getAssignedCourses().clear();
                currentLecturer.getAssignedCourses().addAll(lecturerCourses);
                
                for (Course course : currentLecturer.getAssignedCourses()) {
                    List<Student> enrolledStudents = DataAccess.getEnrolledStudents(course.getCourseId());
                    course.getEnrolledStudents().clear();
                    course.getEnrolledStudents().addAll(enrolledStudents);
                    
                    for (Student student : enrolledStudents) {
                        double score = DataAccess.getStudentCourseScore(student.getId(), course.getCourseId());
                        student.setCourseScore(course, score);
                    }
                }
                
                // Recreate the grade management panel
                JPanel newPanel = createGradeManagementPanel();
                
                // Replace the current panel with the new one
                panel.removeAll();
                panel.add(newPanel, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
                
                JOptionPane.showMessageDialog(this, "Grades refreshed successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to refresh grades: " + ex.getMessage());
            }
        });
        
        buttonPanel.add(updateGradeButton);
        buttonPanel.add(refreshButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void handleLogin(String username, String password, String userType) {
        try {
            System.out.println("Attempting login with:");
            System.out.println("Username: " + username);
            System.out.println("UserType: " + userType);
            
            Person user = DataAccess.login(username, password, userType);
            if (user != null) {
                System.out.println("Login successful for user: " + user.getName());
                currentUser = user;
                // Remove existing panels
                mainPanel.removeAll();
                // Add new panels
                mainPanel.add(createLoginPanel(), "login");
                mainPanel.add(createStudentPanel(), "student");
                mainPanel.add(createLecturerPanel(), "lecturer");
                mainPanel.add(createRegistrationPanel(), "registration");
                // Show appropriate panel based on user type
                if (user instanceof Student) {
                    cardLayout.show(mainPanel, "student");
                } else if (user instanceof Lecturer) {
                    cardLayout.show(mainPanel, "lecturer");
                }
            } else {
                System.out.println("Login failed: Invalid credentials");
                JOptionPane.showMessageDialog(this, 
                    "Invalid credentials. Please check your username, password, and user type.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // User type selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("User Type:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Student", "Lecturer"});
        panel.add(typeCombo, gbc);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        panel.add(nameField, gbc);

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Email field
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        panel.add(emailField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Department field (for lecturers)
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel deptLabel = new JLabel("Department:");
        panel.add(deptLabel, gbc);

        gbc.gridx = 1;
        JTextField deptField = new JTextField(20);
        panel.add(deptField, gbc);

        // Show/hide department field based on user type
        typeCombo.addActionListener(e -> {
            boolean isLecturer = typeCombo.getSelectedItem().equals("Lecturer");
            deptLabel.setVisible(isLecturer);
            deptField.setVisible(isLecturer);
        });

        // Register button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                String name = nameField.getText();
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String department = deptField.getText();

                if (type.equals("Student")) {
                    Student student = new Student(name, username, email, password);
                    DataAccess.addStudent(student);
                } else {
                    if (department.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Department is required for lecturers");
                        return;
                    }
                    Lecturer lecturer = new Lecturer(name, username, email, password, department);
                    DataAccess.addLecturer(lecturer);
                }

                JOptionPane.showMessageDialog(this, "Registration successful!");
                cardLayout.show(mainPanel, "login");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
            }
        });
        panel.add(registerButton, gbc);

        // Back to login button
        gbc.gridy = 7;
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        panel.add(backButton, gbc);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentManagementSystem().setVisible(true);
        });
    }
} 