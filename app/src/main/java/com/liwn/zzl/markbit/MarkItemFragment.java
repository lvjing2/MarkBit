package com.liwn.zzl.markbit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.liwn.zzl.markbit.mark.DummyContent;
import com.liwn.zzl.markbit.mark.DummyContent.DummyItem;
import com.liwn.zzl.markbit.mark.MyMarkItemRecyclerViewAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MarkItemFragment extends Fragment {
    private final static String TAG = MarkItemFragment.class.getSimpleName();

    public static final int MENU1 = 1;
    public static final int MENU2 = 2;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters

    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;
    public Context mContext;
    public static final String OLD_POS_ID = "OLD_POS_ID";
    public static final String NEW_POS_ID = "NEW MARK ID";
    public static final int REQUEST_CHOOSE_NEW_MARK_A = 6;
    public static final int REQUEST_CHOOSE_NEW_MARK_B = 7;

    private RecyclerView recyclerView_A;
    private RecyclerView recyclerView_B;
    private Button switcher_A;
    private Button switcher_B;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MarkItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MarkItemFragment newInstance(int columnCount) {
        MarkItemFragment fragment = new MarkItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markitem_list, container, false);
        final View recyclerView_A = view.findViewById(R.id.list_A);
        final View recyclerView_B = view.findViewById(R.id.list_B);
        switcher_A = (Button) view.findViewById(R.id.switcher_A);
        switcher_B = (Button) view.findViewById(R.id.switcher_B);


        // Set the adapter
        if (recyclerView_A instanceof RecyclerView) {
            Context context = recyclerView_A.getContext();
            this.recyclerView_A = (RecyclerView) recyclerView_A;
            DisplayMetrics displayMetrics = MarkBitApplication.applicationContext.getResources().getDisplayMetrics();
            mColumnCount =  (int) (displayMetrics.widthPixels / getResources().getDimension(R.dimen.itemHeight));
            if (mColumnCount <= 1) {
                this.recyclerView_A.setLayoutManager(new LinearLayoutManager(context));
            } else {
                this.recyclerView_A.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            mContext = getContext();
            this.recyclerView_A.setAdapter(new MyMarkItemRecyclerViewAdapter(MarkBitApplication.dummyContent.ITEM_MAP_A, mListener));
            this.recyclerView_A.setSelected(true);
        }
        if (recyclerView_B instanceof RecyclerView) {
            Context context = recyclerView_B.getContext();
            this.recyclerView_B = (RecyclerView) recyclerView_B;
            DisplayMetrics displayMetrics = MarkBitApplication.applicationContext.getResources().getDisplayMetrics();
            mColumnCount =  (int) (displayMetrics.widthPixels / getResources().getDimension(R.dimen.itemHeight));
            if (mColumnCount <= 1) {
                this.recyclerView_B.setLayoutManager(new LinearLayoutManager(context));
            } else {
                this.recyclerView_B.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            mContext = getContext();
            this.recyclerView_B.setAdapter(new MyMarkItemRecyclerViewAdapter(MarkBitApplication.dummyContent.ITEM_MAP_B, mListener));
            this.recyclerView_B.setSelected(true);
        }

        switcher_A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView_A.setVisibility(View.VISIBLE);
                recyclerView_B.setVisibility(View.GONE);
            }
        });
        switcher_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView_A.setVisibility(View.GONE);
                recyclerView_B.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.index_setting);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMarkItemFragmentInteraction(DummyItem item);
    }

    public void replaceMark(boolean type, int old_id, int new_id) {
        DummyContent.replaceItem(type, old_id, new_id);

        if (type) {
            Log.e(TAG, "notify A");
            recyclerView_A.getAdapter().notifyDataSetChanged();
        } else {
            Log.e(TAG, "notify B");
            recyclerView_B.getAdapter().notifyDataSetChanged();
        }
    }

    public void updateMark(int num) {
        DummyContent.updateItem(num);
        // TODO: change index num
//        recyclerView_A.getAdapter().notifyDataSetChanged();
//        recyclerView_B.getAdapter().notifyDataSetChanged();
    }

    public void updateAllMark(int num) {
        DummyContent.updateAllItem(num);
    }
}
