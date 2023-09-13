package org.acme.schooltimetabling.solver;

import java.time.Duration;

import org.acme.schooltimetabling.domain.Lesson;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.TimeTable;
import java.util.stream.Collectors;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;



public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                studentGroupSpecificTimeSlots(constraintFactory),
                teacherOverlappingTimeslots(constraintFactory),
                teacherMaxHours(constraintFactory),
                // Soft constraints
                // teacherRoomStability(constraintFactory),
                // teacherTimeEfficiency(constraintFactory),
                // studentGroupSubjectVariety(constraintFactory),
                studentGroupRoomStability(constraintFactory)
        };
    }

    Constraint roomConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getRoom))
                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        // A student can attend at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    Constraint studentGroupRoomStability(ConstraintFactory constraintFactory) {
        // A student group should always stay in the same room.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getStudentGroup))
                .filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Student room stability");
    }   
    
    
    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach in a single room.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher))
                .filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher room stability");
    }


    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class, Joiners.equal(Lesson::getTeacher),
                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher time efficiency");
    }

    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(Lesson::getStudentGroup),
                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Student group subject variety");
    }

    // custom constraints
    
    // bound student groups to specific timeslots
    Constraint studentGroupSpecificTimeSlots(ConstraintFactory constraintFactory) {
    return constraintFactory.forEach(Lesson.class)
        .filter(lesson -> !isStudentGroupInAllowedTimeSlot(lesson))
        .penalize("Student group specific time slots", HardSoftScore.ONE_HARD);
        }
        
        boolean isStudentGroupInAllowedTimeSlot(Lesson lesson) {
                // Get the allowed time slots for the student group
                Set<String> allowedTimeSlots = getAllowedTimeSlotsForStudentGroup(lesson.getStudentGroup()).stream().map(Timeslot::toString).collect(Collectors.toSet());

                return allowedTimeSlots.contains(lesson.getTimeslot().toString());
        }

        Set<Timeslot> getAllowedTimeSlotsForStudentGroup(String studentGroup) {
                Set<Timeslot> allowedTimeSlots = new HashSet<>();
                int startHour = 0;
                int startMinute = 0;
                int endHour = 0;
                int endMinute = 0;

                if (studentGroup.equals("3A")) {
                        startHour = 9;
                        startMinute = 0;
                        endHour = 12;
                        endMinute = 0;
                } else if (studentGroup.equals("3B"))
                {
                       startHour = 14;
                        startMinute = 30;
                        endHour = 17;
                        endMinute = 30; 
                } else if (studentGroup.equals("3C"))
                {
                       startHour = 14;
                        startMinute = 30;
                        endHour = 17;
                        endMinute = 30; 
                } else if (studentGroup.equals("3D"))
                {
                       startHour = 17;
                        startMinute = 30;
                        endHour = 20;
                        endMinute = 30; 
                } else if (studentGroup.equals("3E"))
                {
                       startHour = 19;
                        startMinute = 0;
                        endHour = 22;
                        endMinute = 0; 
                }

                allowedTimeSlots.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)));
                allowedTimeSlots.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)));
                allowedTimeSlots.add(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)));
                allowedTimeSlots.add(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(startHour, startMinute), LocalTime.of(endHour, endMinute)));

                return allowedTimeSlots;
        }

        Constraint teacherOverlappingTimeslots(ConstraintFactory constraintFactory) {
                // A teacher cannot have lessons in overlapping timeslots 
                return constraintFactory
                        .forEach(Lesson.class)
                        .join(Lesson.class, Joiners.equal(Lesson::getTeacher),
                                Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
                        .filter((lesson1, lesson2) -> {
                                
                                if (lesson1 == lesson2) {
                                        return false;
                                }

                                Duration between = Duration.between(lesson1.getTimeslot().getStartTime(),
                                        lesson2.getTimeslot().getStartTime());

                                // swap lesson1 with lesson2 if necessary
                                if (between.isNegative()) {
                                        Lesson lesson1_copy = lesson1;
                                        lesson1 = lesson2;
                                        lesson2 = lesson1_copy;
                                        lesson1_copy = null;
                                }
                                Duration timeBreak = Duration.between(lesson1.getTimeslot().getEndTime(),
                                        lesson2.getTimeslot().getStartTime());
                                return timeBreak.isNegative();
                        })
                        .penalize(HardSoftScore.ONE_HARD)
                        .asConstraint("Teacher overlapping timeslots");
                }

                Constraint teacherMaxHours(ConstraintFactory constraintFactory) {
                // A teacher cannot have lessons in overlapping timeslots 
                return constraintFactory
                        .forEach(Lesson.class)
                        .groupBy(
                                Lesson::getTeacher,
                                lesson -> lesson.getTimeslot().getDayOfWeek(),
                                ConstraintCollectors.sumDuration(lesson -> lesson.getTimeslot().getDuration())
                        )
               .filter((teacher, dayOfWeek, totalDuration) -> totalDuration.toHours() > 6 ) 
                        .penalize(HardSoftScore.ONE_HARD)
                        .asConstraint("Teacher max hours");
                }
        

}
