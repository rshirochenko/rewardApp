package info.androidhive.fitnessreward.activity;
/**
 * Created by Daria, Roma, Alper
 */
import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import info.androidhive.fitnessreward.auxilary.TestDataMonth;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import info.androidhive.fitnessreward.R;

public class StatsFragment extends Fragment {
    private Cursor cursor;
    private DataBase mDbHelper;
    private SQLiteDatabase mDb;
    private GraphView graph1;
    private GraphView graph2;
    private Button mButtonStart;
    private Button mButtonEnd;
    private TextView mWeekData;
    public static Calendar mCalendar;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private int mDay;
    private int mMonth;
    private int mYear;
    private int mPosition;
    private GregorianCalendar mStartDate;
    private GregorianCalendar mEndDate;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check for data presence in DB, if not fill with dump data
        checkDatabaseForData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        /*
        Spinner (choice measurements type)
         */
        mWeekData = (TextView) rootView.findViewById(R.id.text_week_data);
        Spinner graphSpinner = (Spinner) rootView.findViewById(R.id.graph_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.graph_choice, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpinner.setAdapter(adapter);
        graphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d("item selected", "item selected");
                changeGraphs(position);
                mPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        /*
        DatePickers` buttons
        */
        mButtonStart = (Button) rootView.findViewById(R.id.start_date);
        mButtonStart.setText("Set start date");
        mButtonEnd = (Button) rootView.findViewById(R.id.end_date);
        mButtonEnd.setText("Set end date");
        //Button to pick start date
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                        updateButtonStart();
                        setCustomDateGraph();
                    }
                };
                final Calendar c = Calendar.getInstance();
                DatePickerDialog d = new DatePickerDialog(getActivity(),
                        R.style.Base_Theme_AppCompat, mDateSetListener, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                d.show();
            }
        });
        //Button to pick end date
        mButtonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                        updateButtonEnd();
                        setCustomDateGraph();
                    }
                };
                final Calendar c = Calendar.getInstance();
                DatePickerDialog d = new DatePickerDialog(getActivity(),
                        R.style.Base_Theme_AppCompat, mDateSetListener, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                d.show();
            }
        });

         /*
         Weekly graph
         */
        graph1 = (GraphView) rootView.findViewById(R.id.graph1);
        DataPoint[] weeklyPoints = this.createDataPoints(7,1);

        BarGraphSeries<DataPoint> seriesWeek = new BarGraphSeries<DataPoint>(weeklyPoints);
        graph1.addSeries(seriesWeek);

        // styling
        seriesWeek.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });

        seriesWeek.setSpacing(50);
        seriesWeek.setDrawValuesOnTop(true);

        /*
         Monthly graph
         */
        graph2 = (GraphView) rootView.findViewById(R.id.graph2);
        DataPoint[] monthlyPoints = this.createDataPoints(30,1);
        graph2.getViewport().setMaxX(32);

        BarGraphSeries<DataPoint> seriesMonth = new BarGraphSeries<DataPoint>(monthlyPoints);
        graph2.addSeries(seriesMonth);

        // styling
        seriesMonth.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });

        seriesMonth.setSpacing(30);
        seriesMonth.setDrawValuesOnTop(false);
        seriesMonth.setValuesOnTopColor(Color.RED);
        seriesWeek.setDrawValuesOnTop(true);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Change the graph data on choosing different value in Spinner
     * @param position - spinner position number
     */
    private void changeGraphs(int position){
        BarGraphSeries<DataPoint> seriesWeek = new BarGraphSeries<DataPoint>(createDataPoints(7, position));
        BarGraphSeries<DataPoint> seriesMonth = new BarGraphSeries<DataPoint>(createDataPoints(30, position));

        graph1.removeAllSeries();
        graph1.addSeries(seriesWeek);

        // styling
        seriesWeek.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });
        seriesWeek.setSpacing(50);
        seriesWeek.setDrawValuesOnTop(true);
        seriesWeek.setSpacing(30);

        graph2.removeAllSeries();
        graph2.addSeries(seriesMonth);
        // styling
        seriesMonth.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });
        graph2.getViewport().setMaxX(32);
        mWeekData.setText("Statistics for the Week time");

    }

    /**
     * Read the data from DB and format its in GraphView library compatible format
     * @param days - number of days to show (int)
     * @param position - spinner position (int)
     * @return - datapoint array in GraphView library compatible format
     */
    private DataPoint[] createDataPoints(int days, int position){
        //Choice parameter
        String parameter = "distance";
        switch (position){
            case 0:
                parameter = "distance";
                break;
            case 1:
                parameter = "steps";
                Log.d("Steps choice", Integer.toString(position));
                break;
            case 2:
                parameter = "calories";
                break;
        }
        mDbHelper = new DataBase(getActivity());
        mDb = mDbHelper.getReadableDatabase();
        DataPoint[] dataPoints = new DataPoint[days+1];
        Cursor mCursor = mDbHelper.getLastNDataRecord(days);
        int i = 0;
        //Read through database
        while(mCursor.moveToNext()){
            String distance = mCursor.getString(mCursor.getColumnIndex(parameter));
            //String day_month = mCursor.getString(mCursor.getColumnIndex("date")).substring(5, 10);
            try {
                dataPoints[i] = new DataPoint(i+1, Integer.parseInt(distance));
            } catch (NullPointerException e) {
                dataPoints[i] = new DataPoint(i+1, 1);
                e.printStackTrace();
            }
            i++;
        }
        dataPoints[i] = new DataPoint(i+1, 0);
        Log.d("Requested ", Integer.toString(i));
        mDbHelper.close();
        return dataPoints;
    }

    /**
     * Read the data from DB and format them for custom dates chose
     * @param position - spinner position(int)
     * @return datapoint array in GraphView library compatible format
     */
    private DataPoint[] createCustomDataPoints(int position){
        GregorianCalendar mToday = new GregorianCalendar();
        int differenceStartDayToday = dayDifference(mStartDate,mToday);
        int differenceEndDayToday = dayDifference(mEndDate,mToday);
        if (differenceEndDayToday < 0 || differenceStartDayToday < 0) {
            Toast.makeText(getActivity(), "Chose date earlier than today", Toast.LENGTH_SHORT).show();
        }
        int days = differenceStartDayToday;
        //Choice parameter
        String parameter = "distance";
        switch (mPosition){
            case 0:
                parameter = "distance";
                break;
            case 1:
                parameter = "steps";
                Log.d("Steps choice", Integer.toString(position));
                break;
            case 2:
                parameter = "calories";
                break;
        }
        mDbHelper = new DataBase(getActivity());
        mDb = mDbHelper.getReadableDatabase();
        DataPoint[] dataPoints = new DataPoint[days+1];
        Cursor mCursor = mDbHelper.getLastNDataRecord(days);

        for (int i = 0; i < days; i++) {
            try {
                mCursor.moveToNext();
                String distance = mCursor.getString(mCursor.getColumnIndex(parameter));
                dataPoints[i] = new DataPoint(i+1, Integer.parseInt(distance));
            } catch (Exception e) {
                e.printStackTrace();
                dataPoints[i] = new DataPoint(i+1, 0);
            }
        }

        mDbHelper.close();
        DataPoint[] dataCustomPoints = Arrays.copyOfRange(dataPoints, 0, days-differenceEndDayToday);
        return dataCustomPoints;
    }

    /**
     * Chech database for data presence
     */
    private void checkDatabaseForData(){
        mDbHelper = new DataBase(getActivity());
        mDb = mDbHelper.getWritableDatabase();
        Cursor cursor = mDbHelper.getLastDataRecord();
        if ((cursor == null) || (cursor.getCount() < 29)) {
                TestDataMonth testData = new TestDataMonth();
                ArrayList<String[]> testDataArray = testData.createTestData();
                for (String[] testDay : testDataArray) {
                    String date = testDay[0];
                    String steps = testDay[1];
                    String distance = testDay[2];
                    String calories = testDay[3];
                    mDbHelper.createNewDataRecord(date, steps, distance, calories);
                }
        }
        mDbHelper.close();
    }

    /**
     * Change graphs data when custom date was chosen
     */
    private void setCustomDateGraph(){
        if(mStartDate != null && mEndDate != null){
            if (mEndDate.compareTo(mStartDate) > 0 && mEndDate.compareTo(new GregorianCalendar()) <= 0) {
                dayDifference(mStartDate, mEndDate);
                BarGraphSeries<DataPoint> seriesWeek = new BarGraphSeries<DataPoint>(createCustomDataPoints(mPosition));

                graph1.removeAllSeries();
                graph1.addSeries(seriesWeek);
                mWeekData.setText("Statistics for the time range");

                // styling
                seriesWeek.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                    @Override
                    public int get(DataPoint data) {
                        return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
                    }
                });
                seriesWeek.setSpacing(50);
                seriesWeek.setDrawValuesOnTop(true);
                seriesWeek.setSpacing(30);

            } else {
                Toast.makeText(getActivity(), "End date is earlier than start date", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (mStartDate != null) {
                Toast.makeText(getActivity(), "Choice start date", Toast.LENGTH_SHORT).show();
            }
            if (mEndDate != null) {
                Toast.makeText(getActivity(), "Choice end date", Toast.LENGTH_SHORT).show();
            }
         }
    }

    /**
     * Change text for From date button
     */
    private void updateButtonStart() {
        GregorianCalendar c = new GregorianCalendar(mYear, mMonth, mDay);
        mStartDate = c;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        mButtonStart.setText(sdf.format(c.getTime()));
    }

    /**
     * Change text for Until date button
     */
    private void updateButtonEnd() {
        GregorianCalendar c = new GregorianCalendar(mYear, mMonth, mDay);
        mEndDate = c;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        mButtonEnd.setText(sdf.format(c.getTime()));
    }

    /**
     * Calculates difference between two dates in days
     * @param cal1 - Date 1 (GregorianCalendar format)
     * @param cal2 - Date 2 (GregorianCalendar format)
     * @return - int value number of days difference
     */
    private int dayDifference(GregorianCalendar cal1,GregorianCalendar cal2){
        Date d1=cal1.getTime();
        Date d2=cal2.getTime();

        long diff=d2.getTime()-d1.getTime();
        int diffDays=(int)(diff/(1000*24*60*60));

        return diffDays;
    }
}