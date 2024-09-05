package com.example.teamnovapersonalprojectprojecting.ui.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.teamnovapersonalprojectprojecting.R;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> days;

    private int currentDay;
    private int currentMonth;
    private int currentYear;
    private Calendar calendar;

    public CalendarAdapter(Context context, ArrayList<String> days, int currentDay, int currentMonth, int currentYear) {
        this.context = context;
        this.days = days;
        this.currentDay = currentDay;
        this.currentMonth = currentMonth;
        this.currentYear = currentYear;
        this.calendar = Calendar.getInstance();
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }
        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.height = (int) (parent.getHeight() / 5);
        convertView.setLayoutParams(params);

        TextView dayText = convertView.findViewById(R.id.day_text);
        dayText.setText(days.get(position));

        // 현재 날짜와 일치하는 경우 색상 변경
        if (!days.get(position).isEmpty()) {
            int day = Integer.parseInt(days.get(position));
            calendar.set(Calendar.DAY_OF_MONTH, day);
            dayText.setBackgroundColor(Color.TRANSPARENT); // 다른 날짜는 투명 배경

            if (calendar.get(Calendar.DAY_OF_MONTH) == currentDay &&
                    calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.YEAR) == currentYear) {
                convertView.setBackgroundColor(0xFF97C6CA); // 현재 날짜 배경색
            }
        } else {
            dayText.setBackgroundColor(Color.TRANSPARENT); // 빈 날짜는 투명 배경
        }

        return convertView;
    }
}
