import java.util.ArrayList;
import java.util.List;

public class Programme {
    private String programmeId;
    private String programmeName;
    private List<Course> courses;
    private List<Student> students;

    public Programme(String programmeId, String programmeName) {
        this.programmeId = programmeId;
        this.programmeName = programmeName;
        this.courses = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    public void addCourse(Course course) {
        if (!courses.contains(course)) {
            courses.add(course);
            course.addProgramme(this);
        }
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.removeProgramme(this);
    }

    public void addStudent(Student student) {
        if (!students.contains(student)) {
            students.add(student);
            student.setProgramme(this);
        }
    }

    public void removeStudent(Student student) {
        students.remove(student);
        student.setProgramme(null);
    }

    // Getters and Setters
    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<Student> getStudents() {
        return students;
    }

    @Override
    public String toString() {
        return programmeId + " - " + programmeName;
    }
} 