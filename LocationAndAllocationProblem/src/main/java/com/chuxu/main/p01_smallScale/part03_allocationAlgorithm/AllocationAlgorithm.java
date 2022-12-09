package com.chuxu.main.p01_smallScale.part03_allocationAlgorithm;

import com.chuxu.entity.AllocationResult;
import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;
import com.chuxu.entity.Edge;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.chuxu.main.p01_smallScale.MainAlgorithm_SmallScale.edgesForTrafficOfGlobal;

public class AllocationAlgorithm {

    static List<String> nodes = new ArrayList<>();  //点列表
    static LinkedHashMap<String, Integer> nodeNameIndex = new LinkedHashMap<>();  //点的名字和索引的对照表
    static Set<Edge> edgesForTraffic = new LinkedHashSet<>();  //专门用于求最大流的边集
    static Set<Edge> edgesForShortestWay = new LinkedHashSet<>();  //专门用于求最短路径的边集
    static double[] shortestDis;  //定义存储最短距离的数组
    static String[] preNode;  //定义存储最短距离的数组
    static double INF = 10000.0;

    //4.分配子算法全过程【现在还只有最小费用最大流算法，后续还要加上一些前面的判断】
    public static AllocationResult allocationAlgorithm(List<Community> communities, List<Candidate> candidates, double[][] disMatrix) {
        System.out.println("============================================================================");
        System.out.println("============================================================================");
        System.out.println("现在进入分配子算法");

        //Step2：若F_temp=F1∪FF1中的备选点无法覆盖所有未覆盖到的居民点E\E1\EE1=EE5(E)，则该情况下分配子算法无解，结束分配子算法；
        //将所有备选点的能覆盖到的所有居民点的id的并集求出，并看其能否覆盖EE5，能覆盖则当前的这个F_temp可以搞定EE5
        List<Integer> neighborCommunityIds = new ArrayList<>();
        for (Candidate candidate : candidates) {
            neighborCommunityIds.addAll(candidate.getSlaveCommunityIds());
        }
        neighborCommunityIds = neighborCommunityIds.stream().distinct().collect(Collectors.toList());
        List<Integer> curCommunityIds = new ArrayList<>();
        for (Community community : communities) {
            curCommunityIds.add(community.getId());
        }
        //如果neighborCommunityIds不能包含curCommunityIds，那么就不行，分配子算法无解
        if (!neighborCommunityIds.containsAll(curCommunityIds)) {
            System.out.println("neighborCommunityIds不能包含curCommunityIds！");
            return new AllocationResult(false, null, null);
        }

        //每次调用分配子算法之前都要把之前的使用痕迹清理掉，两个数组会在BellmanFord算法里初始化，这里不用清理
        nodes.clear();
        nodeNameIndex.clear();
        //此处clear会导致回溯子算法中 edgesForTrafficOfGlobal = allocationResult.getEdgesForTraffic()也跟着清掉
        //所以在回溯子算法中应该new一个新的集合，可以解决增删同步的问题，若要对其中的元素的属性进行修改，就要用深拷贝
        edgesForTraffic.clear();
        edgesForShortestWay.clear();

        //1.初始化数据
        initialize(communities, candidates, disMatrix);

        //2.进入求最短路径==>调整流量的大循环
        int count = 0;
        while (true) {
            //2.1通过BellmanFord算法找最短路
            boolean flag = bellmanFord();
            count++;

            System.out.println("flag = " + flag);
            System.out.println("shortestDis = " + Arrays.toString(shortestDis));
            //如果最短路径为正无穷或者有负权环，说明BellmanFord算法结束了
            if (shortestDis[nodes.size() - 1] == INF || !flag) {
                break;
            }

//            System.out.println("BellmanFord算法处理后的最短距离列表shortestDis = " + Arrays.toString(shortestDis));
//            System.out.println("BellmanFord算法处理后的最短距离对应的前驱结点列表preNode = " + Arrays.toString(preNode));
//            System.out.println("BellmanFord算法求得超级源点s到超级汇点t的最短距离为 = " + shortestDis[nodes.size() - 1]);
            List<String> curShortestWay = searchShortestWay("s", "t");
//            System.out.println("BellmanFord算法求得超级源点s到超级汇点t的最短路径为 = " + curShortestWay);

            //2.2计算瓶颈值
            //获取当前最短路径curShortestWay涉及到的各条边并计算瓶颈值

            List<Double> curVariations = new ArrayList<>();
            for (int i = 0; i < curShortestWay.size() - 1; i++) {
                String curAffectedStartName = curShortestWay.get(i);
                String curAffectedEndName = curShortestWay.get(i + 1);
                for (Edge edge : edgesForTraffic) {
                    //这里是拿edgesForShortestWay中的边来和edgesForTraffic中的边做匹配，所以无论正反，都算匹配上了
                    if (curAffectedStartName.equals(edge.startName) && curAffectedEndName.equals(edge.endName)) {
                        //减运算用bigDecimal
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(edge.capacity));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(edge.actualFlow));
                        double variation = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
                        curVariations.add(variation);  //正向弧的最大调整量为(容量-实际流量)
                    }
                    if (curAffectedStartName.equals(edge.endName) && curAffectedEndName.equals(edge.startName)) {
                        curVariations.add(edge.actualFlow);  //反向弧的最大调整量为(实际流量-0.0)
                    }
                }
            }
//            System.out.println("curVariations = " + curVariations);
            double θ = Collections.min(curVariations);
//            System.out.println("调整量θ = " + θ);

