package com.chuxu.main.p01_smallScale.part02_properties;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;
import com.chuxu.entity.ServeCouple;
import com.chuxu.main.p01_smallScale.MainAlgorithm;
import com.chuxu.main.p01_smallScale.part05_lowerBoundAlgorithm.LowerBoundAlgorithm;

import java.math.BigDecimal;
import java.util.*;

public class Properties {

    //性质2-补
    public static boolean property02_c(List<Candidate> F1_Add_FF1) {
        return F1_Add_FF1.size() <= MainAlgorithm.K;
    }

    //性质2
    public static boolean property02(List<Candidate> F_Minus_F0_Minus_FF0, List<Community> EE5) {
        //存储孤立居民点及其对应的备选点的键值对
        //遍历各居民点E\E1和备选点集合F\F0，寻找孤立的备选点
        for (Community community : EE5) {
            //计算各备选点在F\F0中的邻接点个数
            int curCount = 0;
            for (Candidate candidate : F_Minus_F0_Minus_FF0) {
                //如果当前居民点的邻接备选点id包含了F\F0中当前遍历到的备选点的id，curCount就加1
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    curCount++;
                }
            }
            //内层循环结束后，如果curCount=0，说明该居民点是一个孤立点，此时无解
            if (curCount == 0) {
                System.out.println("community = " + community);
                return false;
            }
        }
        return true;
    }

    //性质3
    public static boolean property03(List<Candidate> F_Minus_F0_Minus_FF0, List<Community> EE5) {
        double wholeRemainCapacity = 0.0;
        double wholeUnsatisfiedNeed = 0.0;
        for (Candidate candidate : F_Minus_F0_Minus_FF0) {
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(wholeRemainCapacity));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(candidate.getRemainCapacity()));
            wholeRemainCapacity = bigDecimal_1.add(bigDecimal_2).doubleValue();
        }
        for (Community community : EE5) {
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(wholeUnsatisfiedNeed));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
            wholeUnsatisfiedNeed = bigDecimal_1.add(bigDecimal_2).doubleValue();
        }
        return wholeRemainCapacity >= wholeUnsatisfiedNeed;
    }

    //性质4
    public static LinkedHashMap<Community, Candidate> property04(List<Candidate> F_Minus_F0, List<Community> E_Minus_E1) {
        //存储孤立居民点及其对应的备选点的键值对
        LinkedHashMap<Community, Candidate> singleCommunityCandidate = new LinkedHashMap<>();
        //遍历各居民点E\E1和备选点集合F\F0，寻找孤立的备选点
        for (Community community : E_Minus_E1) {
            //计算各备选点在F\F0中的邻接点个数
            int curCount = 0;
            List<Candidate> correspondingCandidates = new ArrayList<>();
            for (Candidate candidate : F_Minus_F0) {
                //如果当前居民点的邻接备选点id包含了F\F0中当前遍历到的备选点的id，curCount就加1
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    //如果当前备选点的剩余容量足以容纳当前居民点的剩余需求量
                    //前面的要用剩余容量，因为可能多个单度居民点指向同一个备选点，后面的就用全部需求量，因为该居民点没有其它选择了
                    //但是又涉及到了一个更新的问题呀
                    //==>此处【不进行容量判断】，回到上界子算法中再统一判断，如果容量不够就直接无解了
                    curCount++;
                    correspondingCandidates.add(candidate);
                }
            }
            //内层循环结束后，如果curCount=1，correspondingCandidates中也只会有一个备选点
            //说明它只被一个备选点覆盖，那就加入一个HashMap
            if (curCount == 1) {
                singleCommunityCandidate.put(community, correspondingCandidates.get(0));  //该列表也只有一个元素
            }
        }
        return singleCommunityCandidate;
    }

    //性质5
    public static LinkedHashMap<Community, Candidate> property05(List<Candidate> F_Minus_F0_Minus_FF0, List<Community> EE5) {
        //存储孤立居民点及其对应的备选点的键值对
        LinkedHashMap<Community, Candidate> singleCommunityCandidate = new LinkedHashMap<>();
        //遍历各居民点E\E1\EE1=EE5和备选点集合F\F0\FF0，寻找孤立的备选点
        for (Community community : EE5) {
            //计算各备选点在F\F0中的邻接点个数
            int curCount = 0;
            List<Candidate> correspondingCandidates = new ArrayList<>();
            for (Candidate candidate : F_Minus_F0_Minus_FF0) {
                //如果当前居民点的邻接备选点id包含了F\F0中当前遍历到的备选点的id，curCount就加1
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    curCount++;
                    correspondingCandidates.add(candidate);
                }
            }
            //内层循环结束后，如果curCount=1，说明它只被一个备选点覆盖，那就加入一个HashMap
            if (curCount == 1) {
                singleCommunityCandidate.put(community, correspondingCandidates.get(0));  //该列表也只有一个元素
            }
        }
        return singleCommunityCandidate;
    }

    //性质6
    public static List<ServeCouple> property06(List<Candidate> F_Minus_F0_Minus_FF0, List<Community> EE5) {
        //定义性质6中的集合A
        List<Candidate> A = new ArrayList<>();
        //定义存储服务关系的列表
        List<ServeCouple> serveCouples = new ArrayList<>();
        //遍历EE5中各居民点ei，寻找其在F\F0\FF0中的最小服务距离及其对应的备选点fj
        for (Community community : EE5) {
            double minDis = Double.MAX_VALUE;
            Candidate minDisCandidate = null;
            for (Candidate candidate : F_Minus_F0_Minus_FF0) {
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    int rowIndex = candidate.getId() - 1;
                    int columnIndex = community.getId() - 1;
                    if (MainAlgorithm.disMatrix[rowIndex][columnIndex] < minDis) {
                        minDis = MainAlgorithm.disMatrix[rowIndex][columnIndex];
                        minDisCandidate = candidate;
                    }
                }
            }
            //内层循环结束后，当前遍历到的居民点已经找到了其最短距离对应的备选点id及其最短距离
            //将该最短距离备选点加入A
            A.add(minDisCandidate);
            //
            serveCouples.add(new ServeCouple(community.getId(), minDisCandidate.getId(), community.getUnsatisfiedNeed(), minDis));
        }
        //现在已经获取到了所有EE5中的居民点在F\F0\FF0中的最短服务距离备选点的id
        //接下来要遍历A中的备选点和居民点EE5，看其是否满足容量条件
        for (Candidate candidate : A) {
            double curRemainCapacity = candidate.getRemainCapacity();
            double curDominateNeed = 0.0;
            for (Community community : EE5) {
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curDominateNeed));
                    BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
                    curDominateNeed = bigDecimal_1.add(bigDecimal_2).doubleValue();
                }
            }
            //只要有一个备选点不满足容量条件，就return一个null，
            //主函数中再根据返回的是null还是一个List<Candidate>来判断一下，就知道性质6执行的结果是什么了
            if (curRemainCapacity < curDominateNeed) {
                return null;
            }
        }
        //如果上面的for循环没有返回空列表，说明性质6满足，返回serveCouples，serveCouples与全局的S【或许还有其他的】合并一下就可以计算一下当前的开设备选点和目标值了
        return serveCouples;
    }

    //性质7
    public static List<Candidate> property07(List<Candidate> F_Minus_F0_Minus_FF0, List<Community> EE5) {
        //定义要删除【加入F0】的备选点集合，返回到主函数中再进行删除
        List<Candidate> deletedCandidates = new ArrayList<>();
        //遍历图G[F\F0\FF0]中任意物资集散备选点fj和fh
        for (Candidate candidate01 : F_Minus_F0_Minus_FF0) {
            NextCandidate:
            for (Candidate candidate02 : F_Minus_F0_Minus_FF0) {
                //不同的备选点才会去做比较
                if (!candidate01.getId().equals(candidate02.getId())) {
                    //获取两个备选点在EE5中的邻接居民点
                    List<Community> neighborCommunities01 = new ArrayList<>();
                    List<Community> neighborCommunities02 = new ArrayList<>();
                    for (Community community : EE5) {
                        if (candidate01.getSlaveCommunityIds().contains(community.getId())) {
                            neighborCommunities01.add(community);
                        }
                        if (candidate02.getSlaveCommunityIds().contains(community.getId())) {
                            neighborCommunities02.add(community);
                        }
                    }
                    //1.判断它们能服务的居民点集合是否存在包含关系，当前candidate01是更好的选择
                    //由于每一对都会遍历两次，且角色互换，所以这里不用再写一份倒过来的代码了
                    if (neighborCommunities01.containsAll(neighborCommunities02)) {
                        //遍历被包含的那个居民点集合neighborCommunities02，
                        //2.看是否candidate01到neighborCommunities02的距离都不大于candidate02到neighborCommunities02的距离
                        for (Community community : neighborCommunities02) {
                            int columnIndex = community.getId() - 1;
                            int rowIndex01 = candidate01.getId() - 1;
                            int rowIndex02 = candidate02.getId() - 1;
                            //只要有一个大于就break判断下一对，也就是说必须全部满足，只要有一个不满足都不行，此时直接返回一个空列表deletedCandidate
                            if (MainAlgorithm.disMatrix[rowIndex01][columnIndex] > MainAlgorithm.disMatrix[rowIndex02][columnIndex]) {
                                continue NextCandidate;  //只要有一个大于就continue判断下一对，if后面的也千万不要执行了，因为若是执行了且刚好满足第三个条件，那就还真还加上去了
                            }
                        }
                        //3.如果经历了上面的for循环而没有break掉，那就来到了最后一关
                        //判断candidate01的容量是否能容纳neighborCommunities01中所有居民点需求量
                        double curRemainCapacity = candidate01.getRemainCapacity();
                        double curDominateNeed = 0.0;
                        for (Community community : neighborCommunities01) {
                            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curDominateNeed));
                            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
                            curDominateNeed = bigDecimal_1.add(bigDecimal_2).doubleValue();
                        }
                        //如果满足条件，那么所有条件都满足了，只有坚持到这里，deletedCandidates才会加上candidate02
                        if (curRemainCapacity >= curDominateNeed) {
                            deletedCandidates.add(candidate02);
                        }
                    }
                }
            }
        }
        return deletedCandidates;
    }

    //性质8：解除符合条件的备选点和居民点之间的邻接关系【降阶时使用；分配中使用就相当于回溯中使用了】
    public static void property08(List<Candidate> F_Minus_F0_Minus_FF0, List<Community> EE5) {
        //遍历F\F0\FF0中的各fj和EE5中的各ei
        for (Candidate fj : F_Minus_F0_Minus_FF0) {
            for (Community ei : EE5) {
                //如果当前遍历到的居民点是当前遍历到的备选点的邻接居民点
                if (fj.getSlaveCommunityIds().contains(ei.getId())) {
                    //进一步的，如果当前备选点的剩余容量已经少于当前居民点的剩余需求量
                    if (fj.getRemainCapacity() < ei.getUnsatisfiedNeed()) {
                        //那么就基于当前居民点遍历其在F\F0\FF0中的邻接备选点，寻找一个各方面完全碾压当前fj的备选点fh
                        for (Candidate fh : F_Minus_F0_Minus_FF0) {
                            //如果当前遍历到的备选点是当前遍历到的居民点的邻接备选点
                            if (ei.getDominatedCandidateIds().contains(fh.getId())) {
                                int columnIndex = ei.getId() - 1;
                                int rowIndex_fj = fj.getId() - 1;
                                int rowIndex_fh = fh.getId() - 1;
                                //1.条件1：dih≤dij
                                if (MainAlgorithm.disMatrix[rowIndex_fh][columnIndex] <= MainAlgorithm.disMatrix[rowIndex_fj][columnIndex]) {
                                    //2.条件1满足的基础上再判断条件2
                                    double curRemainCapacity = fh.getRemainCapacity();
                                    double curDominateNeed = 0.0;
                                    for (Community community : EE5) {
                                        //是邻接点才要加上这个值
                                        if (fh.getSlaveCommunityIds().contains(community.getId())) {
                                            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curDominateNeed));
                                            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
                                            curDominateNeed = bigDecimal_1.add(bigDecimal_2).doubleValue();
                                        }
                                    }
                                    //如果满足条件，那么所有条件都满足了
                                    if (curRemainCapacity >= curDominateNeed) {
                                        //解除ei和fj的邻接关系：把对方从邻接点集中删除
                                        //此处直接修改属性，调用方传入的实参也就同步修改了，因为我并没有克隆再操作
                                        ei.getDominatedCandidateIds().remove(fj.getId());
                                        fj.getSlaveCommunityIds().remove(ei.getId());
                                        //(1)降阶时可以直接对主函数中的全局变量进行操作；
                                        //(2)分配中需要对复制过来的变量进行操作；
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //性质9
    public static List<Candidate> property09(List<Candidate> F, List<Candidate> F0, List<Candidate> F1, List<Community> E, List<Community> E1) {

        //定义不开设【加入F0】的备选点集合，返回到主函数中再进行处理
        //虽然这是在降阶过程中，也可以直接对主函数的变量进行操作，但是还是拐一道弯更清爽一些
        List<Candidate> deleteCandidates = new ArrayList<>();
        //从主算法中获取上界
        double u = MainAlgorithm.u;

        //F\F0\F1=F5就是F，E就是E\E1，由于性质9还是在进行假设，所以复制一份列表再进行操作，暂时不要对主函数中的变量造成影响
        //计算F\F0\F1=F5(F)，E\E1=E
        List<Candidate> newF = new ArrayList<>(F);
        List<Candidate> newF0 = new ArrayList<>(F0);
        List<Candidate> newF1 = new ArrayList<>(F1);
        List<Community> newE = new ArrayList<>(E);
        List<Community> newE1 = new ArrayList<>(E1);

        for (Candidate fj : newF) {
            //定义FF0
            List<Candidate> FF1 = new ArrayList<>();
            //FF1={fj}，FF5(F)=FF5\{fj}
            FF1.add(fj);
            //新建一个列表用来存储FF5\{fj}的情况，以免形成一边遍历一边修改的情况，并且也保持了newF始终如一
            List<Candidate> newF_Minus_fj = new ArrayList<>(newF);
            newF_Minus_fj.remove(fj);
            //调用下界子算法计算下界，以F1_temp作为形参，但是下界子算法同时需要F0和FF0的，所以这些都要根据当前元素更新之后再传入下界子算法
            double b = LowerBoundAlgorithm.lowerBoundAlgorithm(newF_Minus_fj, newF0, newF1, new ArrayList<>(), FF1, newE, newE1, new ArrayList<>());
            System.out.println("当前假设fj：" + fj.getId() + "开设时，下界为：" + b);
            //如果得到的下界大于上界，那么fj一定不开设，将其加入F0，可以直接在主函数中的全局变量上操作
            if (b > u) {
                deleteCandidates.add(fj);
            }
        }
        //在调用方进行非空判断
        return deleteCandidates;
    }

    //性质10
    public static List<Candidate> property10(List<Candidate> F, List<Candidate> F0, List<Candidate> F1, List<Community> E, List<Community> E1) {
        //定义不开设【加入F1】的备选点集合，返回到主函数中再进行处理
        //虽然这是在降阶过程中，也可以直接对主函数的变量进行操作，但是还是拐一道弯更清爽一些
        List<Candidate> openCandidates = new ArrayList<>();
        //从主算法中获取上界
        double u = MainAlgorithm.u;

        //F\F0\F1=F5(F)，E就是E\E1，由于性质9还是在进行假设，所以复制一份列表再进行操作，暂时不要对主函数中的变量造成影响
        //计算F\F0\F1=F5(F)，E\E1=E
        List<Candidate> newF = new ArrayList<>(F);
        List<Candidate> newF0 = new ArrayList<>(F0);
        List<Candidate> newF1 = new ArrayList<>(F1);
        List<Community> newE = new ArrayList<>(E);
        List<Community> newE1 = new ArrayList<>(E1);

        for (Candidate fj : newF) {
            //定义FF0
            List<Candidate> FF0 = new ArrayList<>();
            //FF1={fj}，FF5(F)=FF5\{fj}
            FF0.add(fj);
            //新建一个列表用来存储FF5\{fj}的情况，以免形成一边遍历一边修改的情况，并且也保持了newF始终如一
            List<Candidate> newF_Minus_fj = new ArrayList<>(newF);
            newF_Minus_fj.remove(fj);
            //调用下界子算法计算下界，以F1_temp作为形参，但是下界子算法同时需要F0和FF0的，所以这些都要根据当前元素更新之后再传入下界子算法
            double b = LowerBoundAlgorithm.lowerBoundAlgorithm(newF_Minus_fj, newF0, newF1, FF0, new ArrayList<>(), newE, newE1, new ArrayList<>());
            System.out.println("当前假设fj：" + fj.getId() + "不开设时，下界为：" + b);
            //如果得到的下界大于上界，那么fj一定不开设，将其加入F0，可以直接在主函数中的全局变量上操作
            if (b > u) {
                openCandidates.add(fj);
            }
        }
        //在调用方进行非空判断
        return openCandidates;
    }

    //性质11
    public static List<Candidate> property11(List<Candidate> F_Minus_F0, List<Community> E_Minus_E1) {
        //定义要开设【加入F1】的备选点集合，返回到主函数中再进行处理
        List<Candidate> openCandidates = new ArrayList<>();

        //遍历E\E1，计算其总剩余未满足需求量
        double wholeUnsatisfiedNeed = 0.0;
        for (Community community : E_Minus_E1) {
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(wholeUnsatisfiedNeed));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(community.getUnsatisfiedNeed()));
            wholeUnsatisfiedNeed = bigDecimal_1.add(bigDecimal_2).doubleValue();
        }

        //依次做假设，看假设之后剩余可能开设的备选点的总剩余容量是否大于等于总剩余需求量
        //此时的F_temp=F\F0，因为此时FF0_temp和FF1_temp都是空的
        for (Candidate fj : F_Minus_F0) {
            //将当前candidate加入FF0，FF0=空集∪{fj}，F_temp=F\F0\FF0【{fj}】
            List<Candidate> F_temp = new ArrayList<>(F_Minus_F0);
            //本轮假设fj不开设，F_temp是F\F0\{fj}之后的列表
            F_temp.remove(fj);
            //遍历F\F0\{fj}的备选点集合F_temp，计算其总剩余容量
            double wholeRemainCapacity = 0.0;
            for (Candidate candidate : F_temp) {
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(wholeRemainCapacity));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(candidate.getRemainCapacity()));
                wholeRemainCapacity = bigDecimal_1.add(bigDecimal_2).doubleValue();
            }
            if (wholeRemainCapacity < wholeUnsatisfiedNeed) {
                openCandidates.add(fj);
            }
        }
        //在调用方进行非空判断
        return openCandidates;
    }
}