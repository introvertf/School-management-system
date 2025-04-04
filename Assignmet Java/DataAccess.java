import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {
    // Person operations
    public static Person login(String username, String password, String type) throws SQLException {
        String dbType = type.toUpperCase();
        String sql = "SELECT * FROM persons WHERE id = ? AND password = ? AND UPPER(type) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, dbType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (dbType.equals("STUDENT")) {
                        return new Student(
                            rs.getString("name"),
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("password")
                        );
                    } else {
                        return new Lecturer(
                            rs.getString("name"),
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("department")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return null;
    }

    public static void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO persons (id, name, email, password, type) VALUES (?, ?, ?, ?, 'STUDENT')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getId());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getPassword());
            
            stmt.executeUpdate();
        }
    }

    public static void addLecturer(Lecturer lecturer) throws SQLException {
        String sql = "INSERT INTO persons (id, name, email, password, type, department) VALUES (?, ?, ?, ?, 'LECTURER', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, lecturer.getId());
            stmt.setString(2, lecturer.getName());
            stmt.setString(3, lecturer.getEmail());
            stmt.setString(4, lecturer.getPassword());
            stmt.setString(5, lecturer.getDepartment());
            
            stmt.executeUpdate();
        }
    }

    // Course operations
    public static void addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_id, course_name, credits) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, course.getCourseId());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            
            stmt.executeUpdate();
        }
    }

    public static void assignCourseToLecturer(String courseId, String lecturerId) throws SQLException {
        String sql = "UPDATE courses SET lecturer_id = ? WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, lecturerId);
            stmt.setString(2, courseId);
            
            stmt.executeUpdate();
        }
    }

    // Programme operations
    public static void addProgramme(Programme programme) throws SQLException {
        String sql = "INSERT INTO programmes (programme_id, programme_name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, programme.getProgrammeId());
            stmt.setString(2, programme.getProgrammeName());
            
            stmt.executeUpdate();
        }
    }

    public static void addCourseToProgramme(String courseId, String programmeId) throws SQLException {
        String sql = "INSERT INTO programme_courses (programme_id, course_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, programmeId);
            stmt.setString(2, courseId);
            
            stmt.executeUpdate();
        }
    }

    // Enrollment operations
    public static void enrollStudentInCourse(String studentId, String courseId) throws SQLException {
        String sql = "INSERT INTO student_enrollments (student_id, course_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            
            stmt.executeUpdate();
        }
    }

    public static void updateStudentGrade(String studentId, String courseId, double score) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM student_enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new SQLException("No enrollment found for student " + studentId + " in course " + courseId);
                }
            }
        }
        
        String updateSql = "UPDATE student_enrollments SET score = ? WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            
            stmt.setDouble(1, score);
            stmt.setString(2, studentId);
            stmt.setString(3, courseId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update grade for student " + studentId + " in course " + courseId);
            }
        }
    }

    public static void deleteCourse(String courseId) throws SQLException {
        String deleteEnrollmentsSql = "DELETE FROM student_enrollments WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteEnrollmentsSql)) {
            
            stmt.setString(1, courseId);
            stmt.executeUpdate();
        }

        String deleteCourseSql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteCourseSql)) {
            
            stmt.setString(1, courseId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("Course not found: " + courseId);
            }
        }
    }

    // Query operations
    public static List<Course> getLecturerCourses(String lecturerId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE lecturer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, lecturerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                    ));
                }
            }
        }
        return courses;
    }

    public static List<Student> getEnrolledStudents(String courseId) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT p.* FROM persons p " +
                    "JOIN student_enrollments se ON p.id = se.student_id " +
                    "WHERE se.course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getString("name"),
                        rs.getString("id"),
                        rs.getString("email"),
                        rs.getString("password")
                    ));
                }
            }
        }
        return students;
    }

    public static double getStudentCourseScore(String studentId, String courseId) throws SQLException {
        String sql = "SELECT score FROM student_enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double score = rs.getDouble("score");
                    return rs.wasNull() ? -1.0 : score;
                }
            }
        }
        return -1.0;
    }

    public static List<Course> getStudentEnrolledCourses(String studentId) throws SQLException {
        List<Course> enrolledCourses = new ArrayList<>();
        String sql = "SELECT c.*, p.name as lecturer_name, p.id as lecturer_id, p.email as lecturer_email, " +
                    "p.password as lecturer_password, p.department as lecturer_department " +
                    "FROM courses c " +
                    "JOIN student_enrollments se ON c.course_id = se.course_id " +
                    "LEFT JOIN persons p ON c.lecturer_id = p.id " +
                    "WHERE se.student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                    );
                    
                    String lecturerId = rs.getString("lecturer_id");
                    if (lecturerId != null && !lecturerId.isEmpty()) {
                        Lecturer lecturer = new Lecturer(
                            rs.getString("lecturer_name"),
                            lecturerId,
                            rs.getString("lecturer_email"),
                            rs.getString("lecturer_password"),
                            rs.getString("lecturer_department")
                        );
                        course.setLecturer(lecturer);
                    }
                    
                    enrolledCourses.add(course);
                }
            }
        }
        return enrolledCourses;
    }

    public static void deleteStudentEnrollment(String studentId, String courseId) throws SQLException {
        String sql = "DELETE FROM student_enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No enrollment found for student " + studentId + " in course " + courseId);
            }
        }
    }

    public static void deleteAccount(String userId, String userType) throws SQLException {
        if (userType.equals("STUDENT")) {
            String deleteEnrollmentsSql = "DELETE FROM student_enrollments WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteEnrollmentsSql)) {
                
                stmt.setString(1, userId);
                stmt.executeUpdate();
            }
        }
        
        String deletePersonSql = "DELETE FROM persons WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deletePersonSql)) {
            
            stmt.setString(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("User not found: " + userId);
            }
        }
    }
} 