package com.liwn.zzl.markbit.mark;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {
    public final String TAG = DummyContent.class.getSimpleName();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<Integer, DummyItem> ITEM_MAP = new HashMap<Integer, DummyItem>();
    public static Map<Integer, DummyItem> ALL_ITEM_MAP = new HashMap<Integer, DummyItem>();

    private static int COUNT = 0;
    private static int ALL_COUNT = 0;


    public DummyContent() {
        File file = FileIO.getIconFile();
        if (file == null) {
            Toast.makeText(MarkBitApplication.applicationContext, "please import 2 bins file.", Toast.LENGTH_SHORT).show();
            return;
        }

        COUNT = FileIO.getByte(MarkBitApplication.i_file, 0x11);
        ALL_COUNT = FileIO.getByte(MarkBitApplication.i_file, 0x10);
        byte[] commonMarkIndex = new byte[COUNT];
        FileIO.getBytes(MarkBitApplication.i_file, commonMarkIndex, 0x20, COUNT);
        Log.d(TAG, "count: " + COUNT);
        for (int i = 0; i < COUNT; i++) {
            int index = commonMarkIndex[i] & 0xff;
            Bitmap bitmap = new GetBitmap().getBitmap(index);

            DummyItem dummyItem = newDummyContentItem(i, index, index, false, false, "v0.0", bitmap, FileIO.default_file_name);
            ITEM_MAP.put(dummyItem.position, dummyItem);
        }

        for (int i = 0; i < ALL_COUNT; i++) {
            Bitmap bitmap = new GetBitmap().getBitmap(i);
            DummyItem dummyItem = newDummyContentItem(i, i, i, false, false, "v0.0", bitmap, FileIO.default_file_name);
            ALL_ITEM_MAP.put(dummyItem.position, dummyItem);
        }

    }

    public static void replaceItem(int old_id, int new_id) {

        DummyItem newItem = ALL_ITEM_MAP.get(new_id);
        ITEM_MAP.remove(old_id);
        ITEM_MAP.put(old_id, newItem);

        int i_offset = (0x20 & 0xff) + old_id;
        int r_offset = (0x40 & 0xff) + old_id;

        Log.e("DummyContent", "i_offset: " + i_offset + "; r_offset: " + r_offset);
        int set_num = 1;
        byte[] value = new byte[set_num];
        value[0] = (byte) newItem.control_id;
        FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
        FileIO.setBytes(MarkBitApplication.r_file, r_offset, set_num, value);
    }

    public DummyItem newDummyContentItem(int position, int control_id, int server_id, boolean is_control_synced,
                                         boolean is_server_synced, String version, Bitmap img, String filePath) {

        String fileName = filePath.split("\\.")[0];
        String[] fileInfo = fileName.split("_");

        String date = fileInfo[2];
        String tag = fileInfo[3];

        return new DummyItem(position, control_id, server_id, is_control_synced, is_server_synced, version, img, date, tag, filePath);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final int position;
        public final int control_id;
        public final int server_id;
        public final boolean is_control_synced;
        public final boolean is_server_synced;
        public final String version;
        public final Bitmap img;
        public final String date;
        public final String tag;
        public final String filePath;

        public DummyItem(int position, int control_id, int server_id, boolean is_control_synced,
                         boolean is_server_synced, String version, Bitmap img, String date, String tag, String filePath) {
            this.position = position;
            this.control_id = control_id;
            this.server_id = server_id;
            this.is_control_synced = is_control_synced;
            this.is_server_synced = is_server_synced;
            this.version = version;
            this.img = img;
            this.date = date;
            this.tag = tag;
            this.filePath = filePath;
        }
    }
}
