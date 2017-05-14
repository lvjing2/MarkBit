package com.liwn.zzl.markbit;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.liwn.zzl.markbit.font.Num2Mat;
import com.liwn.zzl.markbit.mark.GetBitmap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private final static int width = MarkBitApplication.BIT_LCD_WIDTH;
    private final static int height = MarkBitApplication.BIT_LCD_HEIGHT;
    private final static int imgIconSize = width * height * 2 / 8;
    private final static int preSize = 576;

    @Test
    public void isSameFileTest() throws Exception {
        File f1 = new File("/storage/sdcard0/MarkBit/icon100.bin");
        File f2 = new File("/storage/emulated/0/MarkBit/icon100.bin");
        Assert.assertTrue(FileIO.isSameFile(f1, f2));
    }

    private String getUrl(Document doc) {
        Elements elements = doc.getElementsByClass("li_text");
        for (Element e : elements) {
            String content = e.child(1).text();
            if (content.startsWith("icon")) {
                return e.child(1).child(0).attr("href");
            }
        }
        return null;
    }

    @Test
    public void testGetUrl() throws Exception {
        Document doc = Jsoup.connect(MarkBitApplication.DOWNLOAD_INDEX).get();
        String url = getUrl(doc);
        if (url != null) {
            System.out.println(url);
            return;
        }

        // if not found at the first page, then scan the pages one by one
        Elements pageWraps = doc.getElementsByClass("pages");
        List<String> pageList = new ArrayList<>();
        for (Element pageWarp : pageWraps) {
            Elements pages = pageWarp.getElementsByTag("a");
            for (Element page : pages) {
                if (page.text().matches("\\d+")) {
                    pageList.add(MarkBitApplication.WEB_INDEX + page.attr("href"));
                    System.out.println(page.attr("href"));
                }
            }
        }

        for (String s : pageList) {
            Document doci = Jsoup.connect(s).get();
            String urli = getUrl(doci);
            if (urli != null) {
                System.out.println(urli);
                return;
            }
        }
        System.out.println("");
    }

    @Test
    public void testMatch() {
        List<String> strings = Arrays.asList("1", "2", "23", "s2");
        for (String s : strings) {
            if (s.matches("\\d+")) {
                System.out.println(s);
            }
        }
    }

    @Test
    public void getMat() {
        boolean[][] mat = getOriBitmap(2);
        printMat(mat);
    }

    private void printMat(boolean[][] mat) {
        for (int i = 0; i < mat.length; ++i) {
            System.out.print("{");
            for (int j = 0; j < mat[0].length; ++j) {
                System.out.print((mat[i][j] ? "+" : "o"));
                if (j == mat[0].length - 1) {
                    System.out.print("}");
                }
                System.out.print(" ");
            }
            System.out.println();
        }


    }

    private boolean[][] getOriBitmap(int index) {
        boolean[][] mat0 = getIconBin(index, 0);
        boolean[][] mat1 = getIconBin(index, 1);

        int width = mat0[0].length;
        int height = mat0.length;

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
            }
        }

        return img;
    }

    // get bit from bin files
    public boolean[][] getIconBin(int index, int offset) {

        // System.out.println("index: " + index + ", part: " + part);
        int width = MarkBitApplication.BIT_LCD_WIDTH;
        int height = MarkBitApplication.BIT_LCD_HEIGHT;
        int size = width * height / 8;

        boolean[][] mat = new boolean[height][width];
        try {
//            File file = FileIO.getIconFile();
            File file = new File("/Users/zzl/Documents/android/MarkBit/app/src/main/assets/bins/icon100.bin");
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


    @Test
    public void testFont() {
            try {
                int sizeof_byte = 8;
                int font_size = 48;
                int font_width = font_size;
                int font_height = font_size;
                int size_step = font_width * font_height / sizeof_byte;
                int offset_step = font_width * font_height / sizeof_byte;

                byte[] incode = String.valueOf("慢").getBytes("GB2312");
                int t1 = (incode[0] & 0xff);
                int t2 = (incode[1] & 0xff);
                // TODO: need to calculate the offset carefully
                int offset = ((t1 - 0xb0) * 94 + (t2 - 0xa1)) * offset_step;

                // calculate offset for different size font
//			if (t1 > 0xa0) {
//				offset = ((t1 - 0xa1) * 94 + (t2 - 0xa1)) * offset_step;
//			} else {
//				offset = (t1 + 156 - 1) * offset_step;
//			}

                byte[] cbuf = new byte[offset_step];
                InputStream inputStream = new FileInputStream(new File("/Users/zzl/Documents/android/MarkBit/app/src/main/assets/fonts/HZK" + font_size));
                inputStream.skip(offset);
                if (inputStream.read(cbuf, 0, offset_step) < 0) {
                    System.out.println("read failed!");
                    inputStream.close();
                    return;
                }


                boolean[][] mat = getMat(cbuf, font_size);
                printMat(mat);
                Bitmap bitmap = new GetBitmap().getBitmap(mat, false);

                System.out.println("============================");

                byte[] buf_1 = saveBitMatrix(mat, 0, 0);
                printMat(getMat(buf_1, font_size));

                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public boolean[][] getMat(byte[] cbuf, int font_size) {
        int font_height = font_size;
        int font_width = font_size;
        int size_step = 8;
        int sizeof_byte = font_width * font_height / size_step;
        char[] key = {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
        boolean[][] mat = null;
        if (font_size == 12 || font_size == 16 || font_size == 24 || font_size == 32) {
//			mat = new byte[offset_step];
            mat = new boolean[font_height][font_width];
            // ����ȡ��
            for (int i = 0; i < font_size; i++) {
                for (int j = 0; j < font_size; j++) {
                    int index = j * font_width + i;
                    int flag = (cbuf[index / size_step] & key[index % size_step]);
                    mat[i][j] = flag > 0 ? true : false;
                }
            }
        } else if (font_size == 40 || font_size == 48) {
            // mat = new byte[offset_step];
            mat = new boolean[font_height][font_width];
            // ����ȡ��
            for (int i = 0; i < font_height; i++) {
                for (int j = 0; j < font_width; j++) {
                    int index = i * font_width + j;
                    int flag = (cbuf[index / size_step] & key[index % size_step]);
                    mat[i][j] = flag > 0 ? true : false;
                }
            }
        }

        return mat;
    }

    @Test
    public void testSaveMat() {

    }

    private static byte[] saveBitMatrix(boolean[][] mat, int offset, int index) {

        File icon = new File("/Users/zzl/Documents/android/MarkBit/app/src/main/assets/bins/icon100.bin");
        File rcon = new File("/Users/zzl/Documents/android/MarkBit/app/src/main/assets/bins/rcon100.bin");

        int byte_size = 8;
        int height = mat.length;
        int width = mat[0].length;
        int buf_size = 2 * width * height / byte_size;
        if (width != MarkBitApplication.BIT_LCD_WIDTH || height != MarkBitApplication.BIT_LCD_HEIGHT) {
//            Log.e("save bit mat error", "size is not valid.");
            System.out.println("save bit mat error" + "size is not valid.");
            return null;
        }
        byte[] buf = new byte[buf_size];

        boolean[][] covertedMap = new boolean[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (i < height / 2 && j < width / 2) {
                    covertedMap[i][j] = mat[j][width - i - 1];
                } else if (i < height / 2 && j >= width / 2) {
                    covertedMap[i][j] = mat[j - width / 2][width/2 - i - 1];
                } else if (i >= height / 2 && j < width / 2) {
                    covertedMap[i][j] = mat[width/2 + j][height/2 + width/2 - i - 1];
                } else if (i >= height / 2 && j >= width / 2) {
                    covertedMap[i][j] = mat[j][width + height/2 - i - 1];
                }
            }
        }

        int start = offset * buf_size / 2;
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int byte_index = (i * width + j) / 8;
                int byte_offset = (i * width + j) % 8;
                if (mat[i][j]) {
                    buf[start + byte_index] |= 0x80 >> byte_offset;
                }
            }
        }

        return buf;
    }
}