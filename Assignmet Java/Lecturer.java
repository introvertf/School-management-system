import java.util.ArrayList;
import java.util.List;

public class Lecturer extends Person {
    private List<Course> assignedCourses;
    private String department;

    public Lecturer(String name, String id, String email, String password, String department) {
        super(name, id, email, password);
        this.assignedCourses = new ArrayList<>();
        this.department = department;
    }

    public void assignCourse(Course course) {
        if (!assignedCourses.contains(course)) {
            assignedCourses.add(course);
            course.setLecturer(this);
        }
    }

    public void removeCourse(Course course) {
        assignedCourses.remove(course);
        course.setLecturer(null);
    }

    public List<Course> getAssignedCourses() {
        return assignedCourses;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
} 