import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private Lecturer lecturer;
    private List<Student> enrolledStudents;
    private List<Programme> programmes;

    public Course(String courseId, String courseName, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.enrolledStudents = new ArrayList<>();
        this.programmes = new ArrayList<>();
    }

    public void addStudent(Student student) {
        if (!enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
        }
    }

    public void removeStudent(Student student) {
        enrolledStudents.remove(student);
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public void addProgramme(Programme programme) {
        if (!programmes.contains(programme)) {
            programmes.add(programme);
        }
    }

    public void removeProgramme(Programme programme) {
        programmes.remove(programme);
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public List<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public List<Programme> getProgrammes() {
        return programmes;
    }

    @Override
    public String toString() {
        return courseId + " - " + courseName + " (" + credits + " credits)";
    }
} 