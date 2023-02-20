package com.chuxu.main.part05_lowerBoundAlgorithm;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.CloneObject;
import com.chuxu.entity.Community;
import com.chuxu.entity.ServeCouple;
import com.chuxu.main.MainAlgorithm;
import com.chuxu.main.part02_properties.Properties;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chuxu.main.MainAlgorithm.*;

public class LowerBoundAlgorithm {

    private static final double b_limit = 1000000001.0;  //是重要保重下界的极端值大于上界的极端值，这样出现一些特殊情况时，可以保证下界大于上界进而剪枝

    public static void main(String[] args) {

    }

    public static double lowerBoundAlgorithm(List<Candidate> F, List<Candidate> F0, List<Candidate> F1, List<Candidate> FF0, List<Candidate> FF1, List<Community> E, List<Community> E1, List<Community> EE1) {
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("现在进入下界子算法");
        //定义下界
        double b = 0.0;

        //注意：上界子算法中的一部分是全局降阶；而下界子算法从一开始就不是全局降阶，都是在自己的一个小空间内自己玩，所以下界子算法中一开始就要克隆所有的形参，然后自己玩，不对主函数中传入的原变量造成影响
        CloneObject cloneObject = cloneObject(F, F0, F1, FF0, FF1, E, E1, EE1);
        F = cloneObject.getF();  //本质上为FF5=F\F1\F0\FF1\FF1_temp\FF0
        F0 = cloneObject.getF0();
        F1 = cloneObject.getF1();
        FF0 = cloneObject.getFF0();
        FF1 = cloneObject.getFF1();
        E = cloneObject.getE();  //本质上为EE5，代表所有还未找到归属的居民点，是主要操作对象
        E1 = cloneObject.getE1();
        EE1 = cloneObject.getEE1();

        //Step1：由性质7找出FF5中一定不开设的备选点并加入集合FF0；
        //定义传入性质7中的参数，由于F是实际上的FF5，而E也是实际上的EE5，所以这里把它们赋值一份传入性质7中去
        //注意：property07()里面采取的是add()操作，所以deletedCandidates内部存储的备选点和传入形参F中的备选点指向的是同一个对象，【一改具改】
        List<Candidate> deletedCandidates = com.chuxu.main.part02_properties.Properties.property07(F, E);
        //对性质7的返回做非空判断，如果不为空才会进入后续的处理逻辑，由于性质内部都是new一个对象来返回的，所以不会为null，只用判断其size是否为0即可
        if (!deletedCandidates.isEmpty()) {
            //从F(FF5)中移除deletedCandidates，移除时会进行equals判断，我重写了方法，只要id相同就认为是同一个备选点
            F.removeAll(deletedCandidates);
            //将deletedCandidates加入FF0
            FF0.addAll(deletedCandidates);
            //遍历所有的E\E1\EE1(E)，移除所有的居民点的dominatedCandidateIds属性中的deletedCandidate的id
            for (Community community : E) {
                for (Candidate candidate : deletedCandidates) {
                    if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                        Set<Integer> curDominatedCandidateIds = community.getDominatedCandidateIds();
                        curDominatedCandidateIds.remove(candidate.getId());
                        community.setDominatedCandidateIds(curDominatedCandidateIds);
                    }
                }
            }
            //处理完毕后打印看看结果
            System.out.println("经过性质7处理，有部分结点加入F0之后的FF5(F)：");
            F.forEach(System.out::println);
            System.out.println("=================================================");
            System.out.println("经过性质7处理后的FF0：");
            FF0.forEach(System.out::println);
            System.out.println("=================================================");
            System.out.println("经过性质7处理后，将EE5(E)中所有居民点的邻接备选点中涉及到F0中元素的备选点都删了：");
            E.forEach(System.out::println);
            System.out.println("========================================================================");
        }

