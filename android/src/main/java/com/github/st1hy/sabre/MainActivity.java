package com.github.st1hy.sabre;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.github.st1hy.core.utils.ArgumentChangedHandler;
import com.github.st1hy.core.utils.BacktrackAware;
import com.github.st1hy.imagecache.CacheHandler;
import com.github.st1hy.imagecache.CacheProvider;
import com.github.st1hy.sabre.core.DependencyDelegate;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, CacheProvider {
    private volatile boolean stopped = false;
    private final List<Runnable> afterRestore = new LinkedList<>();
    private DependencyDelegate dependencyDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, NavState.HISTORY.newInstance()).commit();
        }
    }

    public DependencyDelegate getDependencyDelegate() {
        if (dependencyDelegate == null) {
            synchronized (this) {
                if (dependencyDelegate == null) {
                    dependencyDelegate = new DependencyDelegate(this);
                }
            }
        }
        return dependencyDelegate;
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopped = false;
        getDependencyDelegate().onStart();
        for (Runnable task: afterRestore) {
            task.run();
        }
        afterRestore.clear();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopped = true;
        getDependencyDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDependencyDelegate().onDestroy();
    }

    public boolean setState(final NavState newState, final Bundle bundle) {
        if (stopped) {
            afterRestore.add(new Runnable() {
                @Override
                public void run() {
                    setState(newState, bundle);
                }
            });
            return false;
        }
        if (newState == getCurrentState()) {
            return false;
        }
        if (newState == NavState.HISTORY) {
            String tag = getFragmentManager().getBackStackEntryAt(0).getName();
            getFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return true;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(newState.name());
        if (fragment != null) {
            if (fragment instanceof ArgumentChangedHandler) ((ArgumentChangedHandler) fragment).onArgumentsChanged(bundle);
            getSupportFragmentManager().popBackStack(newState.name(), 0);
            return true;
        }
        fragment = newState.newInstance();
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_activity_fragment_container, fragment, newState.name());
        transaction.addToBackStack(newState.name());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_fragment_container);
        if (fragment instanceof BacktrackAware && ((BacktrackAware) fragment).handleBacktrackEvent()) {
            return;
        }
        super.onBackPressed();
    }

    public NavState getCurrentState() {
        int backStackCount = getFragmentManager().getBackStackEntryCount();
        if (backStackCount == 0) {
            return NavState.HISTORY;
        }
        return NavState.valueOf(getFragmentManager().getBackStackEntryAt(backStackCount - 1).getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    //Called using menu_history.xml
    public void onActionSettings(final MenuItem item) {
        setState(NavState.SETTINGS, null);
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    @NonNull
    public CacheHandler getCacheHandler() {
        return getDependencyDelegate().getCacheHandler();
    }
}
