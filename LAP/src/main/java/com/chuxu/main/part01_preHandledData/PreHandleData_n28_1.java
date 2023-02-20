package com.chuxu.main.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;

import java.text.DecimalFormat;
import java.util.*;

public class PreHandleData_n28_1 {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static int K = 18;  //选址点数量
    public static final int candidateNum = 27;
    public static final int communityNum = 42;
    public static final double D = 10.0;  //服务距离上限d
    public static double[][] disMatrix = {
            {5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, 4.0},
            {INF, INF, INF, 2.0, INF, INF, 7.0, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF},
            {INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {8.0, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, 6.0, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, 3.0, INF, 2.0, 5.0, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, 9.0, INF, INF, INF, INF, 9.0},
            {4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, 7.0, INF, INF, INF},
            {INF, INF, INF, INF, 5.0, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, 5.0, INF, INF, 4.0, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, 1.0},
            {INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, 2.0, INF, INF, INF, INF, INF},
            {6.0, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, 7.0, 8.0, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, 1.0, INF, 1.0, INF, INF, INF, INF, INF, INF},
            {INF, INF, 1.0, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, 10.0, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, 9.0, 7.0, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, 1.0, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, 4.0, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, 10.0, INF, 9.0, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, 10.0, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, 9.0, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, 7.0, INF},
            {INF, 5.0, INF, INF, 4.0, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, 1.0, INF, 4.0, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, 9.0, INF, INF, 3.0, INF, INF, INF, INF, INF},
            {4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, 10.0, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, 1.0, INF, INF, INF, INF, INF, 8.0, 10.0, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, 2.0, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, 9.0, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, INF, 9.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, 10.0, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF},
    };

    //人口向量
    public static final int[] populationNums =
            {1507, 1103, 1297, 1627, 1136, 1720, 1650, 1909, 1392, 1640, 1696, 1775, 1473, 1154, 1371, 1109, 1591, 1518, 1942, 1587, 1278, 1648, 1396, 1242, 1146, 1533, 1906, 1125, 1258, 1706, 1516, 1663, 1345, 1587, 1852, 1994, 1180, 1684, 1798, 1510, 1050, 1096};

    //容量向量
    public static final double[] capacities =
            {2955.0, 1569.0, 2265.0, 2535.0, 3271.0, 1923.0, 1651.0, 3290.0, 1633.0, 2683.0, 2411.0, 3483.0, 3397.0, 3437.0, 2303.0, 3166.0, 2778.0, 3050.0, 1724.0, 1765.0, 2291.0, 1994.0, 3168.0, 2823.0, 2580.0, 1604.0, 3281.0, 1600.0};
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