        //Step2：由性质5找出FF5中一定开设的备选点fj并加入集合FF1_temp，并将对应的居民点加入EE1，更新FF1_temp中各备选点的剩余容量rj。
        List<Candidate> FF1_temp = new ArrayList<>();
        List<ServeCouple> serveCouples = new ArrayList<>();
        //定义形参，应为F\F0\FF0=F1∪FF1∪FF5(E)代表能连的备选点，E\E1\EE1=EE5(E)代表还未找到归属的居民点
        List<Candidate> F1_Add_FF1_Add_FF5 = new ArrayList<>();
        F1_Add_FF1_Add_FF5.addAll(F1);
        F1_Add_FF1_Add_FF5.addAll(FF1);
        F1_Add_FF1_Add_FF5.addAll(F);

        //借用一下上面定义的F1_Add_FF1_Add_FF5，用作性质2和性质3的判断
        //由性质2和性质3判断该情况下是否无解，
        //F(FF5)\F0\FF0=F1∪FF1∪FF5(F)，E(EE5)\E1\EE1=EE5(E)
        if (!com.chuxu.main.part02_properties.Properties.property02(F1_Add_FF1_Add_FF5, E)) {
            System.out.println("该情况下无可行解！");
            return b_limit;
        }
        if (!com.chuxu.main.part02_properties.Properties.property03(F1_Add_FF1_Add_FF5, E)) {
            System.out.println("该情况下无可行解！");
            return b_limit;
        }

        //调用性质5
        //注意：property05()里面采取的是put()操作，所以singleCommunityCandidate内部存储的<备选点，居民点>和传入形参的<F1_Add_FF1_Add_FF5,E>指向的是同一个对象，【一改具改】
        LinkedHashMap<Community, Candidate> singleCommunityCandidate = com.chuxu.main.part02_properties.Properties.property05(F1_Add_FF1_Add_FF5, E);
        //做非空判断
        if (!singleCommunityCandidate.isEmpty()) {
            for (Map.Entry<Community, Candidate> entry : singleCommunityCandidate.entrySet()) {
                //当前涉及到居民点和备选点，它们和<F1_Add_FF1_Add_FF5,E>指向的是同一个对象，【一改具改】
                Community curCommunity = entry.getKey();
                Candidate curCandidate = entry.getValue();
                //计算更新后的容量
                //正是因为curCommunity以及F1_Add_FF1_Add_FF5以及下面的varyCandidate本质上指向的是同一个对象，所以下面重新setRemainCapacity，而这里又getRemainCapacity才能保证是最新的
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curCandidate.getRemainCapacity()));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curCommunity.getUnsatisfiedNeed()));
                double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();

                //从F1_Add_FF1_Add_FF5中找到当前curCandidate对应的那个备选点对象，因为curCandidate就是在property05中从F1_Add_FF1_Add_FF5中找到的，所以现在也肯定能从F1_Add_FF1_Add_FF5中找到
                //然后让varyCandidate这个引用也指向那个对象，修改其剩余容量并重新设置
                //如此curCandidate、varyCandidate、candidate这三个引用都共同指向F、F1、FF1、FF1temp中的对象，修改其剩余容量并重新设置无需索引，因为只修改一个的剩余容量就【一改具改】
