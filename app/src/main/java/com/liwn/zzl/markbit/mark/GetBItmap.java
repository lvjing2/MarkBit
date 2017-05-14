package com.liwn.zzl.markbit.mark;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zzl on 16/8/7.
 */
public class GetBitmap {
    private final static int preSize = 576;
//    private final static int outer_circle_width = 77;
//    private final static int outer_circle_height = 77;
//    private final static int inner_circle_width = 60;
//    private final static int inner_circle_height = 60;
    private final static int outer_circle_width = 77;
    private final static int outer_circle_height = 77;
    private final static int inner_circle_width = 60;
    private final static int inner_circle_height = 60;
    private final static int width = MarkBitApplication.BIT_LCD_WIDTH;
    private final static int height = MarkBitApplication.BIT_LCD_HEIGHT;
    private final static int imgIconSize = width * height * 2 / 8;
    private final static int imgColor = 2;
    private final static int mark_yellow = Color.parseColor("#EE9A49");
    private final static int mark_circle_red = Color.parseColor("#FF4500");
    private final static int mark_red = Color.parseColor("#FF4500");
    private final Bitmap mark_background;

    private char[] key = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};


    // generate the outter circle
    public GetBitmap() {
        super();
        mark_background = Bitmap.createBitmap(outer_circle_width, outer_circle_height, Bitmap.Config.ARGB_8888);
        mark_background.eraseColor(Color.TRANSPARENT);
        int outer_radis = (int) Math.floor(outer_circle_width / 2);
        int inner_radis = (int) Math.floor(inner_circle_width / 2);
        for (int i = 0; i < outer_circle_height; ++i) {
            int outer_left = -1, outer_right = -1, inner_left = -1, inner_right = -1;
            Double temp = Math.sqrt(outer_radis * outer_radis - (i - outer_radis) * (i - outer_radis));
            int deta = (int) Math.ceil(temp);
            outer_left = outer_radis - deta;
            outer_right = outer_radis + deta;
            if (outer_left < 0) {
                outer_left = 0;
            }
            if (outer_right > outer_circle_width) {
                outer_right = outer_circle_width;
            }

            // no inner junction
            if (i < outer_radis - inner_radis || i > outer_radis + inner_radis) {
                for (int j = outer_left; j < outer_right; ++j) {
                    mark_background.setPixel(i, j, mark_circle_red);
                }
            } else {
                temp = Math.sqrt(inner_radis * inner_radis - (i - outer_radis) * (i - outer_radis));
                deta = (int) Math.ceil(temp);
                inner_left = outer_radis - deta;
                inner_right = outer_radis + deta;
                if (inner_left < outer_radis - inner_radis) {
                    inner_left = outer_radis - inner_radis;
                }
                if (inner_right > outer_radis + inner_radis) {
                    inner_right = outer_radis + inner_radis;
                }

                for (int j = outer_left; j <= inner_left; ++j) {
                    mark_background.setPixel(i, j, mark_circle_red);
                }
                for (int j = inner_right; j <= outer_right; ++j) {
                    mark_background.setPixel(i, j, mark_circle_red);
                }
            }

        }
    }


    public Bitmap getBitmap(int index) {
        return getOriBitmap(index);
    }

    public Bitmap getOriBitmap(int index) {
        boolean[][] mat0 = getIconBin(index, 0);
        boolean[][] mat1 = getIconBin(index, 1);

        if (mat0 == null || mat1 == null) {
            Bitmap img = Bitmap.createBitmap(2*MarkBitApplication.BIT_LCD_WIDTH, 2*MarkBitApplication.BIT_LCD_HEIGHT, Bitmap.Config.ARGB_8888);
            img.eraseColor(Color.TRANSPARENT);
            return img;
        }
        int width = mat0[0].length;
        int height = mat0.length;
        int bytes_per_line = width / 8;

//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bitmap.eraseColor(Color.TRANSPARENT);
        Bitmap bitmap = mark_background.copy(Bitmap.Config.ARGB_8888, true);
        int start_width = (outer_circle_width - width) / 2;
        int start_height = (outer_circle_height - height) / 2;

        boolean[][] img = new boolean[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i < height / 2 && j < width / 2) {
                    img[i][j] = mat0[j][width - i - 1];
                } else if (i < height / 2 && j >= width / 2) {
                    img[i][j] = mat0[j - width / 2][width/2 - i - 1];
                } else if (i >= height / 2 && j < width / 2) {
                    img[i][j] = mat0[width/2 + j][height/2 + width/2 - i - 1];
                } else if (i >= height / 2 && j >= width / 2) {
                    img[i][j] = mat0[j][width + height/2 - i - 1];
                }

                if (img[i][j]) {
                    bitmap.setPixel(j + start_width, i + start_height, mark_red);
                }
            }
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i < height / 2 && j < width / 2) {
                    img[i][j] = mat1[j][width - i - 1];
                } else if (i < height / 2 && j >= width / 2) {
                    img[i][j] = mat1[j - width / 2][width/2 - i - 1];
                } else if (i >= height / 2 && j < width / 2) {
                    img[i][j] = mat1[width/2 + j][height/2 + width/2 - i - 1];
                } else if (i >= height / 2 && j >= width / 2) {
                    img[i][j] = mat1[j][width + height/2 - i - 1];
                }

                if (img[i][j]) {
                    bitmap.setPixel(j + start_width, i + start_height, mark_yellow);
                }
            }
        }

        return bitmap;
    }

    // get mat from fonts, and then convert it to bitmap
    public Bitmap getBitmap(boolean[][] mat, boolean color) {

        if (mat == null) {
            return mark_background;
        }
        int width = mat[0].length;
        int height = mat.length;
        int bytes_per_line = width / 8;

        Bitmap bitmap = mark_background.copy(Bitmap.Config.ARGB_8888, true);
        int start_width = (outer_circle_width - width) / 2;
        int start_height = (outer_circle_height - height) / 2;

        if (color) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (mat[i][j]) {
                        bitmap.setPixel(j + start_width, i + start_height, mark_red);
                    }
                }
            }
        } else {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (mat[i][j]) {
                        bitmap.setPixel(j + start_width, i + start_height, mark_yellow);
                    }
                }
            }
        }

        return bitmap;
    }

    // get bit from bin files
    public boolean[][] getIconBin(int index, int offset) {

        // System.out.println("index: " + index + ", part: " + part);
        int width = MarkBitApplication.BIT_LCD_WIDTH;
        int height = MarkBitApplication.BIT_LCD_HEIGHT;
        int size = width * height / 8;

        boolean[][] mat = new boolean[height][width];
        try {
            File file = FileIO.getIconFile();
            if (file == null) {
                Toast.makeText(MarkBitApplication.applicationContext, R.string.bins_not_import, Toast.LENGTH_SHORT).show();
                return null;
            }

            RandomAccessFile raFile = new RandomAccessFile(file, "rw");

            if (raFile != null) {
                byte[] bytes = new byte[size];
                raFile.seek(index * imgIconSize + offset * size + preSize);
                raFile.read(bytes, 0, size);

                int bytes_per_line = width / 8;
                for (int i = 0; i < height; ++i) {
                    // for (int j = 0; j < bytes_per_line; ++j) {
                    // System.out.println(bytesToHexString(bytes, bytes_per_line * i, bytes_per_line));
                    for (int j = 0; j < bytes_per_line; ++j) {
                        for (int k = 0; k < 8; ++k) {
                            if (((bytes[bytes_per_line * i + j] >> (8-k-1)) & 0x01) == 1) {
                                // System.out.print("+");
                                mat[i][j*8 + k] = true;
                            } else {
                                // System.out.print("o");
                                mat[i][j*8 + k] = false;
                            }
                        }
                    }
                    // System.out.println();
                    // }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mat;
    }

    @Nullable
    public static boolean saveBitMatrix(@NonNull boolean[][] mat, int offset, int index) {

        File icon = FileIO.getIconFile();
        File rcon = FileIO.getRconFile();

        int byte_size = 8;
        int height = mat.length;
        int width = mat[0].length;
        int buf_size = 2 * width * height / byte_size;
        if (width != MarkBitApplication.BIT_LCD_WIDTH || height != MarkBitApplication.BIT_LCD_HEIGHT) {
//            Log.e("save bit mat error", "size is not valid.");
            System.out.println("save bit mat error" + "size is not valid.");
            return false;
        }
        byte[] buf = new byte[buf_size];

//        if (i < height / 2 && j < width / 2) {
//            img[i][j] = mat1[j][width - i - 1];
//        } else if (i < height / 2 && j >= width / 2) {
//            img[i][j] = mat1[j - width / 2][width/2 - i - 1];
//        } else if (i >= height / 2 && j < width / 2) {
//            img[i][j] = mat1[width/2 + j][height/2 + width/2 - i - 1];
//        } else if (i >= height / 2 && j >= width / 2) {
//            img[i][j] = mat1[j][width + height/2 - i - 1];
//        }

        int start = offset * buf_size / 2;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int byte_index = (i * width + j) / 8;
                int byte_offset = (i * width + j) % 8;

                boolean flag = false;
                if (i < height / 2 && j < width / 2) {
                    flag = mat[height / 2 - j][width / 2 + i];
                } else if (i < height / 2 && j >= width / 2) {
                    flag = mat[width - 1 - j][i];
                } else if (i >= height / 2 && j < width / 2) {
                    flag = mat[width/2 + height / 2 - j - 1][i - height/2];
                } else if (i >= height / 2 && j >= width / 2) {
                    flag = mat[3 * height / 2 - j - 1][i];
                }

                if (flag) {
                    buf[start + byte_index] |= 0x80 >> byte_offset;
                }
            }
        }

        // no need to add the offset of color
        int write_offset = index * imgIconSize + preSize;
        FileIO.setBytes(icon, write_offset, buf.length, buf);
        // TODO: set bytes to rcon
//        FileIO.setBytes(rcon, index * buf_size, buf.length, buf);

        return true;
    }
}