            //如果最短路径上的流量调整量为0，也即虽然能找到最短路径，但是那条路上没得调了，就退出
//            if (θ == 0.0) {
//                break;
//            }

            //2.3调整edges中受到影响的边的流量
            //保存这一轮流量调整中收到影响的边，并不是完整的保存调整前的情况
            List<Edge> edgesForTraffic_Pre = new ArrayList<>();
            for (int i = 0; i < curShortestWay.size() - 1; i++) {
                String curAffectedStartName = curShortestWay.get(i);
                String curAffectedEndName = curShortestWay.get(i + 1);
                for (Edge edge : edgesForTraffic) {
                    if (curAffectedStartName.equals(edge.startName) && curAffectedEndName.equals(edge.endName)) {
                        edgesForTraffic_Pre.add(new Edge(edge.startName, edge.endName, edge.distance, edge.capacity, edge.actualFlow));

                        //加运算用bigDecimal
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(edge.actualFlow));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(θ));
                        edge.actualFlow = bigDecimal_1.add(bigDecimal_2).doubleValue();
                    }
                    if (curAffectedStartName.equals(edge.endName) && curAffectedEndName.equals(edge.startName)) {
                        edgesForTraffic_Pre.add(new Edge(edge.startName, edge.endName, edge.distance, edge.capacity, edge.actualFlow));

                        //减运算用bigDecimal
                        BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(edge.actualFlow));
                        BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(θ));
                        edge.actualFlow = bigDecimal_1.subtract(bigDecimal_2).doubleValue();
                    }
                }
            }

            //2.4根据EdgesForTraffic_Pre和EdgesForTraffic的变化情况，调整edgesForShortestWay中的边，
            //可能会增加一些负权边，可能有些边要完全变为反向
            //这里又涉及到了一边遍历一边删、一边遍历一边加的问题，要谨慎处理，一边遍历一边改倒是问题不大
            List<Edge> addEdges = new ArrayList<>();  //临时保存要加的边
            List<Edge> deleteEdges = new ArrayList<>();  //临时保存要删的边
            for (Edge edge_Pre : edgesForTraffic_Pre) {
                for (Edge edge : edgesForTraffic) {
                    if (edge_Pre.startName.equals(edge.startName) && edge_Pre.endName.equals(edge.endName)) {
                        //拿两种状态做对比，可以确定之前edgesForShortestWay中与该<起点，终点>相关的边的状态，
                        //是正向弧还是反向弧还是正反向都有，再结合现在的流量边的情况，确定现在需要做的事情
                        //①如果零流弧==>中间弧，之前为零流，说明只有正向弧，所以要增加一条反向弧
                        if (edge_Pre.actualFlow == 0.0 && edge.actualFlow > 0.0 && edge.actualFlow < edge.capacity) {
                            //注意这个edge.distance是从流量弧里面取得，它始终是正的，也正可作为一个标杆
                            addEdges.add(new Edge(edge.endName, edge.startName, edge.distance * (-1)));
                        }
                        //②如果零流弧==>饱和弧，之前为零流，说明只有正向弧，将正向变反向，权值*（-1）即可，但是这个修改得到edgesForShortestWay里面修改啊，要么就【先删后加】？
                        if (edge_Pre.actualFlow == 0.0 && edge.actualFlow.equals(edge.capacity)) {
                            deleteEdges.add(new Edge(edge.startName, edge.endName, edge.distance));
                            addEdges.add(new Edge(edge.endName, edge.startName, edge.distance * (-1)));
                        }
                        //③如果中间弧==>零流弧，就要删除反向边
                        if (edge_Pre.actualFlow > 0.0 && edge_Pre.actualFlow < edge.capacity && edge.actualFlow == 0.0) {
                            deleteEdges.add(new Edge(edge.endName, edge.startName, edge.distance * (-1)));
                        }
                        //④如果中间弧==>饱和弧，就要删除正向边
                        if (edge_Pre.actualFlow > 0.0 && edge_Pre.actualFlow < edge.capacity && edge.actualFlow.equals(edge.capacity)) {
                            deleteEdges.add(new Edge(edge.startName, edge.endName, edge.distance));
                        }
                        //⑤如果饱和弧==>零流弧，之前为饱和弧，说明只有反向弧，将反向变正向即可，也是先删后加
                        if (edge_Pre.actualFlow.equals(edge_Pre.capacity) && edge.actualFlow == 0.0) {
                            deleteEdges.add(new Edge(edge.endName, edge.startName, edge.distance * (-1)));
                            addEdges.add(new Edge(edge.startName, edge.endName, edge.distance));
                        }
                        //⑥如果饱和弧==>中间弧，之前为饱和弧，说明只有反向弧，就要增加正向边
                        if (edge_Pre.actualFlow.equals(edge_Pre.capacity) && edge.actualFlow > 0.0 && edge.actualFlow < edge.capacity) {
                            addEdges.add(new Edge(edge.startName, edge.endName, edge.distance));
                        }
                        break;  //既然当前的这一对【前世今生】已经找到了，那就去找下一对吧
                    }
                }
            }
            //移除边
            for (Edge deleteEdge : deleteEdges) {
                edgesForShortestWay.removeIf(edge -> edge.startName.equals(deleteEdge.startName) && edge.endName.equals(deleteEdge.endName));
            }
            //加边
            edgesForShortestWay.addAll(addEdges);
            System.out.println("============================================================================================");

