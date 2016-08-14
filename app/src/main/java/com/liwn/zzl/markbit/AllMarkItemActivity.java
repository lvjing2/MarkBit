package com.liwn.zzl.markbit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;

import com.liwn.zzl.markbit.mark.AllMarkItemRecyclerViewAdapter;

public class AllMarkItemActivity extends AppCompatActivity {
    private final static String TAG = AllMarkItemActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private int mColumnCount;
    private int old_position_id;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_mark_item);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        DisplayMetrics displayMetrics = MarkBitApplication.applicationContext.getResources().getDisplayMetrics();
        mColumnCount =  (int) (displayMetrics.widthPixels / getResources().getDimension(R.dimen.allItemWidth));
        recyclerView = (RecyclerView) findViewById(R.id.all_mark_item);

        old_position_id = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            old_position_id = extras.getInt(MarkItemFragment.OLD_POS_ID);
//            Log.e(TAG, "extras bundle is not null");
        }
//        Log.e(TAG, "old position: " + old_position_id);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, mColumnCount));
        }

        recyclerView.setAdapter(new AllMarkItemRecyclerViewAdapter(old_position_id, MarkBitApplication.dummyContent.ALL_ITEM_MAP));
        recyclerView.setSelected(true);
    }
}
