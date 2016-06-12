package com.liwn.zzl.markbit.dummy;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.liwn.zzl.markbit.MarkBitApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Integer, DummyItem> ITEM_MAP = new HashMap<Integer, DummyItem>();

    private static final int COUNT = 6;

    static {
        // Add some sample items.

        for (int i = 1; i <= COUNT; i++) {

            Bitmap img = Bitmap.createBitmap(2*MarkBitApplication.BIT_LCD_WIDTH, 2*MarkBitApplication.BIT_LCD_HEIGHT, Bitmap.Config.ARGB_8888);
            img.eraseColor(Color.RED);
            addItem(createDummyItem(i, i, i, "v1.0", img, "Item " + i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.position, item);
    }

    private static DummyItem createDummyItem(int position, int control_id, int server_id, String version, Bitmap img, String name) {
        return new DummyItem(position, control_id, server_id, version, img, name);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final int position;
        public final int control_id;
        public final int server_id;
        public final String version;
        public final Bitmap img;
        public final String name;

        public DummyItem(int position, int control_id, int server_id, String version, Bitmap img, String name) {
            this.position = position;
            this.control_id = control_id;
            this.server_id = server_id;
            this.version = version;
            this.img = img;
            this.name = name;
        }
    }
}
