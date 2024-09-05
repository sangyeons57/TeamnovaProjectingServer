package com.example.teamnovapersonalprojectprojecting.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.R;

import java.util.ArrayList;

import java.util.Calendar;

public class CalendarFragment extends Fragment {
    private GridView calendarGrid;
    private ArrayList<String> days;
    private CalendarAdapter calendarAdapter;

    private TextView monthYearText;
    private int currentDay;
    private int currentMonth;
    private int currentYear;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarGrid = view.findViewById(R.id.calendar_grid);
        monthYearText = view.findViewById(R.id.month_year_text);
        days = new ArrayList<>();

        calendarGrid.setVerticalScrollBarEnabled(false);
        calendarGrid.setHorizontalFadingEdgeEnabled(false);

        // 현재 날짜 가져오기
        Calendar calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentMonth = calendar.get(Calendar.MONTH); // 0-11, 0=January
        currentYear = calendar.get(Calendar.YEAR);

        //월과 년도 표시
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthYearText.setText(monthNames[currentMonth] + " " + currentYear);


        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(""); // 빈 날짜
        }

        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }

        calendarAdapter = new CalendarAdapter(view.getContext(), days, currentDay, currentMonth, currentYear);
        calendarGrid.setAdapter(calendarAdapter);

        calendarGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String day = days.get(position);
                if (!day.isEmpty()) {
                    Toast.makeText(view.getContext(), "날짜 클릭됨: " + day, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}