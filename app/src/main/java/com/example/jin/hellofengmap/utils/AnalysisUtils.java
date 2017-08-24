package com.example.jin.hellofengmap.utils;

import com.fengmap.android.analysis.search.FMSearchAnalyser;
import com.fengmap.android.analysis.search.FMSearchResult;
import com.fengmap.android.analysis.search.model.FMSearchModelByKeywordRequest;
import com.fengmap.android.analysis.search.model.FMSearchModelByTypeRequest;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.marker.FMModel;

import java.util.ArrayList;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 搜素分析
 */
public class AnalysisUtils {


    /**
     * 通过关键字查询模型
     *
     * @param searchAnalyser 搜素控制
     * @param map            地图
     * @param keyword        关键字
     */
    public static ArrayList<FMModel> queryModelByKeyword(FMMap map, FMSearchAnalyser searchAnalyser,
                                                         String keyword) {
        int[] groupIds = map.getMapGroupIds();
        return queryModelByKeyword(map, groupIds, searchAnalyser, keyword);
    }

    /**
     * 通过关键字查询模型
     *
     * @param searchAnalyser 搜素控制
     * @param map            地图
     * @param keyword        关键字
     * @param groupIds       楼层集合
     */
    public static ArrayList<FMModel> queryModelByKeyword(FMMap map, int[] groupIds, FMSearchAnalyser searchAnalyser,
                                                         String keyword) {
        ArrayList<FMModel> list = new ArrayList<FMModel>();
        FMSearchModelByKeywordRequest request = new FMSearchModelByKeywordRequest(groupIds, keyword);
        ArrayList<FMSearchResult> result = searchAnalyser.executeFMSearchRequest(request);
        for (FMSearchResult r : result) {
            String fid = (String) r.get("FID");
            FMModel model = map.getFMLayerProxy().queryFMModelByFid(fid);
            list.add(model);
        }
        return list;
    }


    /**
     * 通过类型查询模型
     *
     * @param searchAnalyser 搜素控制
     * @param map            地图
     * @param type           类型
     */
    public static ArrayList<FMModel> queryModelByType(FMMap map, FMSearchAnalyser searchAnalyser,
                                                      int type) {
        int[] groupIds = map.getMapGroupIds();
        return queryModelByType(map, groupIds, searchAnalyser, type);
    }

    /**
     * 通过类型查询模型
     *
     * @param searchAnalyser 搜素控制
     * @param map            地图
     * @param type           类型
     * @param groupIds       楼层集合
     */
    public static ArrayList<FMModel> queryModelByType(FMMap map, int[] groupIds, FMSearchAnalyser searchAnalyser,
                                                      int type) {
        ArrayList<FMModel> list = new ArrayList<FMModel>();
        FMSearchModelByTypeRequest request = new FMSearchModelByTypeRequest(groupIds, type);
        ArrayList<FMSearchResult> result = searchAnalyser.executeFMSearchRequest(request);
        for (FMSearchResult r : result) {
            String fid = (String) r.get("FID");
            FMModel model = map.getFMLayerProxy().queryFMModelByFid(fid);
            list.add(model);
        }
        return list;
    }

}
