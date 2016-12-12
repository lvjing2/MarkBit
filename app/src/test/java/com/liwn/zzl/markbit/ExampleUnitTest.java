package com.liwn.zzl.markbit;

import android.util.Log;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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
}