//                Candidate varyCandidate = null;
//                for (Candidate candidate : F1_Add_FF1_Add_FF5) {
//                    if (curCandidate.getId().equals(candidate.getId())) {
//                        varyCandidate = candidate;
//                        break;
//                    }
//                }
//                varyCandidate.setRemainCapacity(newRemainCapacity);

                //既然三个引用指向同一个对象等会我直接设置curCandidate的【剩余容量】不也行么？
                curCandidate.setRemainCapacity(newRemainCapacity);

                //将fj并加入集合FF1_temp，注意FF1temp是可能会重复的，它是Value
                FF1_temp.add(curCandidate);
                //将ei加入EE1，EE1不会重复，它是Key
                EE1.add(curCommunity);

                //记录当前的ServeCouple对，最后的下界根据ServeCouple来整体计算
                int rowIndex = curCandidate.getId() - 1;
                int columnIndex = curCommunity.getId() - 1;
                double curDis = disMatrix[rowIndex][columnIndex];

                //将ei,fj,ei的需求量,服务距离，加入服务集S中，这里是下界了，一定要用unsatisfiedNeed属性了
                serveCouples.add(new ServeCouple(curCommunity.getId(), curCandidate.getId(), curCommunity.getUnsatisfiedNeed(), curDis));
            }

            //打印serveCouples看看咯
            System.out.println("=================================================");
            System.out.println("serveCouples：");
            serveCouples.forEach(System.out::println);
            //循环结束后，先使用Stream流优雅地对FF1_temp去重
            System.out.println("FF1_temp.size() = " + FF1_temp.size());
            FF1_temp = FF1_temp.stream().distinct().collect(Collectors.toList());
            System.out.println("FF1_temp.size() = " + FF1_temp.size());
            System.out.println("=================================================");
            System.out.println("FF1_temp：");
//            FF1_temp.forEach(System.out::println);

            //由性质2补判断该情况下是否无解，传入形参应为F1∪FF1∪FF1_temp
            List<Candidate> F1_Add_FF1_Add_FF1temp = new ArrayList<>();
            F1_Add_FF1_Add_FF1temp.addAll(F1);
            F1_Add_FF1_Add_FF1temp.addAll(FF1);
            F1_Add_FF1_Add_FF1temp.addAll(FF1_temp);
            F1_Add_FF1_Add_FF1temp=F1_Add_FF1_Add_FF1temp.stream().distinct().collect(Collectors.toList());  //去重
            if (!Properties.property02_c(F1_Add_FF1_Add_FF1temp)) {
                System.out.println("该情况下无解！");
                return b_limit;
            }

            //性质5的后续判断：循环结束后，查看FF1_temp中元素的剩余容量是否都不小于0，如果是，那就可以继续；否则该问题无解。
            for (Candidate candidate : FF1_temp) {
                if (candidate.getRemainCapacity() < 0) {
                    System.out.println("单度居民点对应的备选点容量不够，该情况下无解！");
                    return b_limit;
                }
            }

            //一次性执行FF5(F)=FF5\FF1_temp，EE5(E)=E\EE1，而不是在循环里面发现一个弄一个
            F.removeAll(FF1_temp);
            E.removeAll(EE1);

            //处理完毕后打印看看结果
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的FF5(F)：");
//            F.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的F1：");
//            F1.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的F0：");
//            F0.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的FF1：");
//            FF1.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的FF1_temp：");
//            FF1_temp.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的FF0：");
//            FF0.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的EE5(E)：");
//            E.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的E1：");
//            E1.forEach(System.out::println);
//            System.out.println("=================================================");
//            System.out.println("经过性质5处理后的EE1：");
//            EE1.forEach(System.out::println);
        }

        //Step3：初始化集合Flow=F1∪FF1∪FF1temp，FF5(F)=F\F1\F0\FF1\FF1_temp\FF0始终成立，EE5(E)=E\E1\EE1始终成立；
        List<Candidate> F_low = new ArrayList<>();
        //F_low克隆自F1、FF1、FF1_temp，也是保持了当前的最新状态
        for (Candidate candidate : F1) {
            F_low.add(candidate.clone());
        }
        for (Candidate candidate : FF1) {
            F_low.add(candidate.clone());
        }
        for (Candidate candidate : FF1_temp) {
            F_low.add(candidate.clone());
        }
        //对Flow去重，因为FF1_temp很可能和F1、FF1有重叠
        F_low = F_low.stream().distinct().collect(Collectors.toList());

        //Step4：依次获取EE5中各居民点ei的min_d(G[F\F0\FF0], ei)对应的备选点fj并将其加入集合Flow；
        //在这里就直接用serveCouples存储服务关系
        //现在的F实际上是FF5，F\F0\FF0=F1∪FF1∪FF5，注意此处的【FF1分为了FF1和FF1_temp】，所以将此时的F、F1、FF1、FF1_temp合并起来就是F\F0\FF0
        List<Candidate> F1_Add_FF1_Add_FF1temp_Add_FF5 = new ArrayList<>();
        //此处使用了克隆，等会也要看看有没有必要
        for (Candidate candidate : F1) {
            F1_Add_FF1_Add_FF1temp_Add_FF5.add(candidate.clone());
        }
        for (Candidate candidate : FF1) {
            F1_Add_FF1_Add_FF1temp_Add_FF5.add(candidate.clone());
        }
        for (Candidate candidate : F) {
            F1_Add_FF1_Add_FF1temp_Add_FF5.add(candidate.clone());
        }
        for (Candidate candidate : FF1_temp) {
            F1_Add_FF1_Add_FF1temp_Add_FF5.add(candidate.clone());
        }
        System.out.println("=================================================");
        System.out.println("F1∪FF1∪FF1_temp∪FF5：");
        F1_Add_FF1_Add_FF1temp_Add_FF5 = F1_Add_FF1_Add_FF1temp_Add_FF5.stream().distinct().collect(Collectors.toList());  //由于F1和FF1_temp中的元素可能会重合，所以要去重
