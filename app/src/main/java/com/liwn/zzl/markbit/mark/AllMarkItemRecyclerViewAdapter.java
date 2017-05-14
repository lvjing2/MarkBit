package com.liwn.zzl.markbit.mark;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.liwn.zzl.markbit.MarkItemFragment;
import com.liwn.zzl.markbit.MarkItemFragment.OnListFragmentInteractionListener;
import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.mark.DummyContent.DummyItem;

import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class AllMarkItemRecyclerViewAdapter extends RecyclerView.Adapter<AllMarkItemRecyclerViewAdapter.ViewHolder> {

    private final Map<Integer, DummyItem> mValues;
    private int old_position_id;
    private int old_control_id;
    public AllMarkItemRecyclerViewAdapter(int old_position_id, int old_control_id, Map<Integer, DummyItem> items) {
        mValues = items;
        this.old_position_id = old_position_id;
        this.old_control_id = old_control_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_allmarkitem, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mImgView.setImageBitmap(mValues.get(position).img);
        holder.old_position_id = old_position_id;
        holder.old_control_id = old_control_id;
//        holder.mIdView.setText(String.valueOf(mValues.get(position).position));
//        holder.mNameView.setText(mValues.get(position).filePath);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final String TAG = ViewHolder.class.getSimpleName();
        public final View mView;
        public final Context mParentContext;
        public final ImageView mImgView;


        @Override
        public void onClick(View v) {
            Log.d(TAG, "clicked on id: " + mItem.control_id);
            new AlertDialog.Builder(mView.getContext())
                    .setTitle(mView.getResources().getString(R.string.mark_replace_title))
                    .setMessage(mView.getResources().getString(R.string.mark_replace_message))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent i = new Intent();
                            i.putExtra(MarkItemFragment.OLD_POS_ID, old_position_id);
                            i.putExtra(MarkItemFragment.OLD_CTL_ID, old_control_id);
                            i.putExtra(MarkItemFragment.NEW_POS_ID, old_position_id);
                            i.putExtra(MarkItemFragment.NEW_CTL_ID, mItem.control_id);
                            ((Activity)mParentContext).setResult(Activity.RESULT_OK, i);
                            ((Activity)mParentContext).finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .show();
        }

//        public final TextView mIdView;
//        public final TextView mNameView;
        public DummyItem mItem;
        public int old_position_id;
        public int old_control_id;

        public ViewHolder(View view, Context context) {
            super(view);
            mView = view;
            mParentContext = context;
            mImgView = (ImageView) view.findViewById(R.id.mark_allItem_img);
//            mIdView = (TextView) view.findViewById(R.id.markItem_id);
//            mNameView = (TextView) view.findViewById(R.id.markItem_name);
//            view.setOnCreateContextMenuListener(this);
            view.setOnClickListener(this);
        }
    }
}
