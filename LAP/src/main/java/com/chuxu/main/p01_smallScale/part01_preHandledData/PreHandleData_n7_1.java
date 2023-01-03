package com.chuxu.main.p01_smallScale.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;

import java.text.DecimalFormat;
import java.util.*;

public class PreHandleData_n7_1 {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static int K = 4;  //选址点数量
    public static final int candidateNum = 7;
    public static final int communityNum = 8;
    public static final double D = 10.0;  //服务距离上限
    public static double[][] disMatrix = {
            {5, 7, 8, INF, INF, INF, INF, INF},
            {3, INF, INF, 9, INF, 6, INF, INF},
            {INF, 4, INF, 5, INF, INF, INF, INF},
            {INF, INF, 10, INF, 6, INF, INF, INF},
            {INF, 10, INF, 7, INF, 2, INF, INF},
            {INF, INF, INF, INF, INF, 9, 7, 6},
            {INF, INF, INF, INF, INF, INF, 8, 7}
    };

    //人口向量
    public static final int[] populationNums = {1000, 1500, 1800, 2000, 1700, 2000, 1200, 800};

    //容量向量
    public static final double[] capacities = {2000, 2400, 2000, 2400, 2000, 3000, 2000};

    //备选点和居民点列表
    public static final List<Community> communities = new ArrayList<>();
    public static final List<Candidate> candidates = new ArrayList<>();

    public static void main(String[] args) {
        createEntity();
        candidates.forEach(System.out::println);
        communities.forEach(System.out::println);
    }

    //2.预处理全过程
    public static void preHandle() throws Exception {
        createEntity();
    }

    //1.创建实体类
    public static void createEntity() {
        for (int i = 0; i < candidateNum; i++) {
            Candidate candidate = new Candidate();
            candidate.setId(i + 1);
            candidate.setWholeCapacity(capacities[i]);
            candidate.setRemainCapacity(capacities[i]);
            Set<Integer> slaveCommunityIds = new LinkedHashSet<>();
            for (int j = 0; j < communityNum; j++) {
                if (disMatrix[i][j] <= D) {
                    slaveCommunityIds.add(j + 1);
                }
            }
            candidate.setSlaveCommunityIds(slaveCommunityIds);
            candidates.add(candidate);
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.0000");
        for (int i = 0; i < communityNum; i++) {
            Community community = new Community();
            community.setId(i + 1);
            community.setPopulationNum(populationNums[i]);
            double wholeNeed = Double.parseDouble(decimalFormat.format(populationNums[i] * need));
            community.setWholeNeed(wholeNeed);
            community.setUnsatisfiedNeed(wholeNeed);
            Set<Integer> dominatedCandidateIds = new LinkedHashSet<>();
            for (int j = 0; j < candidateNum; j++) {
                //注意列不变，行变。行代表备选点，列代表居民点
                if (disMatrix[j][i] <= D) {
                    dominatedCandidateIds.add(j + 1);
                }
            }
            community.setDominatedCandidateIds(dominatedCandidateIds);
            communities.add(community);
        }
    }
}