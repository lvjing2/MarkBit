package com.liwn.zzl.markbit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liwn.zzl.markbit.mark.DummyContent.DummyItem;
import com.liwn.zzl.markbit.mark.MyMarkItemRecyclerViewAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MarkItemFragment extends Fragment {

    public static final int MENU1 = 1;
    public static final int MENU2 = 2;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    private void loadData() {
        File[] marks = FileIO.getFiles();
        for (int i = 0; i < marks.length; ++i) {
            File mark = marks[i];
            String fileName = mark.getName().split("\\.")[0];
            String[] fileInfos = fileName.split("_");
            String tag;
            int id;
            String date;
            String filePath = mark.getAbsolutePath();
            if (fileInfos.length >= 4) {
                id = Integer.valueOf(fileInfos[1]);
                date = fileInfos[2];
                tag = fileInfos[3];
            } else {
                id = Integer.valueOf(FileIO.default_num);
                date = FileIO.default_date;
                tag = FileIO.default_tag;
            }
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(mark);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, MarkBitApplication.opts);
                MarkBitApplication.dummyContent.setDummyContentItem(id, id, id, false, false, "v0.0", bitmap, filePath);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }


            loadData();
            recyclerView.setAdapter(new MyMarkItemRecyclerViewAdapter(MarkBitApplication.dummyContent.ITEM_MAP, mListener));
            recyclerView.setSelected(true);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.marks_management);
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


}