//            for (Edge edge : edgesForTraffic) {
//                System.out.println(edge);
//            }
        }

        //3.算法结束后，判断从超级源点到各居民点的边是否均为饱和弧，若不是则分配子算法无解返回false，否则计算目标函数值并返回true
        //流量弧均为正向弧
        edgesForTraffic.forEach(System.out::println);

        for (Edge edge : edgesForTraffic) {
            if (edge.startName.equals("s")) {
                //这里由于计算机处理数值的误差，不一定是严格相等，可能是它们之间的绝对值之差小于某个很小的数
                //使用BigDecimal计算就解决了这一问题
                if (!edge.actualFlow.equals(edge.capacity)) {
                    return new AllocationResult(false, null, null);
                }
            }
        }

        //4.根据BellmanFord算法结束后的流量边，计算目标函数值
        double objectValue = 0.0;
        //这里偷了个懒，本来只计算ei到fj的弧的，但是其它弧的距离为0所以算了也不影响【0*flow=0】，所以就省了判断了
        for (Edge edge : edgesForTraffic) {
            if (edge.actualFlow != 0.0) {
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(objectValue));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(edge.actualFlow * edge.distance));
                objectValue = bigDecimal_1.add(bigDecimal_2).doubleValue();
            }
        }
        System.out.println("当前选址方案的最优目标值为 = " + objectValue);
        System.out.println("流量调整次数为 = " + count);
        return new AllocationResult(true, objectValue, edgesForTraffic);
    }

    //3.BellMan-Ford算法结束后根据前驱结点列表获取最短路径
    public static List<String> searchShortestWay(String startName, String endName) {
        //定义列表
        List<String> shortestWay = new ArrayList<>();
        //先把终点加进去
        shortestWay.add(endName);
        while (true) {
            //获取当前终点的前驱结点
            Integer curEndIndex = nodeNameIndex.get(endName);
            String curTransitName = preNode[curEndIndex];
            //如果当前终点的前驱结点已经是起点，结束寻路
            if (Objects.equals(curTransitName, startName)) {
                break;
            } else {
                //否则将当前终点的前驱节点作为一个中转结点加入列表
                endName = curTransitName;
                shortestWay.add(curTransitName);
            }
        }
        //最后加起点
        shortestWay.add(startName);
        //反转列表
        Collections.reverse(shortestWay);
        return shortestWay;
    }

    //2.BellMan-Ford核心算法
    public static boolean bellmanFord() {
        //1.每次调用bellmanFord算法，都要置距离矩阵除源点外均为正无穷，前驱节点均为自己
        shortestDis = new double[nodes.size()];
        Arrays.fill(shortestDis, INF);
        shortestDis[0] = 0.0;  //源点s的索引固定为0
        //2.初始化前驱结点矩阵
        preNode = new String[nodes.size()];
        for (int i = 0; i < preNode.length; i++) {
            preNode[i] = nodes.get(i);
        }
        //3.核心BellmanFord算法
        for (int i = 1; i < nodes.size(); i++) {  //执行结点个数-1轮
            for (Edge curEdge : edgesForShortestWay) {  //松弛每条边
                //取出当前第j条边的起点索引和终点索引
                Integer curStartIndex = nodeNameIndex.get(curEdge.startName);
                Integer curEndIndex = nodeNameIndex.get(curEdge.endName);
                //如果当前边的起点的最短距离为自定义的正无穷，那么无论它加上或者减去当前边的权重，都不可能使得当前边的终点的最短距离变小
                if (shortestDis[curStartIndex] == INF) {
                    continue;
                }
                //进行松弛判断
//                System.out.println("dis[curStartIndex] = " + shortestDis[curStartIndex]);
//                System.out.println("curEdge.weight = " + curEdge.distance);
//                System.out.println("dis[curEndIndex] = " + shortestDis[curEndIndex]);
                //加运算用bigDecimal
                BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(shortestDis[curStartIndex]));
                BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curEdge.distance));
                double newDis = bigDecimal_1.add(bigDecimal_2).doubleValue();
                if (newDis < shortestDis[curEndIndex]) {
//                    System.out.println("更新！");
                    shortestDis[curEndIndex] = newDis;
                    preNode[curEndIndex] = curEdge.startName;  //将终点的前驱结点修改为当前起点的名字
                }
