package sexy.tad.lunatic.sample;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import sexy.tad.lunatic.DatePickerView;
import sexy.tad.lunatic.JavaUtil;
import sexy.tad.lunatic.Joda;
import sexy.tad.lunatic.Session;
import sexy.tad.lunatic.ThreeTen;

import org.joda.time.DateTimeConstants;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LunaticActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ViewPagerAdapter());

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);
    }

    private class ViewPagerAdapter extends PagerAdapter {
        private static final String THREETEN = "threeten";
        private static final String JODA = "joda";
        private static final String JAVA_UTIL = "java.util";

        private DatePickerView mThreetenPicker;
        private DatePickerView mJodaPicker;
        private DatePickerView mJavaUtilPicker;

        ViewPagerAdapter() {
            mThreetenPicker = picker(THREETEN);
            threetenSession(mThreetenPicker);

            mJodaPicker = picker(JODA);
            jodaSession(mJodaPicker);

            mJavaUtilPicker = picker(JAVA_UTIL);
            javaUtilSession(mJavaUtilPicker);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                    container.addView(mThreetenPicker);
                    return THREETEN;
                case 1:
                    container.addView(mJodaPicker);
                    return JODA;
                case 2:
                    container.addView(mJavaUtilPicker);
                    return JAVA_UTIL;
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            switch (position) {
                case 0:
                    container.removeView(mThreetenPicker);
                    break;
                case 1:
                    container.removeView(mJodaPicker);
                    break;
                case 2:
                    container.removeView(mJavaUtilPicker);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return THREETEN;
                case 1:
                    return JODA;
                case 2:
                    return JAVA_UTIL;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.getTag().equals(object);
        }

        private DatePickerView picker(String tag) {
            DatePickerView picker = new DatePickerView(LunaticActivity.this);
            picker.setTag(tag);
            picker.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return picker;
        }

        private Session threetenSession(DatePickerView datePicker) {
            return Session.with(datePicker)
                    .setOptions(ThreeTen.newOptionsBuilder()
                            .setMin(LocalDate.of(2015, Month.JANUARY, 1))
                            .setMax(LocalDate.of(2099, Month.DECEMBER, 31))
                            .setFirstDayOfWeek(DayOfWeek.SUNDAY)
                            .setWeekdayFormat("ccc")
                            .build());
        }

        private Session jodaSession(DatePickerView datePicker) {
            return Session.with(datePicker)
                    .setOptions(Joda.newOptionsBuilder()
                            .setMin(new org.joda.time.LocalDate(2015, DateTimeConstants.JANUARY, 1))
                            .setMax(new org.joda.time.LocalDate(2099, DateTimeConstants.DECEMBER, 31))
                            .setFirstDayOfWeek(DateTimeConstants.SUNDAY)
                            .setWeekdayFormat("ccc")
                            .build());
        }

        private Session javaUtilSession(DatePickerView datePicker) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.set(2015, Calendar.JANUARY, 1);
            Date min = cal.getTime();
            cal.set(2099, Calendar.DECEMBER, 31);
            Date max = cal.getTime();
            return Session.with(datePicker)
                    .setOptions(JavaUtil.newOptionsBuilder()
                            .setMin(min)
                            .setMax(max)
                            .setFirstDayOfWeek(Calendar.SUNDAY)
                            .setWeekdayFormat("ccc")
                            .build());
        }
    }
}
