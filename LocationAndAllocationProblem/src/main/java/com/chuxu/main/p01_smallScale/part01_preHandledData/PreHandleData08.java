package com.chuxu.main.p01_smallScale.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;

import java.text.DecimalFormat;
import java.util.*;

public class PreHandleData08 {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static int K = 18;  //选址点数量
    public static final int candidateNum = 27;
    public static final int communityNum = 42;
    public static final double D = 10.0;  //服务距离上限d
    public static double[][] disMatrix = {
            {INF, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, 3.0, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF},
            {6.0, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, 4.0, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, 2.0, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF},
            {INF, INF, 10.0, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, INF, 5.0, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, 5.0, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, 5.0, INF, 6.0, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF},
            {INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF},
            {INF, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF},
            {INF, INF, 8.0, INF, 5.0, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, 4.0, INF, INF, INF, INF, INF, 6.0, INF, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, 3.0, 5.0, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, 6.0},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, 3.0, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF},
            {5.0, INF, 2.0, INF, INF, INF, INF, 5.0, INF, 8.0, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, 10.0, INF, 4.0, INF, INF, INF, INF, INF, INF},
            {INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, 10.0, INF, INF, INF, INF, 3.0, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, 2.0, INF, 6.0, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, 4.0, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF},
            {INF, INF, 3.0, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, 1.0, INF, INF, 9.0, INF, 6.0, INF, INF, 3.0, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, INF, INF},
            {1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, INF, INF, 1.0, INF, 8.0, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, 8.0, INF, INF, INF, 8.0},
            {INF, INF, INF, 5.0, 7.0, INF, 9.0, INF, INF, 9.0, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF}
    };

    //人口向量
    public static final int[] populationNums = {1991, 611, 1681, 1703, 625, 1654, 1229, 1276, 1755, 684, 570, 1831, 767, 1184, 1211, 633, 1255, 1506, 1103, 1726, 1913, 1241, 1934, 1204, 926, 592, 1517, 1627, 710, 1200, 791, 1603, 821, 1644, 1859, 1438, 1396, 1322, 647, 1420, 1967, 1706};

    //容量向量
    public static final double[] capacities = {3226.0, 2905.0, 2359.0, 3012.0, 2266.0, 3679.0, 3217.0, 3707.0, 2617.0, 3785.0, 3746.0, 3881.0, 2791.0, 3164.0, 2480.0, 3386.0, 2521.0, 3910.0, 2708.0, 3179.0, 3830.0, 3267.0, 3061.0, 2705.0, 3275.0, 2096.0, 3039.0};
//    public static final double[] capacities = {1941.0, 2305.0, 2286.0, 2222.0, 1562.0, 1684.0, 2337.0, 2919.0, 2026.0, 1957.0, 2780.0, 1786.0, 1740.0, 2529.0, 2012.0, 1546.0, 2101.0, 2398.0, 2567.0, 2739.0, 2351.0, 2425.0, 2156.0, 2142.0, 1811.0, 1616.0, 2020.0};

    //备选点和居民点列表
    public static final List<Community> communities = new ArrayList<>();
    public static final List<Candidate> candidates = new ArrayList<>();

    public static void main(String[] args) {
        createEntity();
        for (double[] matrix : disMatrix) {
            System.out.println(Arrays.toString(matrix));
        }
        System.out.println("====================================================");
        System.out.println(Arrays.toString(populationNums));
        System.out.println("====================================================");
        System.out.println(Arrays.toString(capacities));
        System.out.println("====================================================");
        candidates.forEach(System.out::println);
        System.out.println("====================================================");
        communities.forEach(System.out::println);
    }

    //2.预处理全过程
    public static void preHandle() throws Exception {
        createEntity();
    }

    //1.创建实体类
    public static void createEntity() {
        DecimalFormat decimalFormat = new DecimalFormat("#.0000");

        //有哪些东西需要随机生成？
        //①距离矩阵：先生成0-30之间的保留的整数，这样每个备选点会服务到1/3的居民点，然后再处理一下，将<D的保持不变，大于D的变为INF；
        //②居民点的人口数量向量：生成500-2000之间的整数；平均1250*0.7=875*15=13125\10=1312.5==>备选点的最小平均容量；
        //③备选点的容量向量：生成1000-2500之间的随机整数，试试效果

        //①距离矩阵：先生成0-30之间的保留的整数，这样每个备选点会服务到1/3的居民点，然后再处理一下，将<D的保持不变，大于D的变为INF；
        //1/3的比例似乎太大了，改为1/5吧

        //②居民点的人口数量向量：生成500-2000之间的整数；平均1250*0.7=875*15=13125\10=1312.5==>备选点的最小平均容量；
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

        //③备选点的容量向量：生成1000-2500之间的随机整数，试试效果
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
    }
}