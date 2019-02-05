package com.liwn.zzl.markbit.bins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinsDownloadUrl {
    private static final String INDEX_URL = "http://www.ahzhongche.com/upload/attachment/";
    private static final String ICON_NAME = "/icon100.bin";
    private static final String RCON_NAME = "/rcon100.bin";

    // 综合图库
    public static final String TYPE_ZHTK = "zhtk";
    public static final String ZHTK_I_URL = INDEX_URL + TYPE_ZHTK + ICON_NAME;
    public static final String ZHTK_R_URL = INDEX_URL + TYPE_ZHTK + RCON_NAME;

    // 公安消防
    public static final String TYPE_GAXF = "gaxf";
    public static final String GAXF_I_URL = INDEX_URL + TYPE_GAXF + ICON_NAME;
    public static final String GAXF_R_URL = INDEX_URL + TYPE_GAXF + RCON_NAME;

    // 公安交警
    public static final String TYPE_GAJJ = "gajj";
    public static final String GAJJ_I_URL = INDEX_URL + TYPE_GAJJ + ICON_NAME;
    public static final String GAJJ_R_URL = INDEX_URL + TYPE_GAJJ + RCON_NAME;

    // 交通运政
    public static final String TYPE_JTYZ = "jtyz";
    public static final String JTYZ_I_URL = INDEX_URL + TYPE_JTYZ + ICON_NAME;
    public static final String JTYZ_R_URL = INDEX_URL + TYPE_JTYZ + RCON_NAME;

    public static final List<IRPairs> downloadUrls = new ArrayList<>();

    static {
        IRPairs zhtk_pairs = new IRPairs(ZHTK_I_URL, ZHTK_R_URL);
        IRPairs gaxf_pairs = new IRPairs(GAXF_I_URL, GAXF_R_URL);
        IRPairs gajj_pairs = new IRPairs(GAJJ_I_URL, GAJJ_R_URL);
        IRPairs jtyz_pairs = new IRPairs(JTYZ_I_URL, JTYZ_R_URL);

        downloadUrls.add(zhtk_pairs);
        downloadUrls.add(gaxf_pairs);
        downloadUrls.add(gajj_pairs);
        downloadUrls.add(jtyz_pairs);
    }
}