//        F1_Add_FF1_Add_FF1temp_Add_FF5.forEach(System.out::println);  //打印

        //专用于存储当前步骤确定的服务关系的ServeCouple对象列表
        List<ServeCouple> serveCouples_temp = new ArrayList<>();
        //遍历此时的EE5(E)【还未找到归属的】和F1_Add_FF1_Add_FF1temp_Add_FF5【还能被连接的】寻找各居民点的最短距离备选点
        for (Community community : E) {
            double minDis = Double.MAX_VALUE;
            int minDisCandidateId = -1;
            for (Candidate candidate : F1_Add_FF1_Add_FF1temp_Add_FF5) {
                if (community.getDominatedCandidateIds().contains(candidate.getId())) {
                    int columnIndex = community.getId() - 1;
                    int rowIndex = candidate.getId() - 1;
                    if (disMatrix[rowIndex][columnIndex] < minDis) {
                        minDis = disMatrix[rowIndex][columnIndex];
                        minDisCandidateId = candidate.getId();
                    }
                }
            }

            //循环结束后，就得到了minDis和minDisCandidate设置当前居民点的一些属性
            community.setMinDis(minDis);
            community.setMinDisCandidateId(minDisCandidateId);

            //向serveCouples中添加一组对应关系
            serveCouples_temp.add(new ServeCouple(community.getId(), minDisCandidateId, community.getUnsatisfiedNeed(), minDis));
        }

        //获取serveCouples中涉及到的备选点然后去重
        System.out.println("打印各居民点及其最短距离对应的备选点：");
        for (ServeCouple serveCouple : serveCouples_temp) {
//            System.out.println(serveCouple);
            //当前服务对涉及到的两个对象，由于serveCouple中只存储了id，所以要从主函数的完整列表中根据这个(id-1)获取一下
            //由于下界必须自己玩，不能对主函数中的变量造成影响，所以这里克隆一下，此时获得的居民点和备选点是一张白纸，备选点的容量是满的
            Community curCommunity = MainAlgorithm.EE5.get(serveCouple.getCommunityId() - 1).clone();
            Candidate curCandidate = MainAlgorithm.FF5.get(serveCouple.getCandidateId() - 1).clone();
            //计算更新后的容量
            //判断F_low是否已经包含当前curCandidate，如果包含那就从F_low取出来直接更新容量即可，否则就先加入F_low中一个【崭新的】【来自于FF5的curCandidate】再更新容量
            //由于重写了equals方法，此处只要id一样就会认为是一个备选点
            if (F_low.contains(curCandidate)) {
                for (Candidate candidate : F_low) {
                    if (candidate.getId().equals(curCandidate.getId())) {
                        //注意这里是candidate
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(candidate.getRemainCapacity()));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curCommunity.getWholeNeed()));
                        double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
                        //注意这里是candidate
                        candidate.setRemainCapacity(newRemainCapacity);
//                        System.out.println("candidate.getRemainCapacity() = " + candidate.getRemainCapacity());
                        break;  //只要找到了就行了
                    }
                }
            } else {
                //注意这里是curCandidate，是来自于FF5的【崭新的】curCandidate，再更新容量
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(curCandidate.getRemainCapacity()));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curCommunity.getWholeNeed()));
                double newRemainCapacity = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
                //注意这里是curCandidate
                curCandidate.setRemainCapacity(newRemainCapacity);
