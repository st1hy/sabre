package com.github.st1hy.sabre;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.github.st1hy.sabre.core.CacheHandler;
import com.github.st1hy.sabre.core.DependencyDelegate;
import com.github.st1hy.sabre.core.cache.CacheProvider;
import com.github.st1hy.sabre.core.util.ArgumentChangedHandler;
import com.github.st1hy.sabre.core.util.BacktrackAware;
import com.github.st1hy.sabre.history.content.HistoryUtils;
import com.github.st1hy.sabre.image.ImageActivity;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, CacheProvider {
    private static final int REQUEST_IMAGE = 0x16ed;
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

    //Called using menu_history.xml
    public void onActionOpen(final MenuItem item) {
        onActionOpen();
    }

    //Called using history_open_floating_button.xml
    public void onActionOpen(final View view) {
        onActionOpen();
    }

    private void onActionOpen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE:
                if (resultCode == RESULT_OK && null != data) {
                    Uri uri = data.getData();
                    openImage(uri);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void openImage(final Uri uri) {
        final Context context = getApplicationContext();
        final Date date = new Date();
        Application.CACHED_EXECUTOR_POOL.execute(new Runnable() {
            @Override
            public void run() {
                HistoryUtils.updateDatabaseWithImage(context, uri, date, true);
            }
        });
        final Bundle arguments = new Bundle();
        arguments.putParcelable(NavState.ARG_IMAGE_URI, uri);
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtras(arguments);
        startActivity(intent);
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public CacheHandler getCacheHandler() {
        return getDependencyDelegate().getCacheHandler();
    }
}
