package com.example.idrive.fragments;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.idrive.R;
import com.example.idrive.data.db.FirebaseHandler;
import com.example.idrive.data.models.Lesson;
import com.example.idrive.data.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminHomeFragment extends Fragment {
    TextView tvMonthTitle;
    CalendarView calendarView;
    ViewGroup titlesContainer;
    Map<LocalDate, Lesson> lessonByDate = new HashMap<>();
    Map<String, String> uidToNameMap = new HashMap<>();
    FirebaseHandler firebaseHandler = new FirebaseHandler();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarView = view.findViewById(R.id.calendarView);
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        titlesContainer = view.findViewById(R.id.titlesContainer);

        firebaseHandler.getLessonList((lessons, uidToFullName) -> {
            uidToNameMap = uidToFullName;
            for (Lesson lesson : lessons) {
                LocalDate date = LocalDate.parse(lesson.getDate());
                lessonByDate.put(date, lesson);
            }
            innitCalendar();
        }, e -> {});

        for (int i = 0; i < titlesContainer.getChildCount(); i++) {
            View child = titlesContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView tvChild = (TextView) child;
                DayOfWeek dayOfWeek = daysOfWeek().get(i);
                String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                tvChild.setText(title);
            }
        }
    }

    private void innitCalendar() {
        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(
                currentMonth.minusMonths(1),
                currentMonth.plusMonths(1),
                daysOfWeek().get(0)
        );
        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(new MonthDayBinder<AdminHomeFragment.DayViewContainer>() {
            @NonNull
            @Override
            public AdminHomeFragment.DayViewContainer create(@NonNull View view) {
                return new AdminHomeFragment.DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull AdminHomeFragment.DayViewContainer container, @NonNull CalendarDay day) {
                TextView textView = container.textView;
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                if (day.getPosition() == DayPosition.MonthDate) {
                    textView.setTextColor(Color.BLACK);
                    if (lessonByDate.containsKey(day.getDate())) {
                        textView.setBackgroundResource(R.drawable.lesson_date_highlight);
                    } else {
                        textView.setBackground(null);
                    }

                    textView.setOnClickListener(view -> {
                        LocalDate clickedDate = day.getDate();
                        if (lessonByDate.containsKey(day.getDate())) {
                            Lesson lesson = lessonByDate.get(day.getDate());
                            if (lesson != null) {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Options for " + clickedDate)
                                        .setItems(new String[]{"View Lesson", "Add Another Lesson", "Delete Lesson"}, (dialog, which) -> {
                                            switch (which) {
                                                case 0:
                                                    showLessonDialog(lesson);
                                                    break;
                                                case 1:
                                                    showCreateLessonDialog(clickedDate);
                                                    break;
                                                case 2:
                                                    firebaseHandler.deleteLesson(lesson.getId());
                                                    break;
                                            }
                                        })
                                        .show();
                            }
                        } else {
                            showCreateLessonDialog(clickedDate);
                        }
                    });
                } else {
                    textView.setTextColor(Color.GRAY);
                    textView.setBackground(null);
                    textView.setOnClickListener(null);
                }
            }
        });

        calendarView.setMonthScrollListener(month -> {
            YearMonth yearMonth = month.getYearMonth();
            String monthTitle = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.getYear();
            tvMonthTitle.setText(monthTitle);
            return null;
        });
    }

    private void showLessonDialog(Lesson lesson) {
        String fullName = uidToNameMap.getOrDefault(lesson.getUserId(), "No User Found");
        String message = "User: " + fullName + "\nTime: " + lesson.getTime() + "\nNotes: " + lesson.getNotes();

        new AlertDialog.Builder(requireContext())
                .setTitle("Lesson Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNegativeButton("REMOVE", (dialog, which) -> {
                    firebaseHandler.deleteLesson(lesson.getId());
                    lessonByDate.remove(LocalDate.parse(lesson.getDate()));
                    calendarView.notifyCalendarChanged();
                    Toast.makeText(getContext(), "Lesson removed.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showCreateLessonDialog(LocalDate pickedDate) {
        firebaseHandler.getAllMembers(uidToNameMap -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_lesson, null);
            TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
            EditText etNotes = dialogView.findViewById(R.id.etNotes);
            Spinner userSpinner = dialogView.findViewById(R.id.spinnerUsers);

            List<String> fullNames = new ArrayList<>(uidToNameMap.values());
            List<String> uidList = new ArrayList<>(uidToNameMap.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fullNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userSpinner.setAdapter(adapter);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Create Lesson on " + pickedDate.toString())
                    .setView(dialogView)
                    .setPositiveButton("Create", (dialog, which) -> {
                        int hour = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                        String notes = etNotes.getText().toString().trim();
                        int selectedIndex = userSpinner.getSelectedItemPosition();
                        String userId = uidList.get(selectedIndex);

                        Lesson newLesson = new Lesson(userId, pickedDate.toString(), time, notes);
                        firebaseHandler.addLesson(newLesson);
                        lessonByDate.put(pickedDate, newLesson);
                        calendarView.notifyDateChanged(pickedDate);
                        Toast.makeText(getContext(), "Lesson created", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }, e -> Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show());
    }

    public static class DayViewContainer extends ViewContainer {
        public final TextView textView;
        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
}
