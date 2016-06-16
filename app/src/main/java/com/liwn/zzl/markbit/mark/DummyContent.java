package com.liwn.zzl.markbit.mark;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * A map of sample (dummy) items, by ID.
     */
    public Map<Integer, DummyItem> ITEM_MAP = new HashMap<Integer, DummyItem>();

    private static final int COUNT = 128;

    public DummyContent() {
        for (int i = 0; i < COUNT; i++) {

            Bitmap img = Bitmap.createBitmap(2*MarkBitApplication.BIT_LCD_WIDTH, 2*MarkBitApplication.BIT_LCD_HEIGHT, Bitmap.Config.ARGB_8888);
            img.eraseColor(Color.BLACK);
            setDummyContentItem(i, i, i, false, false, "v0.0", img, FileIO.default_file_name);
        }

    }

    private void setItem(DummyItem item) {
        ITEM_MAP.put(item.position, item);
    }

    public void setDummyContentItem(int position, int control_id, int server_id, boolean is_control_synced,
                                         boolean is_server_synced, String version, Bitmap img, String filePath) {

        String fileName = filePath.split("\\.")[0];
        String[] fileInfo = fileName.split("_");

        String date = fileInfo[2];
        String tag = fileInfo[3];

        DummyItem dummyItem = new DummyItem(position, control_id, server_id, is_control_synced, is_server_synced, version, img, date, tag, filePath);
        setItem(dummyItem);
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
