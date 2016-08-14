package com.liwn.zzl.markbit.mark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.liwn.zzl.markbit.AllMarkItemActivity;
import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.MarkItemFragment;
import com.liwn.zzl.markbit.MarkItemFragment.OnListFragmentInteractionListener;
import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.mark.DummyContent.DummyItem;

import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMarkItemRecyclerViewAdapter extends RecyclerView.Adapter<MyMarkItemRecyclerViewAdapter.ViewHolder> {

    private final Map<Integer, DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyMarkItemRecyclerViewAdapter(Map<Integer, DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_markitem, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mImgView.setImageBitmap(mValues.get(position).img);
//        holder.mIdView.setText(String.valueOf(mValues.get(position).control_id));
//        holder.mNameView.setText(mValues.get(position).filePath);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onMarkItemFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public final View mView;
        public final Context mParentContext;
        public final ImageView mImgView;
//        public final TextView mIdView;
//        public final TextView mNameView;
        public DummyItem mItem;

        public ViewHolder(View view, Context context) {
            super(view);
            mView = view;
            mParentContext = context;
            mImgView = (ImageView) view.findViewById(R.id.markItem_img);
//            mIdView = (TextView) view.findViewById(R.id.markItem_id);
//            mNameView = (TextView) view.findViewById(R.id.markItem_name);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            menu.add(Menu.NONE, MarkItemFragment.MENU1, 0, R.string.delete_mark).setOnMenuItemClickListener(mOnMenu1ClickListener);
            menu.add(Menu.NONE, MarkItemFragment.MENU2, 0, R.string.add_new_mark_by_this).setOnMenuItemClickListener(mOnMenu2ClickListener);
        }

        private final MenuItem.OnMenuItemClickListener mOnMenu1ClickListener = new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: add the menu1 procedure
                Toast.makeText(MarkBitApplication.applicationContext, "" +  mItem.control_id, Toast.LENGTH_SHORT).show();
                return false;
            }
        };

        private final MenuItem.OnMenuItemClickListener mOnMenu2ClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: add the menu2 procedure
                Toast.makeText(MarkBitApplication.applicationContext, "" +  mItem.control_id, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(mParentContext, AllMarkItemActivity.class);
                i.putExtra(MarkItemFragment.OLD_POS_ID, mItem.position);

                ((Activity)mParentContext).startActivityForResult(i, MarkItemFragment.REQUEST_CHOOSE_NEW_MARK);
                return false;
            }
        };
    }
}
