package org.acme.schooltimetabling.bootstrap;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "timeTable.demoData", defaultValue = "SMALL")
    DemoData demoData;

    @Inject
    TimeslotRepository timeslotRepository;
    @Inject
    RoomRepository roomRepository;
    @Inject
    LessonRepository lessonRepository;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (demoData == DemoData.NONE) {
            return;
        }

        List<Timeslot> timeslotList = new ArrayList<>();
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 00), LocalTime.of(12, 00)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(17, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(17, 30), LocalTime.of(20, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.MONDAY, LocalTime.of(19, 00), LocalTime.of(22, 00)));

        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 00), LocalTime.of(12, 00)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(17, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(17, 30), LocalTime.of(20, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.TUESDAY, LocalTime.of(19, 00), LocalTime.of(22, 00)));

        timeslotList.add(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(9, 00), LocalTime.of(12, 00)));
        timeslotList.add(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(17, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(17, 30), LocalTime.of(20, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.WEDNESDAY, LocalTime.of(19, 00), LocalTime.of(22, 00)));

        timeslotList.add(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(9, 00), LocalTime.of(12, 00)));
        timeslotList.add(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(17, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(17, 30), LocalTime.of(20, 30)));
        timeslotList.add(new Timeslot(DayOfWeek.THURSDAY, LocalTime.of(19, 00), LocalTime.of(22, 00)));

        timeslotRepository.persist(timeslotList);

        List<Room> roomList = new ArrayList<>();
        roomList.add(new Room("Room A"));
        roomList.add(new Room("Room B"));
        roomList.add(new Room("Room C"));
        //roomList.add(new Room("Room D"));

        roomRepository.persist(roomList);

        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(new Lesson("Matematica", "Scalise", "3A"));
        lessonList.add(new Lesson("Matematica", "Scalise", "3B"));
        lessonList.add(new Lesson("Matematica", "Scalise", "3C"));
        lessonList.add(new Lesson("Matematica", "Scalise", "3D"));
        lessonList.add(new Lesson("Matematica", "Scalise", "3E"));

        lessonList.add(new Lesson("Lettere", "Menegazzi", "3A"));
        lessonList.add(new Lesson("Lettere", "Menegazzi", "3B"));
        lessonList.add(new Lesson("Lettere", "Menegazzi", "3C"));
        lessonList.add(new Lesson("Lettere", "Menegazzi", "3D"));
        lessonList.add(new Lesson("Lettere", "Menegazzi", "3E"));

        lessonList.add(new Lesson("Lettere", "Capuozzo", "3A"));
        lessonList.add(new Lesson("Lettere", "Capuozzo", "3B"));
        lessonList.add(new Lesson("Lettere", "Capuozzo", "3C"));
        lessonList.add(new Lesson("Lettere", "Capuozzo", "3D"));
        lessonList.add(new Lesson("Lettere", "Capuozzo", "3E"));
        
        lessonList.add(new Lesson("Lingue e Tecnologia", "Colli-Caruso-Marsero", "3A"));
        lessonList.add(new Lesson("Lingue e Tecnologia", "Colli-Caruso-Marsero", "3B"));
        lessonList.add(new Lesson("Lingue e Tecnologia", "Colli-Caruso-Marsero", "3C"));
        lessonList.add(new Lesson("Lingue e Tecnologia", "Colli-Caruso-Marsero", "3D"));
        lessonList.add(new Lesson("Lingue e Tecnologia", "Colli-Caruso-Marsero", "3E"));

        Lesson lesson = lessonList.get(0);
        lesson.setTimeslot(timeslotList.get(0));
        lesson.setRoom(roomList.get(0));

        lessonRepository.persist(lessonList);
    }

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

}
