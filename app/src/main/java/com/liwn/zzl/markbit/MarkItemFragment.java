package com.liwn.zzl.markbit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.liwn.zzl.markbit.mark.DummyContent.DummyItem;
import com.liwn.zzl.markbit.mark.MyMarkItemRecyclerViewAdapter;
import com.liwn.zzl.markbit.tools.Tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static final int MENU3 = 3;
    public static final int REQUEST_CODE_REPLACE_MARK = 1;
    public static final int REQUEST_CODE_EDIT_MARK = 2;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;
    private MyMarkItemRecyclerViewAdapter mAdapter;

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
                MarkBitApplication.dummyContent.setDummyContentItem(id, false, id, id, false, false, "v0.0", bitmap, filePath);

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
            mAdapter = new MyMarkItemRecyclerViewAdapter(getContext(), MarkBitApplication.dummyContent.ITEM_MAP, mListener);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setSelected(true);
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_REPLACE_MARK:
                    Uri uri = data.getData();


                    File file = FileIO.getFile(getContext(), uri);
                    String chooseFilePath = file.getAbsolutePath();
                    String extension = FileIO.getFileExtension(chooseFilePath);
                    Bitmap bitmap = null;

                    if (extension.equals("png")) {
                        bitmap = BitmapFactory.decodeFile(chooseFilePath, MarkBitApplication.opts);
                    } else if (extension.equals("bin")) {
                        bitmap = bin2Bitmap(file);
                    }

                    Bundle bundle = data.getExtras();
                    int position;
                    boolean is_empty;
                    int control_id;
                    int server_id;
                    boolean is_control_synced;
                    boolean is_server_synced;
                    String version;
                    String date;
                    String tag;
                    String filePath;
                    if (bundle != null) {
                        position = bundle.getInt("position");
                        is_empty = bundle.getBoolean("is_empty");
                        control_id = bundle.getInt("control_id");
                        server_id = bundle.getInt("server_id");
                        is_control_synced = bundle.getBoolean("is_control_synced");
                        is_server_synced = bundle.getBoolean("is_server_synced");
                        version = bundle.getString("version");
                        date = bundle.getString("date");
                        tag = bundle.getString("tag");
                        filePath = bundle.getString("filePath");
                        MarkBitApplication.dummyContent.setDummyContentItem(position, is_empty, control_id, server_id, is_control_synced, is_server_synced, version, bitmap, filePath);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case REQUEST_CODE_EDIT_MARK:
                    Toast.makeText(getContext(), "edit a new mark", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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


    private Bitmap bin2Bitmap(File file) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            bitmap = Bitmap.createBitmap(MarkBitApplication.BIT_LCD_WIDTH, MarkBitApplication.BIT_LCD_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.BLACK);
            byte[] tmp = new byte[MarkBitApplication.BIT_LCD_WIDTH / 8];
            if (inputStream != null) {
                int lines = 0;
                while (-1 != inputStream.read(tmp)) {
                    for (int i = 0; i < MarkBitApplication.BIT_LCD_WIDTH / 8; ++i) {

                        for (int j = 0; j < 8; ++j) {
                            if ((tmp[i] & (1 << j)) != 0) {
                                bitmap.setPixel(lines / 2, 8 * i + j, (lines % 2 == 0) ? Color.RED : Color.YELLOW);
                            }
                        }
                    }
                    lines++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private byte[][] bitmap2Bin(Bitmap bitmap) {

        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            int widthByte = width / 8;
            byte[][] b = new byte[height * 2][widthByte];
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    int color = bitmap.getPixel(j, i);
                    if (color == Color.RED) {
                        b[2 * i][j / 8] |= (1 << (7 - j % 8));
                    }
                    if (color == Color.YELLOW) {
                        b[2 * i + 1][j / 8] |= (1 << (7 - j % 8));
                    }
                }
            }

            return b;
        } else {
            return null;
        }
    }
}
