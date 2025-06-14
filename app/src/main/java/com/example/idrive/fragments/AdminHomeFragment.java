package com.example.idrive.fragments;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.idrive.data.models.Suggestion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
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

public class AdminHomeFragment extends Fragment {
    TextView tvMonthTitle, tvSuggestionCount;
    Button btnShowSuggestions;
    CalendarView calendarView;
    ViewGroup titlesContainer;
    Map<LocalDate, List<Lesson>> lessonByDate = new HashMap<>();
    Map<String, String> uidToNameMap = new HashMap<>();
    List<Suggestion> suggestions = new ArrayList<>();
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
        btnShowSuggestions = view.findViewById(R.id.btnShowSuggestions);
        tvSuggestionCount = view.findViewById(R.id.tvSuggestionCount);

        firebaseHandler.getLessonList((lessons, uidToFullName) -> {
            uidToNameMap = uidToFullName;
            for (Lesson lesson : lessons) {
                LocalDate date = LocalDate.parse(lesson.getDate());
                lessonByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(lesson);
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

        firebaseHandler.getSuggestions(querySnapshot -> {
            if (querySnapshot.isEmpty()) {
                tvSuggestionCount.setText(getString(R.string.no_suggestions));
                btnShowSuggestions.setEnabled(false);
            } else {
                String formattedCount = getString(R.string.suggestion_count, querySnapshot.size());
                tvSuggestionCount.setText(formattedCount);
            }
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Suggestion suggestion = doc.toObject(Suggestion.class);
                if (suggestion != null) {
                    suggestions.add(suggestion);
                }
            }
            btnShowSuggestions.setOnClickListener(v -> {
                pickSuggestion(suggestions);
            });
        }, e -> {
            tvSuggestionCount.setText(getString(R.string.error_loading_suggestions));
        });
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
                        List<Lesson> selectedLessons = lessonByDate.get(clickedDate);
                        if (lessonByDate.containsKey(day.getDate()) && selectedLessons != null) {
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("Options for " + clickedDate)
                                        .setItems(new String[]{"View Lessons", "Add Another Lesson", "Delete Lesson"}, (dialog, which) -> {
                                            switch (which) {
                                                case 0:
                                                    pickLessonDialog(selectedLessons);
                                                    break;
                                                case 1:
                                                    showCreateLessonDialog(clickedDate);
                                                    break;
                                                case 2:
                                                    removeLesson(selectedLessons);
                                                    break;
                                            }
                                        })
                                        .show();
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

    private void pickLessonDialog(List<Lesson> lessons) {
        String[] items = new String[lessons.size()];
        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);
            String name = uidToNameMap.getOrDefault(l.getUserId(), "No User Found");
            items[i] = name + " at " + l.getTime();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Lessons on selected day")
                .setItems(items, (dialog, which) -> showLessonDialog(lessons.get(which)))
                .show();
    }

    private void showLessonDialog(Lesson lesson) {
        firebaseHandler.getAllMembers(uidToNameMap -> {
            String fullName = uidToNameMap.getOrDefault(lesson.getUserId(), "No User Found");
            String message = "User: " + fullName + "\nTime: " + lesson.getTime() + "\nNotes: " + lesson.getNotes();

            new AlertDialog.Builder(requireContext())
                    .setTitle("Lesson Details")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .setNegativeButton("REMOVE", (dialog, which) -> {
                        firebaseHandler.deleteLesson(lesson.getId(), aVoid -> {
                            List<Lesson> list = lessonByDate.get(LocalDate.parse(lesson.getDate()));
                            if (list != null) {
                                list.remove(lesson);
                                if (list.isEmpty()) {
                                    lessonByDate.remove(LocalDate.parse(lesson.getDate()));
                                }
                            }
                            calendarView.notifyDateChanged(LocalDate.parse(lesson.getDate()));
                            Toast.makeText(getContext(), "Lesson removed.", Toast.LENGTH_SHORT).show();
                        }, e -> {
                            Toast.makeText(getContext(), "Error on lesson removal", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .show();
        }, e -> Toast.makeText(getContext(), "Failed loading Users", Toast.LENGTH_SHORT).show());
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

                        LocalTime newStart = LocalTime.of(hour, minute);
                        LocalTime newEnd = newStart.plusHours(2);

                        List<Lesson> lessonsOnDate = lessonByDate.getOrDefault(pickedDate, new ArrayList<>());

                        boolean overlaps = lessonsOnDate.stream().anyMatch(existing -> {
                            if (!existing.getUserId().equals(userId)) return false;

                            LocalTime existingStart = LocalTime.parse(existing.getTime());
                            LocalTime existingEnd = existingStart.plusHours(2);

                            return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
                        });

                        if (overlaps) {
                            Toast.makeText(getContext(), "Lesson overlaps with an existing one for that user.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Lesson newLesson = new Lesson("", userId, pickedDate.toString(), time, notes);
                        firebaseHandler.addLesson(newLesson, aVoid -> {
                            lessonByDate.computeIfAbsent(pickedDate, k -> new ArrayList<>()).add(newLesson);
                            calendarView.notifyDateChanged(pickedDate);
                            Toast.makeText(getContext(), "Lesson created", Toast.LENGTH_SHORT).show();
                        }, e -> {
                            Toast.makeText(getContext(), "Error on lesson creation", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }, e -> Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show());
    }

    private void removeLesson(List<Lesson> lessons) {
        String[] items = new String[lessons.size()];
        for (int i = 0; i < lessons.size(); i++) {
            Lesson l = lessons.get(i);
            String name = uidToNameMap.getOrDefault(l.getUserId(), "No User Found");
            items[i] = name + " at " + l.getTime();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Which lesson to delete?")
                .setItems(items, (dialog, which) -> {
                    Lesson selectedLesson = lessons.get(which);
                    LocalDate date = LocalDate.parse(selectedLesson.getDate());
                    firebaseHandler.deleteLesson(selectedLesson.getId(), aVoid -> {
                        List<Lesson> dateLessons = lessonByDate.get(date);
                        if (dateLessons != null) {
                            dateLessons.remove(selectedLesson);
                            if (dateLessons.isEmpty()) {
                                lessonByDate.remove(date);
                            }
                        }
                        calendarView.notifyDateChanged(date);
                    }, e -> {
                        Toast.makeText(getContext(), "Error on deletion", Toast.LENGTH_SHORT).show();
                    });
                })
                .show();
    }

    private void pickSuggestion(List<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            Toast.makeText(getContext(), "No suggestions available", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] suggestionItems = suggestions.stream()
                .map(s -> {
                    String name = uidToNameMap.getOrDefault(s.getUserId(), "Unknown");
                    return name + " (" + s.getOldDate() + ")";
                })
                .toArray(String[]::new);
        new AlertDialog.Builder(requireContext())
                .setTitle("Suggestions")
                .setItems(suggestionItems, (dialog, which) -> {
                    showSuggestionDialog(suggestions.get(which));
                })
                .show();
    }

    private void showSuggestionDialog(Suggestion suggestion) {
        String fullName = uidToNameMap.getOrDefault(suggestion.getUserId(), "Unknown");

        String message = "Full Name: " + fullName +
                "\nFrom: " + suggestion.getOldDate() + " at " + suggestion.getOldTime() +
                "\nTo: " + suggestion.getNewDate() + " at " + suggestion.getNewTime() +
                "\nReason: " + suggestion.getReason();

        new AlertDialog.Builder(requireContext())
                .setTitle("Suggestion Details")
                .setMessage(message)
                .setPositiveButton("Apply", (dialog, which) -> {
                    firebaseHandler.updateLesson(suggestion.getLessonId(), suggestion, aVoid -> {
                        refreshSuggestionsCount();
                        LocalDate oldDate = LocalDate.parse(suggestion.getOldDate());
                        LocalDate newDate = LocalDate.parse(suggestion.getNewDate());
                        List<Lesson> oldList = lessonByDate.get(oldDate);
                        if (oldList != null) {
                            oldList.removeIf(lesson -> lesson.getId().equals(suggestion.getLessonId()));
                            if (oldList.isEmpty()) lessonByDate.remove(oldDate);
                        }
                        Lesson updatedLesson = new Lesson(
                                suggestion.getLessonId(),
                                suggestion.getUserId(),
                                suggestion.getNewDate(),
                                suggestion.getNewTime(),
                                suggestion.getReason()
                        );
                        updatedLesson.setId(suggestion.getLessonId());
                        lessonByDate.computeIfAbsent(newDate, k -> new ArrayList<>()).add(updatedLesson);

                        calendarView.notifyDateChanged(oldDate);
                        calendarView.notifyDateChanged(newDate);
                        Toast.makeText(getContext(), "Suggestion applied", Toast.LENGTH_SHORT).show();
                    }, e -> Toast.makeText(getContext(), "Failed to add lesson", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    firebaseHandler.deleteSuggestion(suggestion.getId(), aVoid -> {
                        refreshSuggestionsCount();
                        Toast.makeText(getContext(), "Suggestion denied", Toast.LENGTH_SHORT).show();
                    }, e -> {
                        Toast.makeText(getContext(), "Error, something unexpected happened", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void refreshSuggestionsCount() {
        firebaseHandler.getSuggestions(
                querySnapshot -> {
                    int count = querySnapshot.size();
                    if (count == 0) {
                        tvSuggestionCount.setText(getString(R.string.no_suggestions));
                        btnShowSuggestions.setEnabled(false);
                    } else {
                        tvSuggestionCount.setText(getString(R.string.suggestion_count, count));
                    }
                    suggestions.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Suggestion suggestion = doc.toObject(Suggestion.class);
                        if (suggestion != null) {
                            suggestions.add(suggestion);
                        }
                    }
                },
                e -> {
                    tvSuggestionCount.setText(getString(R.string.error_loading_suggestions));
                    btnShowSuggestions.setEnabled(false);
                }
        );
    }

    public static class DayViewContainer extends ViewContainer {
        public final TextView textView;
        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
}
