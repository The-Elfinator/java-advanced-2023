package info.kgeorgiy.ja.treshchev.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class StudentDB implements StudentQuery {

    private static final Comparator<Student> NAME_COMPARATOR = Comparator
            .comparing(Student::getLastName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Student::getFirstName, String.CASE_INSENSITIVE_ORDER)
            .reversed()
            .thenComparing(Student::getId);

    private <T> List<T> getItems(List<Student> list, Function<Student, T> function) {
        return list.stream().map(function).toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> list) {
        return getItems(list, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> list) {
        return getItems(list, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> list) {
        return getItems(list, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> list) {
        return getItems(list, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> list) {
        return list.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> list) {
        return list.stream()
                .max(Comparator.naturalOrder())
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sortStudents(Collection<Student> collection, Comparator<Student> comparator) {
        return collection.stream()
                .sorted(comparator)
                .toList();
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> collection) {
        return sortStudents(collection, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> collection) {
        return sortStudents(collection, NAME_COMPARATOR);
    }


    private <T> Predicate<Student> buildFindPredicate(Function<Student, T> function, T object) {
        return student -> function.apply(student).equals(object);
    }

    private <T> Stream<Student> findStudentBy(Collection<Student> collection, Function<Student, T> function, T object) {
        return sortStudentsByName(collection).stream()
                .filter(buildFindPredicate(function, object));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> collection, String s) {
        return findStudentBy(collection, Student::getFirstName, s).toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> collection, String s) {
        return findStudentBy(collection, Student::getLastName, s).toList();
    }

    private Stream<Student> findStudentsByGroupStream(Collection<Student> collection, GroupName groupName) {
        return findStudentBy(collection, Student::getGroup, groupName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> collection, GroupName groupName) {
        return findStudentsByGroupStream(collection, groupName).toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> collection, GroupName groupName) {
        return findStudentsByGroupStream(collection, groupName)
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparable::compareTo)
                ));
    }
}
