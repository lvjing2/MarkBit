package com.liwn.zzl.markbit.mark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.liwn.zzl.markbit.DrawActivity;
import com.liwn.zzl.markbit.FileIO;
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
    private Context mContext;

    public MyMarkItemRecyclerViewAdapter(Context context, Map<Integer, DummyItem> items, OnListFragmentInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_markitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mImgView.setImageBitmap(mValues.get(position).getImg());
        holder.mIdView.setText(String.valueOf(mValues.get(position).getPosition()));
        holder.mNameView.setText(mValues.get(position).getFilePath());

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
        public final ImageView mImgView;
        public final TextView mIdView;
        public final TextView mNameView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgView = (ImageView) view.findViewById(R.id.markItem_img);
            mIdView = (TextView) view.findViewById(R.id.markItem_id);
            mNameView = (TextView) view.findViewById(R.id.markItem_name);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(Menu.NONE, MarkItemFragment.MENU1, 0, R.string.edit_mark).setOnMenuItemClickListener(mOnEditClickListener);
            menu.add(Menu.NONE, MarkItemFragment.MENU2, 0, R.string.replace_mark).setOnMenuItemClickListener(mOnReplaceClickListener);
            if (mItem.isEmpty() == false) {
                // this item is valued
                menu.add(Menu.NONE, MarkItemFragment.MENU3, 0, R.string.delete_mark).setOnMenuItemClickListener(mOnDeleteClickListener);
            }
        }

        private final MenuItem.OnMenuItemClickListener mOnEditClickListener = new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: add the menu1 procedure
                Intent i = new Intent(mContext, DrawActivity.class);
                i.putExtra("baseMarkPath", mItem.getFilePath());
                ((Activity) mContext).startActivityForResult(i, MarkItemFragment.REQUEST_CODE_EDIT_MARK);
                return false;
            }
        };

        private final MenuItem.OnMenuItemClickListener mOnReplaceClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: add the menu2 procedure

                Uri uri = Uri.parse(FileIO.getMediaFolderName());
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setDataAndType(uri, "file/*");
                i.putExtra("position",          mItem.getPosition());
                i.putExtra("is_empty",          mItem.isEmpty());
                i.putExtra("control_id",        mItem.getControlId());
                i.putExtra("server_id",         mItem.getServerId());
                i.putExtra("is_control_synced", mItem.isControlSynced());
                i.putExtra("is_server_synced",  mItem.isServerSynced());
                i.putExtra("version",           mItem.getVersion());
                i.putExtra("date",              mItem.getDate());
                i.putExtra("tag",               mItem.getTag());
                i.putExtra("filePath",          mItem.getFilePath());
                i.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    ((Activity) mContext).startActivityForResult(i, MarkItemFragment.REQUEST_CODE_REPLACE_MARK);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(mContext, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                return false;
            }
        };

        private final MenuItem.OnMenuItemClickListener mOnDeleteClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                mItem.setEmpty(true);
                FileIO.deleteFileByPath(mItem.getFilePath());
//                FileIO.saveBitmap(MarkBitApplication.applicationContext, MarkBitApplication.defaultBitmap, mItem.getFilePath());
                mItem.setImg(MarkBitApplication.defaultBitmap);
                notifyDataSetChanged();

                return true;
            }
        };
    }

}
