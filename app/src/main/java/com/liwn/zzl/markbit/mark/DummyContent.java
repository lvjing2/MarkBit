package com.liwn.zzl.markbit.mark;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.R;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class DummyContent {
    public final String TAG = DummyContent.class.getSimpleName();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<Integer, DummyItem> ITEM_MAP_A = new HashMap<Integer, DummyItem>();
    public static Map<Integer, DummyItem> ITEM_MAP_B = new HashMap<Integer, DummyItem>();
    public static Map<Integer, DummyItem> ALL_ITEM_MAP = new HashMap<Integer, DummyItem>();

    private static int COUNT = 0;
    private static int ALL_COUNT = 0;

    public DummyContent() {
        File file = FileIO.getIconFile();
        if (file == null) {
            Toast.makeText(MarkBitApplication.applicationContext, R.string.bins_not_import, Toast.LENGTH_SHORT).show();
            return;
        }

        COUNT = FileIO.getByte(MarkBitApplication.i_file, FileIO.A_SAMPLE_NUM_ADDR);
        ALL_COUNT = FileIO.getByte(MarkBitApplication.i_file, FileIO.ALL_SAMPLE_NUM_ADDR);
        byte[] commonMarkIndex_A = new byte[COUNT];
        byte[] commonMarkIndex_B = new byte[COUNT];
        FileIO.getBytes(MarkBitApplication.i_file, commonMarkIndex_A, FileIO.A_INDEX_LIB_ADDR, COUNT);
        FileIO.getBytes(MarkBitApplication.i_file, commonMarkIndex_B, FileIO.B_INDEX_LIB_ADDR, COUNT);

        Log.d(TAG, "count: " + COUNT);

        GetBitmap getBitmap = new GetBitmap();
        for (int i = 0; i < ALL_COUNT; i++) {
            Bitmap bitmap = getBitmap.getBitmap(i);
            DummyItem dummyItem = newDummyContentItem(i, i, i, false, false, "v0.0", bitmap, FileIO.default_file_name, true);
            ALL_ITEM_MAP.put(dummyItem.position, dummyItem);
        }

        for (int i = 0; i < COUNT; i++) {
            int index = commonMarkIndex_A[i] & 0xff;
            Bitmap bitmap = getBitmap.getBitmap(index);
            DummyItem dummyItem = newDummyContentItem(i, index, index, false, false, "v0.0", bitmap, FileIO.default_file_name, true);
            ITEM_MAP_A.put(i, dummyItem);
        }

        for (int i = 0; i < COUNT; i++) {
            int index = commonMarkIndex_B[i] & 0xff;
            Bitmap bitmap = getBitmap.getBitmap(index);
            DummyItem dummyItem = newDummyContentItem(i, index, index, false, false, "v0.0", bitmap, FileIO.default_file_name, false);
            ITEM_MAP_B.put(i, dummyItem);
        }

    }

    public static void updateItem(int num) {
        int size_A = ITEM_MAP_A.size();
        int size_B = ITEM_MAP_B.size();

        if (num == size_A) {

        } else if (num > size_A) {
            byte[] commonMarkIndex_A = new byte[num];
            FileIO.getBytes(MarkBitApplication.i_file, commonMarkIndex_A, FileIO.A_INDEX_LIB_ADDR, num);
            GetBitmap getBitmap = new GetBitmap();
            for (int i = size_A; i < num; i++) {
                int index = commonMarkIndex_A[i] & 0xff;

                Bitmap bitmap = getBitmap.getBitmap(index);
                DummyItem dummyItem = newDummyContentItem(i, index, index, false, false, "v0.0", bitmap, FileIO.default_file_name, true);
                ITEM_MAP_A.put(i, dummyItem);
            }
        } else if (num < size_A) {
            for (int i = num; i < size_A; i++) {
                ITEM_MAP_A.remove(i);
            }
        }

        if (num == size_B) {

        } else if (num > size_B) {
            byte[] commonMarkIndex_B = new byte[num];
            FileIO.getBytes(MarkBitApplication.i_file, commonMarkIndex_B, FileIO.B_INDEX_LIB_ADDR, num);
            GetBitmap getBitmap = new GetBitmap();
            for (int i = size_B; i < num; i++) {
                int index = commonMarkIndex_B[i] & 0xff;

                Bitmap bitmap = getBitmap.getBitmap(index);
                DummyItem dummyItem = newDummyContentItem(i, index, index, false, false, "v0.0", bitmap, FileIO.default_file_name, false);
                ITEM_MAP_B.put(i, dummyItem);
            }
        } else if (num < size_B) {
            for (int i = num; i < size_B; i++) {
                ITEM_MAP_B.remove(i);
            }
        }
    }

    public static void updateAllItem(int num) {
        int size = ALL_ITEM_MAP.size();
        if (num == size) {

        } else if (num > size) {
            GetBitmap getBitmap = new GetBitmap();
            for (int i = size; i < num; i++) {
                Bitmap bitmap = getBitmap.getBitmap(i);
                DummyItem dummyItem = newDummyContentItem(i, i, i, false, false, "v0.0", bitmap, FileIO.default_file_name, true);
                ALL_ITEM_MAP.put(i, dummyItem);
            }
        } else if (num < size) {
            for (int i = num; i < size; i++) {
                ALL_ITEM_MAP.remove(i);
            }
        }
    }

    public static void replaceItemContent(boolean type, int old_position_id, int old_control_id, int new_position_id, int new_control_id) {
        Log.e("DummyContent", String.valueOf(type) + "; old_position_id: " + old_position_id + ", new_position_id: " + new_position_id
                +"; old_control_id: " + old_control_id + ", new_control_id: " + new_control_id);

        if (type) {
            Bitmap bitmap = new GetBitmap().getBitmap(new_control_id);

            for (Map.Entry entry : ITEM_MAP_A.entrySet()) {
                DummyItem item = (DummyItem) entry.getValue();
                if (item.control_id == new_control_id) {
                    int temp_id = item.position;
                    DummyItem dummyItem = newDummyContentItem(item.position, new_control_id, new_control_id, false, false, "v0.0", bitmap, FileIO.default_file_name, type);
                    ITEM_MAP_A.put(temp_id, dummyItem);
                }
            }

            // set to replaced item.
            int i_offset = (FileIO.A_INDEX_LIB_ADDR & 0xff) + old_position_id;
//            int r_offset = (FileIO.B_INDEX_LIB_ADDR & 0xff) + old_id;

            int set_num = 1;
            byte[] value = new byte[set_num];
            value[0] = (byte) new_control_id;
            FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
//            FileIO.setBytes(MarkBitApplication.r_file, r_offset, set_num, value);
        } else {
            Bitmap bitmap = new GetBitmap().getBitmap(new_control_id);
            for (Map.Entry entry : ITEM_MAP_B.entrySet()) {
                DummyItem item = (DummyItem) entry.getValue();
                if (item.control_id == new_control_id) {
                    int temp_id = item.position;
                    DummyItem dummyItem = newDummyContentItem(item.position, new_control_id, new_control_id, false, false, "v0.0", bitmap, FileIO.default_file_name, type);
                    ITEM_MAP_B.put(temp_id, dummyItem);
                }
            }

//            int i_offset = (FileIO.A_INDEX_LIB_ADDR & 0xff) + old_id;
            int r_offset = (FileIO.B_INDEX_LIB_ADDR & 0xff) + old_position_id;

            int set_num = 1;
            byte[] value = new byte[set_num];
            value[0] = (byte) new_control_id;
//            FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
            FileIO.setBytes(MarkBitApplication.i_file, r_offset, set_num, value);
        }
    }

    public static void replaceItem(boolean type, int old_position_id, int old_control_id, int new_position_id, int new_control_id) {
        Log.e("DummyContent", String.valueOf(type) + "; old_position_id: " + old_position_id + ", new_position_id: " + new_position_id
        +"; old_control_id: " + old_control_id + ", new_control_id: " + new_control_id);

        if (type) {
            Bitmap bitmap = new GetBitmap().getBitmap(new_control_id);

            DummyItem dummyItem = newDummyContentItem(old_position_id, new_control_id, new_control_id, false, false, "v0.0", bitmap, FileIO.default_file_name, type);

            ITEM_MAP_A.remove(old_position_id);
            ITEM_MAP_A.put(old_position_id, dummyItem);

            // set to replaced item.
            int i_offset = (FileIO.A_INDEX_LIB_ADDR & 0xff) + old_position_id;
//            int r_offset = (FileIO.B_INDEX_LIB_ADDR & 0xff) + old_id;

            int set_num = 1;
            byte[] value = new byte[set_num];
            value[0] = (byte) new_control_id;
            FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
//            FileIO.setBytes(MarkBitApplication.r_file, r_offset, set_num, value);
        } else {
            Bitmap bitmap = new GetBitmap().getBitmap(new_control_id);
            DummyItem dummyItem = newDummyContentItem(old_position_id, new_control_id, new_control_id, false, false, "v0.0", bitmap, FileIO.default_file_name, type);

            ITEM_MAP_B.remove(old_position_id);
            ITEM_MAP_B.put(old_position_id, dummyItem);

//            int i_offset = (FileIO.A_INDEX_LIB_ADDR & 0xff) + old_id;
            int r_offset = (FileIO.B_INDEX_LIB_ADDR & 0xff) + old_position_id;

            int set_num = 1;
            byte[] value = new byte[set_num];
            value[0] = (byte) new_control_id;
//            FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
            FileIO.setBytes(MarkBitApplication.i_file, r_offset, set_num, value);
        }
    }

    public static void modifyItem(boolean type, int old_id, int new_id) {
        Log.e("DummyContent", String.valueOf(type) + "; old_id: " + old_id + "; new_id: " + new_id);

        if (type) {
            Bitmap bitmap = new GetBitmap().getBitmap(new_id);
            DummyItem dummyItem = newDummyContentItem(old_id, new_id, new_id, false, false, "v0.0", bitmap, FileIO.default_file_name, type);

            ITEM_MAP_A.remove(old_id);
            ITEM_MAP_A.put(old_id, dummyItem);

            int i_offset = (FileIO.A_INDEX_LIB_ADDR & 0xff) + old_id;
//            int r_offset = (FileIO.B_INDEX_LIB_ADDR & 0xff) + old_id;

            int set_num = 1;
            byte[] value = new byte[set_num];
            value[0] = (byte) new_id;
            FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
//            FileIO.setBytes(MarkBitApplication.r_file, r_offset, set_num, value);
        } else {
            Bitmap bitmap = new GetBitmap().getBitmap(new_id);
            DummyItem dummyItem = newDummyContentItem(old_id, new_id, new_id, false, false, "v0.0", bitmap, FileIO.default_file_name, type);

            ITEM_MAP_B.remove(old_id);
            ITEM_MAP_B.put(old_id, dummyItem);

//            int i_offset = (FileIO.A_INDEX_LIB_ADDR & 0xff) + old_id;
            int r_offset = (FileIO.B_INDEX_LIB_ADDR & 0xff) + old_id;

            int set_num = 1;
            byte[] value = new byte[set_num];
            value[0] = (byte) new_id;
//            FileIO.setBytes(MarkBitApplication.i_file, i_offset, set_num, value);
            FileIO.setBytes(MarkBitApplication.i_file, r_offset, set_num, value);
        }
    }

    public static DummyItem newDummyContentItem(int position, int control_id, int server_id, boolean is_control_synced,
                                         boolean is_server_synced, String version, Bitmap img, String filePath, boolean type) {

        String fileName = filePath.split("\\.")[0];
        String[] fileInfo = fileName.split("_");

        String date = fileInfo[2];
        String tag = fileInfo[3];

        return new DummyItem(position, control_id, server_id, is_control_synced, is_server_synced, version, img, date, tag, filePath, type);
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
        public final boolean type;

        public DummyItem(int position, int control_id, int server_id, boolean is_control_synced,
                         boolean is_server_synced, String version, Bitmap img, String date, String tag, String filePath, boolean type) {
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
            this.type = type;
        }
    }
}
