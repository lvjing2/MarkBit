package com.liwn.zzl.markbit.mark;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Toast;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zzl on 16/8/7.
 */
public class GetBitmap {
    private final static int preSize = 576;
    private final static int width = MarkBitApplication.BIT_LCD_WIDTH;
    private final static int height = MarkBitApplication.BIT_LCD_HEIGHT;
    private final static int imgIconSize = width * height * 2 / 8;
    private final static int imgColor = 2;

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

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);

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

                if (img[i][j] ==  true) {
                    bitmap.setPixel(j, i, Color.RED);
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

                if (img[i][j] ==  true) {
                    bitmap.setPixel(j, i, Color.YELLOW);
                }
            }
        }

        return bitmap;
    }

    public boolean[][] getIconBin(int index, int offset) {

        // System.out.println("index: " + index + ", part: " + part);
        int width = MarkBitApplication.BIT_LCD_WIDTH;
        int height = MarkBitApplication.BIT_LCD_HEIGHT;
        int size = width * height / 8;

        boolean[][] mat = new boolean[height][width];
        try {
            File file = FileIO.getIconFile();
            if (file == null) {
                Toast.makeText(MarkBitApplication.applicationContext, "please import 2 bins file.", Toast.LENGTH_SHORT).show();
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
}
