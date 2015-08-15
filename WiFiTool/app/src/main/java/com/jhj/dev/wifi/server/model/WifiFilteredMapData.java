package com.jhj.dev.wifi.server.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 江华健
 */
public class WifiFilteredMapData {
    private static WifiFilteredMapData filteredMapData;

    /**
     * 应用上下文
     */
    private Context appContext;

    /**
     * 过滤后的接入点列表
     */
    private List<LinkedList<Integer>> filteredAPList;

    /**
     * 过滤后的接入点MAC地址集
     */
    private List<String> filteredAPBSSIDs;

    /**
     * 过滤后的的接入点名字集
     */
    private List<String> filteredAPNames;

    public WifiFilteredMapData(Context context)
    {
        this.appContext = context;

        filteredAPList = new ArrayList<LinkedList<Integer>>();

        filteredAPBSSIDs = new ArrayList<String>();

        filteredAPNames = new ArrayList<String>();
    }

    public static WifiFilteredMapData getInstance(Context context)
    {
        if (filteredMapData == null) {
            filteredMapData = new WifiFilteredMapData(context.getApplicationContext());
        }

        return filteredMapData;
    }

    /**
     * 通过指定的接入点获取过滤后的接入点MAC地址
     *
     * @param ap 指定的接入点
     * @return 过滤后的接入点MAC地址
     */
    public String getFilteredAPBSSID(LinkedList<Integer> ap)
    {
        try {
            return filteredAPBSSIDs.get(filteredAPList.indexOf(ap));

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过指定接入点获取过滤后的接入点的名字
     *
     * @param ap 指定的接入点
     * @return 过滤后的接入点的名字
     */
    public String getFilteredAPName(LinkedList<Integer> ap)
    {
        return filteredAPNames.get(filteredAPList.indexOf(ap));
    }

    /**
     * 通过接入点索引获取过滤后的接入点
     *
     * @param apIndex 接入点在过滤后的接入点列表里的索引
     * @return 过滤后的接入点
     */
    public LinkedList<Integer> getFilteredAP(int apIndex)
    {

        return filteredAPList.get(apIndex);

    }

    /**
     * 获取过滤后的接入点列表
     *
     * @return 过滤后的接入点列表
     */
    public List<LinkedList<Integer>> getFilteredAPList()
    {
        //System.out.println("FilteredMapData--->getFilteredAPList.size---------->"+filteredAPList.size());

        return filteredAPList;
    }

    /**
     * 保存过滤后的接入点列表
     *
     * @param apList 要保留的接入点列表
     */
    public void setFilteredAPList(List<LinkedList<Integer>> apList)
    {
        filteredAPList.clear();
        for (LinkedList<Integer> ap : apList) {
            filteredAPList.add(ap);
        }
        //System.out.println("FilteredMapData--->setFilteredAPList.size---------->"+filteredAPList.size());
    }

    /**
     * 获取过滤后的接入点MAC地址集
     *
     * @return 过滤后的接入点MAC地址集
     */
    public List<String> getFilteredAPBSSIDs()
    {
        return filteredAPBSSIDs;
    }

    /**
     * 保存过滤后的接入点MAC地址集
     *
     * @param apBSSIDs 要保留的接入点MAC地址集
     */
    public void setFilteredAPBSSIDs(List<String> apBSSIDs)
    {
        filteredAPBSSIDs.clear();
        for (String apBSSID : apBSSIDs) {
            filteredAPBSSIDs.add(apBSSID);
        }
        //System.out.println("FilteredMapData--->setFilteredAPBSSIDs.size---------->"+filteredAPBSSIDs.size());
    }

    /**
     * 保存过滤后的接入点名字集
     *
     * @param apNames 要保留的接入点名字集
     */
    public void setFilteredAPNames(List<String> apNames)
    {
        filteredAPNames.clear();
        for (String apName : apNames) {
            filteredAPNames.add(apName);
        }

        //System.out.println("FilteredMapData--->setFilteredAPNames.size---------->"+filteredAPNames.size());
    }

}