//                System.out.println("=======================================================");
            }
        }
        boolean flag = true;
        //再多执行一次，如果还能松弛，则说明有负权环，令flag为false
        //很奇怪，这里也改成大数BigDecimal之后就行了；猜测还是因为没有用BigDecimal而导致一些细小的误差【误差都是导致数偏小，从而导致本来是等于的现在变成小于，然后进了if里面】，导致不等式成立
        //无论如何，这里的代码就把上面的内层循环的代码复制过来就完事了
        for (Edge curEdge : edgesForShortestWay) {  //松弛每条边
            //取出当前第j条边的起点索引和终点索引
            Integer curStartIndex = nodeNameIndex.get(curEdge.startName);
            Integer curEndIndex = nodeNameIndex.get(curEdge.endName);
            //如果当前边的起点的最短距离为自定义的正无穷，那么无论它加上或者减去当前边的权重，都不可能使得当前边的终点的最短距离变小
            if (shortestDis[curStartIndex] == INF) {
                continue;
            }
            //进行松弛判断
            //加运算用bigDecimal
            BigDecimal bigDecimal_1 = new BigDecimal(Double.toString(shortestDis[curStartIndex]));
            BigDecimal bigDecimal_2 = new BigDecimal(Double.toString(curEdge.distance));
            double newDis = bigDecimal_1.add(bigDecimal_2).doubleValue();
            if (newDis < shortestDis[curEndIndex]) {
                flag = false;
            }
        }
        return flag;
    }

    //1.初始化
    public static void initialize(List<Community> communities, List<Candidate> candidates, double[][] disMatrix) {
        //1.初始化结点列表
        nodes.add("s");
        for (Community community : communities) {
            nodes.add("e" + community.getId());
        }
        for (Candidate candidate : candidates) {
            nodes.add("f" + candidate.getId());
        }
        nodes.add("t");
//        System.out.println("nodes = " + nodes);

        //2.初始化节点的名字和索引的对照表
        for (int i = 0; i < nodes.size(); i++) {
            nodeNameIndex.put(nodes.get(i), i);
        }
//        System.out.println("nodeNameIndex = " + nodeNameIndex);

        //3.初始化专门用于求最大流的边集
        //(1)源点s到各居民点ei
        for (Community community : communities) {
            edgesForTraffic.add(new Edge("s", "e" + community.getId(), 0.0, community.getUnsatisfiedNeed(), 0.0));
        }
        //(2)各居民点ei到各备选点fj
        for (Community ei : communities) {
            for (Candidate fj : candidates) {
                //如果fj是ei的邻接备选点，那就建立一条它们之间的流量边，且预处理时，已经把大于10的定义为INF了
                if (fj.getSlaveCommunityIds().contains(ei.getId())) {
                    double curDis = disMatrix[fj.getId() - 1][ei.getId() - 1];
                    //预处理时，已经把大于10的定义为INF了，这个if也可以不要
                    if (curDis <= 10.0) {
                        edgesForTraffic.add(new Edge(
                                "e" + ei.getId(),
                                "f" + fj.getId(),
                                curDis,
                                Math.min(fj.getRemainCapacity(), ei.getUnsatisfiedNeed()),
                                0.0
                        ));
                    }
                }
            }
        }
        //(3)各备选点fj到汇点t
        for (Candidate candidate : candidates) {
            edgesForTraffic.add(new Edge("f" + candidate.getId(), "t", 0.0, candidate.getRemainCapacity(), 0.0));
        }

        //打印初始流量网络
//        edgesForTraffic.forEach(System.out::println);

        //4.初始化时专门用于求最短路径的边集，只能这样做深拷贝了
        for (Edge edge : edgesForTraffic) {
            edgesForShortestWay.add(new Edge(edge.startName, edge.endName, edge.distance));
        }

        //打印初始最短路径网络
//        edgesForShortestWay.forEach(System.out::println);
    }
}