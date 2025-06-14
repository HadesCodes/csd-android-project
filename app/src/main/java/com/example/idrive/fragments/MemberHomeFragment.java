package com.example.idrive.fragments;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.idrive.data.models.Lesson;
import com.example.idrive.data.models.Suggestion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.idrive.R;
import com.example.idrive.data.db.FirebaseHandler;
import com.example.idrive.data.models.User;

public class MemberHomeFragment extends Fragment {
    TextView tvGreeting, tvMonthTitle;
    CalendarView calendarView;
    ViewGroup titlesContainer;
    Map<LocalDate, List<Lesson>> lessonByDate = new HashMap<>();
    FirebaseHandler firebaseHandler = new FirebaseHandler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_member_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        calendarView = view.findViewById(R.id.calendarView);
        titlesContainer = view.findViewById(R.id.titlesContainer);
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);

        firebaseHandler.getCurrentUser(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            tvGreeting.setText(getString(R.string.greeting, user.getFirstName()));

            firebaseHandler.getLessonsWithUID(user.getUid(), lessonSnapshot -> {
                for (DocumentSnapshot document : lessonSnapshot.getDocuments()) {
                    Lesson lesson = document.toObject(Lesson.class);
                    if (lesson != null) lessonByDate.computeIfAbsent(
                            LocalDate.parse(lesson.getDate()),
                            k -> new ArrayList<>()).add(lesson);
                }
                innitCalendar();
            }, exception -> {
                Toast.makeText(getContext(), "No lessons found", Toast.LENGTH_SHORT).show();
            });
        }, exception -> {});

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

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
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
                        LocalDate date = day.getDate();
                        List<Lesson> lessons = lessonByDate.get(date);

                        if (lessons != null && !lessons.isEmpty()) {
                            String[] items = lessons.stream()
                                    .map(lesson -> lesson.getTime() + " - " + lesson.getNotes())
                                    .toArray(String[]::new);

                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Your Lessons on " + date)
                                    .setItems(items, (dialog, which) -> {
                                        Lesson selectedLesson = lessons.get(which);
                                        showLessonDialog(selectedLesson);
                                    })
                                    .show();
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
        String message = "Time: " + lesson.getTime() + "\nNotes: " + lesson.getNotes();

        new AlertDialog.Builder(requireContext())
                .setTitle("Lesson Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNegativeButton("Suggest Change", (dialog, which) -> {
                    firebaseHandler.checkSimilarSuggestions(lesson.getUserId(), lesson.getDate(), isUnique -> {
                        if (isUnique) {
                            showSuggestDialog(lesson);
                        } else {
                            Toast.makeText(getContext(), "You’ve already made a suggestion on this day.", Toast.LENGTH_SHORT).show();
                        }
                    }, e -> Toast.makeText(getContext(), "Error on multiple suggestions.", Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    private void showSuggestDialog(Lesson lesson) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_suggestion, null);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        new AlertDialog.Builder(requireContext())
                .setTitle("Suggest a change")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth() + 1;
                    int day = datePicker.getDayOfMonth();
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    String notes = etNotes.getText().toString().trim();
                    LocalDate newDate = LocalDate.of(year, month, day);
                    LocalTime newTime = LocalTime.of(hour, minute);

                    Suggestion suggestion = new Suggestion(
                            lesson.getUserId(),
                            lesson.getId(),
                            lesson.getDate(),
                            lesson.getTime(),
                            newDate.toString(),
                            newTime.toString(),
                            notes
                    );

                    firebaseHandler.addSuggestion(suggestion);
                    Toast.makeText(requireContext(), "Suggestion made", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class DayViewContainer extends ViewContainer {
        public final TextView textView;
        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
}
