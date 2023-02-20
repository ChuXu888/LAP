package com.chuxu.main.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;

import java.text.DecimalFormat;
import java.util.*;

public class PreHandleData_n24_2 {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static int K = 16;  //选址点数量
    public static final int candidateNum = 24;
    public static final int communityNum = 36;
    public static final double D = 10.0;  //服务距离上限d
    public static double[][] disMatrix = {
            {INF, 6.0, INF, INF, INF, INF, INF, 4.0, 2.0, INF, INF, INF, 4.0, INF, 9.0, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 3.0, 4.0, 8.0, INF, INF, INF},
            {5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, 5.0, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, 8.0, 7.0, INF, INF, INF},
            {INF, INF, INF, 7.0, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, 10.0, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, 10.0, INF, INF, 5.0, 7.0, INF, 9.0, INF, 3.0, 5.0, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF},
            {2.0, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, 3.0, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF},
            {INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, 9.0, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, 7.0, 3.0, INF, INF, INF, INF, INF, INF, INF, 5.0},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, 1.0, 4.0, INF, INF, INF, INF, 2.0, INF, INF, 7.0},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, 3.0, INF, 5.0, INF, INF, INF, 6.0, INF, INF, 7.0, INF},
            {8.0, INF, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, 10.0, INF, INF, INF, INF, INF, 7.0, INF, INF, INF, INF},
            {INF, 1.0, INF, INF, INF, INF, 9.0, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0},
            {4.0, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, 7.0, INF, INF, INF, 3.0, INF, 3.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {2.0, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, 2.0, INF, INF, INF, 9.0, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, 2.0, 2.0, INF, 1.0, INF, INF, 7.0, INF, INF, INF, 8.0, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, 3.0, INF, INF, INF, 1.0, INF, INF, 3.0, INF, 2.0, INF, INF, INF, INF, INF, 4.0, INF, INF, INF, INF, INF, 9.0, INF, 5.0, 1.0, INF, 4.0, INF},
            {INF, 7.0, INF, INF, INF, 7.0, INF, INF, INF, 1.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, 4.0, 10.0, INF, INF, INF, INF, INF, 7.0, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 2.0, INF, INF, INF, INF, INF, INF, INF, 1.0, INF, INF, 1.0, 7.0, INF, INF, INF, INF, INF, INF, INF, 3.0, INF},
            {INF, INF, INF, INF, INF, INF, 10.0, INF, 10.0, 2.0, INF, 5.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 6.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 5.0, INF, INF, INF, INF, INF},
            {INF, INF, INF, INF, INF, INF, INF, 8.0, INF, INF, INF, INF, INF, INF, INF, INF, INF, 10.0, INF, INF, INF, INF, INF, INF, INF, 8.0, INF, 8.0, INF, 1.0, INF, INF, INF, INF, INF, INF},
    };

    //人口向量
    public static final int[] populationNums =
            {1748, 1702, 1216, 1578, 1161, 1054, 1062, 1205, 1516, 1642, 1746, 1571, 1411, 1568, 1557, 1552, 1952, 1368, 1250, 1231, 1593, 1199, 1380, 1221, 1864, 1782, 1720, 1853, 1870, 1263, 1207, 1230, 1800, 1220, 1412, 1773};

    //容量向量
    public static final double[] capacities =
            {2591.0, 1576.0, 1656.0, 3028.0, 1567.0, 2678.0, 2799.0, 2301.0, 3531.0, 2343.0, 2774.0, 3079.0, 2221.0, 3328.0, 3918.0, 2864.0, 2661.0, 1666.0, 1929.0, 2770.0, 1501.0, 3235.0, 1710.0, 2503.0};

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