package com.liwn.zzl.markbit.mark;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.R;

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
            setDummyContentItem(i, true, i, i, false, false, "v0.0", img, FileIO.default_file_name);
        }

    }

    private void setItem(DummyItem item) {
        ITEM_MAP.put(item.position, item);
    }

    public void setDummyContentItem(int position, boolean is_empty, int control_id, int server_id, boolean is_control_synced,
                                         boolean is_server_synced, String version, Bitmap img, String filePath) {

        String fileName = filePath.split("\\.")[0];
        String[] fileInfo = fileName.split("_");

        String date = fileInfo[2];
        String tag = fileInfo[3];

        DummyItem dummyItem = new DummyItem(position, is_empty, control_id, server_id, is_control_synced, is_server_synced, version, img, date, tag, filePath);
        setItem(dummyItem);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {

        private final int position;
        private boolean is_empty;
        private int control_id;
        private int server_id;
        private boolean is_control_synced;
        private boolean is_server_synced;
        private String version;
        private Bitmap img;
        private String date;
        private String tag;
        private String filePath;

        public DummyItem(int position, boolean is_empty, int control_id, int server_id, boolean is_control_synced,
                         boolean is_server_synced, String version, Bitmap img, String date, String tag, String filePath) {
            this.position = position;
            this.is_empty = is_empty;
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

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public int getPosition() {
            return position;
        }

//        public int setPosition(int position) {
//            this.position = position;
//        }

        public boolean isEmpty() {
            return is_empty;
        }

        public void setEmpty(boolean is_empty) {
            this.is_empty = is_empty;
        }

        public int getControlId() {
            return control_id;
        }

        public void setControlId(int control_id) {
            this.control_id = control_id;
        }

        public int getServerId() {
            return server_id;
        }

        public void setServerId(int server_id) {
            this.server_id = server_id;
        }

        public boolean isControlSynced() {
            return is_control_synced;
        }

        public void setControlSynced(boolean is_control_synced) {
            this.is_control_synced = is_control_synced;
        }

        public boolean isServerSynced() {
            return is_server_synced;
        }

        public void setServerSynced(boolean is_server_synced) {
            this.is_server_synced = is_server_synced;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Bitmap getImg() {
            return img;
        }

        public void setImg(Bitmap img) {
            this.img = img;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
