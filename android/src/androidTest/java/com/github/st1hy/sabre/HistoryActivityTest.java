package com.github.st1hy.sabre;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.github.st1hy.sabre.history.HistoryActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HistoryActivityTest {

    @Rule
    public final ActivityTestRule<HistoryActivity> main = new ActivityTestRule<>(HistoryActivity.class);

    @Test
    public void testActivity() {
        onView(withId(R.id.history_toolbar)).check(matches(isDisplayed()));
    }
}