//                System.out.println("curCandidate.getRemainCapacity() = " + curCandidate.getRemainCapacity());
                F_low.add(curCandidate);
            }
        }

        //优雅地对F_low进行去重，由于上面有一个F_low.contains(curCandidate)的操作，所以这里不需要去重了
//        F_low = F_low.stream().distinct().collect(Collectors.toList());
        System.out.println("=================================================");
        System.out.println("更新了容量之后的F_low：");
//        F_low.forEach(System.out::println);

        //Step5：若|Flow|≤K，也即松弛了容量约束，数量约束也已满足，全覆盖也满足，返回该情况下的最理想情况作为下界【下界≤最优目标值】，此时就是等于了忽略容量约束的松弛问题的最优目标值，下界子算法结束，否则转至Step6；
        //根据serveCouples【下界中单度点确定的服务对】、serveCouples_temp【各居民点连最短距离确定的服务对】、S【全局降阶确定的服务对】计算下界
        double ideal_b = 0.0;
        if (F_low.size() <= K) {
            serveCouples.addAll(serveCouples_temp);
            serveCouples.addAll(S);
//            serveCouples.forEach(System.out::println);
            for (ServeCouple serveCouple : serveCouples) {
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(ideal_b));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(serveCouple.getResponsibleNeed() * serveCouple.getDistance()));
                ideal_b = bigDecimal_1.add(bigDecimal_2).doubleValue();
            }
            System.out.println("当F_low.size() <= K时，ideal_b = " + ideal_b);
            return ideal_b;
        }

        //Step7：下界根本不会删点，所以不存在上界中的要删除的点fk以及删除点后满足性质4的Etemp和Ftemp这些，复杂度还是低很多的
        //Step7-1：对于Flow\F1\FF1_temp中各备选点fk【还能调的，还有min2_d的】
        List<Candidate> Flow_Minus_F1_Minus_FF1temp = new ArrayList<>();
        //Flow_Minus_F1_Minus_FF1temp克隆自F_low，也是保持了当前的最新状态
        for (Candidate candidate : F_low) {
            Flow_Minus_F1_Minus_FF1temp.add(candidate.clone());
        }
        Flow_Minus_F1_Minus_FF1temp.removeAll(F1);
        Flow_Minus_F1_Minus_FF1temp.removeAll(FF1_temp);

        //Step7-2：假设fk以最短距离服务的EE5中的居民点集合为Etemp(fk)，
        //计算Etemp(fk)中各居民点ei的ki·{min2_d(G[Flow], ei)- min_d(G[Flow], ei)}值并求和，其值记为adjust_sum(fk)，

        for (Candidate fk : Flow_Minus_F1_Minus_FF1temp) {
            //定义变量
            List<Community> E_temp_fk = new ArrayList<>();

            //这里的代码逻辑出现了问题，因为选出来的Flow对于某些居民点来说可能就已经是单度点了，它们没有min2d了，所以要先进行排除

            //遍历EE5(E)=E\E1\EE1中各【非单度还能调的居民点】，看是否为当前遍历到的fk的邻接居民点
            //注意此处为【最短距离】服务的ei集合，而不是所有的ei集合，所以应该遍历serveCouples_temp，他是存储【非单度居民点】最短距离信息的列表
            for (ServeCouple serveCouple : serveCouples_temp) {
                if (serveCouple.getCandidateId().equals(fk.getId())) {
                    //根据id只能从主函数中的完整列表EE5来获取【白纸】对象，好在居民点需求要么由性质确定全部满足，要么就进分配子算法分配，所以除分配子算法的代码中
                    //可以不用过多对其unsatisfiedNeed属性在意，也就是说此处从EE5来获取【白纸】对象而不是从E(EE5)中获取，并没有什么影响
                    Community curCommunity = EE5.get(serveCouple.getCommunityId() - 1).clone();
                    //如果当前的curCommunity在Flow_Minus_F1_Minus_FF1temp为单度点，那么E_temp_fk就不加它，因为加入E_temp_fk的肯定是有min2d的
                    int neighborCount = 0;
                    //应该是在【还能连的】里面找吧，而不是在还能删的里面找
                    //由于在下界子算法中，我不想这边在计算删点的损失值，那边又牵扯进来新的点，所以此处【还能连的】应为Flow
                    //而不是F1_Add_FF1_Add_FF1temp_Add_FF5，它的FF5这部分会牵扯进来新的点
                    for (Candidate candidate : F_low) {
                        if (candidate.getSlaveCommunityIds().contains(curCommunity.getId())) {
                            neighborCount++;
                        }
                    }
                    //只有当前的curCommunity在Flow_Minus_F1_Minus_FF1temp中有至少两个邻接点，该居民点才有min2d，才能计算损失值
                    if (neighborCount > 1) {
                        E_temp_fk.add(curCommunity);
                    }
                }
            }

            //如果有某个节点没有以最短距离服务某个居民点，那么删它是无伤的，所以setWholeLoss(0.0)
            if (E_temp_fk.size() == 0) {
                fk.setWholeLoss(0.0);
            }

            if (E_temp_fk.size() != 0) {
                //定义损失值adjust_sum(fk)
                double adjust_sum_fk = 0.0;
                //循环结束后，得到当前遍历到的备选点fk的E_temp_fk了
                //遍历E_temp_fk中各ei，计算min_d(G[Flow]，ei)的值
                for (Community ei : E_temp_fk) {
                    //遍历Flow_Minus_F1_Minus_FF1temp获取最短距离及其对应的fh
                    double minDis = Double.MAX_VALUE;
                    int minDisCandidateId = -1;

                    //                for (Candidate fh : Flow_Minus_F1_Minus_FF1temp) {
                    //                    if (fh.getSlaveCommunityIds().contains(ei.getId())) {
                    //                        int columnIndex = ei.getId() - 1;
                    //                        int rowIndex = fh.getId() - 1;
                    //                        if (disMatrix[rowIndex][columnIndex] < minDis) {
                    //                            minDis = disMatrix[rowIndex][columnIndex];
                    //                            minDisCandidateId = fh.getId();
                    //                        }
                    //                    }
                    //                }

                    //遍历serveCouples_temp，就能获取当前ei的最短距离备选点及其最短距离，并且一个重要前提是，此时serveCouples_temp中由于都是存储的最短距离服务对
                    //所以还不会出现像上界那样的需求分散的情况，那样的话就会有两条serveCouple的getCommunityId()是一样的
                    for (ServeCouple serveCouple : serveCouples_temp) {
                        if (serveCouple.getCommunityId().equals(ei.getId())) {
                            //如果从serveCouples_temp列表中找到了居民点id和当前ei的id对应上的serveCouple
                            minDis = serveCouple.getDistance();
                            minDisCandidateId = serveCouple.getCandidateId();
                        }
                    }

                    //设置属性
                    ei.setMinDis(minDis);
                    ei.setMinDisCandidateId(minDisCandidateId);

                    //将当前Flow【还能连的】去掉最短距离对应的备选点，再找最短距离就是次短距离了，
                    //其实如果只有增删，而不改变内部对象的属性的话，也可以不用深拷贝
                    List<Candidate> Flow_Minus_Min = new ArrayList<>(F_low);
                    //去掉minDisCandidate，此处使用了一种很新的东西
                    int finalMinDisCandidateId = minDisCandidateId;
                    Flow_Minus_Min.removeIf(candidate -> candidate.getId().equals(finalMinDisCandidateId));
                    //遍历Fup_Minus_F1_Minus_Ftemp_Minus_fk获取次短距离及其对应的fh
                    double minDis2 = Double.MAX_VALUE;
                    int minDis2CandidateId = -1;
                    for (Candidate fh : Flow_Minus_Min) {
                        if (fh.getSlaveCommunityIds().contains(ei.getId())) {
                            int columnIndex2 = ei.getId() - 1;
                            int rowIndex2 = fh.getId() - 1;
                            if (disMatrix[rowIndex2][columnIndex2] < minDis2) {
                                minDis2 = disMatrix[rowIndex2][columnIndex2];
                                minDis2CandidateId = fh.getId();
                            }
                        }
                    }

                    //设置属性
                    ei.setMinDis2(minDis2);
                    ei.setMinDis2CandidateId(minDis2CandidateId);

                    //计算本轮的损失值，并设置到Flow_Minus_F1_Minus_FF1temp中的各备选点fk上的wholeLoss属性上
                    BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(adjust_sum_fk));
                    BigDecimal bigDecimal_2 = new BigDecimal(Double.toString((minDis2 - minDis) * ei.getWholeNeed()));
                    adjust_sum_fk = bigDecimal_1.add(bigDecimal_2).doubleValue();
                }
                fk.setWholeLoss(adjust_sum_fk);
            }
        }

        //Step8：计算下界
        //这里面存的是全部以最短距离服务的情况
        serveCouples.addAll(serveCouples_temp);
        //一定要加上全局降阶时定下来的存储在S中的配对，这三项加起来共同构成了论文中雷打不动的第一项
        serveCouples.addAll(S);
        //将这部分雷打不动的目标值计算出来
        for (ServeCouple serveCouple : serveCouples) {
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(b));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(serveCouple.getResponsibleNeed() * serveCouple.getDistance()));
            b = bigDecimal_1.add(bigDecimal_2).doubleValue();
        }

        System.out.println("========================================================");
        serveCouples.forEach(System.out::println);
        System.out.println("serveCouples+serveCouples_temp+S之后，b = " + b);
        System.out.println("========================================================");
        System.out.println("未按照wholeLoss排序的Flow_Minus_F1_Minus_FF1temp：");
        Flow_Minus_F1_Minus_FF1temp.forEach(System.out::println);

        //Step8-1：若|F1∪FF1_temp|=K，此时Flow\F1\FF1_temp中的都得删，那么就没有必要排序再筛选了；否则就需要排序之后
        //注意：前面就已经说过F1和FF1_temp是会出现重复的，所以这里必须先取并集，然后去重之后再取size()才行
        List<Candidate> calculateSizeCandidates2 = new ArrayList<>();
        calculateSizeCandidates2.addAll(F1);
        calculateSizeCandidates2.addAll(FF1_temp);
        calculateSizeCandidates2 = calculateSizeCandidates2.stream().distinct().collect(Collectors.toList());
        if (calculateSizeCandidates2.size() == K) {
            //然后再加上Flow_Minus_F1_Minus_FF1temp中各元素的损失值wholeLoss属性
            for (Candidate candidate : Flow_Minus_F1_Minus_FF1temp) {
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(b));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(candidate.getWholeLoss()));
                b = bigDecimal_1.add(bigDecimal_2).doubleValue();
            }
            System.out.println("当|F1∪FF1_temp|=K时，b = " + b);
            return b;
        }


        //Step8-2：若|F1∪FF1_temp|<K，则将Flow\F1\FF1_temp中各元素按adjust_sum(fk)值从小到大排序，假设排序后Flow中前|Flow|-K个元素构成的集合为Fdel，执行
        //对Flow_Minus_F1_Minus_FF1temp中各备选点按照adjust_sum_fk的值升序排序
        if (calculateSizeCandidates2.size() < K) {
            System.out.println("========================================================");
            System.out.println("未加上损失值时b = " + b);
            Collections.sort(Flow_Minus_F1_Minus_FF1temp);
            System.out.println("========================================================");
            System.out.println("按照wholeLoss排序之后的Flow_Minus_F1_Minus_FF1temp：");
            Flow_Minus_F1_Minus_FF1temp.forEach(System.out::println);

            List<Candidate> F_del = new ArrayList<>();
            //如果能删的点的数量小于【假想】要删的点的数量F_low.size() - K，那就【能删几个删几个呗，尽量将下界提高一点是一点】
            //注意：calculateSizeCandidates.size()是不能删的点的数量，Flow_Minus_F1_Minus_FF1temp.size()才是能删的点的数量
            if (Flow_Minus_F1_Minus_FF1temp.size() < F_low.size() - K) {
                F_del.addAll(Flow_Minus_F1_Minus_FF1temp);
            } else {
                //当确保能删的点的数量大于等于的要删的点的数量F_low.size() - K，才需要从排序之后的筛选，否则Flow_Minus_F1_Minus_FF1temp会报【指针溢出】
                for (int i = 0; i < F_low.size() - K; i++) {
                    F_del.add(Flow_Minus_F1_Minus_FF1temp.get(i));
                }
            }

            System.out.println("========================================================");
            System.out.println("F_del：");
            F_del.forEach(System.out::println);

            //遍历F_del，将各元素的损失值wholeLoss属性加到b上面去
            for (Candidate candidate : F_del) {
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(b));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(candidate.getWholeLoss()));
                b = bigDecimal_1.add(bigDecimal_2).doubleValue();
            }
            System.out.println("当|F1∪FF1_temp|<K时，b = " + b);
            return b;
        }
        return ideal_b;  //返回兜底方案
    }

    private static CloneObject cloneObject(List<Candidate> F, List<Candidate> F0, List<Candidate> F1, List<Candidate> FF0, List<Candidate> FF1, List<Community> E, List<Community> E1, List<Community> EE1) {
        List<Candidate> F_l = new ArrayList<>();
        for (Candidate candidate : F) {
            F_l.add(candidate.clone());
        }
        List<Candidate> F0_l = new ArrayList<>();
        for (Candidate candidate : F0) {
            F0_l.add(candidate.clone());
        }
        List<Candidate> F1_l = new ArrayList<>();
        for (Candidate candidate : F1) {
            F1_l.add(candidate.clone());
        }
        List<Candidate> FF0_l = new ArrayList<>();
        for (Candidate candidate : FF0) {
            FF0_l.add(candidate.clone());
        }
        List<Candidate> FF1_l = new ArrayList<>();
        for (Candidate candidate : FF1) {
            FF1_l.add(candidate.clone());
        }
        List<Community> E_l = new ArrayList<>();
        for (Community community : E) {
            E_l.add(community.clone());
        }
        List<Community> E1_l = new ArrayList<>();
        for (Community community : E1) {
            E1_l.add(community.clone());
        }
        List<Community> EE1_l = new ArrayList<>();
        for (Community community : EE1) {
            EE1_l.add(community.clone());
        }

        return new CloneObject(F_l, F0_l, F1_l, FF0_l, FF1_l, E_l, E1_l, EE1_l);
    }
}