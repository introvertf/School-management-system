import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends Person {
    private List<Course> enrolledCourses;
    private Map<Course, Double> courseScores;
    private Programme programme;

    public Student(String name, String id, String email, String password) {
        super(name, id, email, password);
        this.enrolledCourses = new ArrayList<>();
        this.courseScores = new HashMap<>();
    }

    public void enrollInCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
            course.addStudent(this);
        }
    }

    public void setCourseScore(Course course, double score) {
        if (enrolledCourses.contains(course)) {
            courseScores.put(course, score);
        }
    }

    public double getCourseScore(Course course) {
        return courseScores.getOrDefault(course, 0.0);
    }

    public double calculateAverageScore() {
        if (courseScores.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Double score : courseScores.values()) {
            sum += score;
        }
        return sum / courseScores.size();
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

    public Programme getProgramme() {
        return programme;
    }

    public Map<Course, Double> getCourseScores() {
        return courseScores;
    }
} 