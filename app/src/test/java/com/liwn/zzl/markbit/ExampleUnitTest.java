package com.liwn.zzl.markbit;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

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
}