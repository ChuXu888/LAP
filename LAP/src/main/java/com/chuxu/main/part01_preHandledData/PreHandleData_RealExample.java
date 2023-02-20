package com.chuxu.main.part01_preHandledData;

import com.chuxu.entity.Candidate;
import com.chuxu.entity.Community;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.*;

public class PreHandleData_RealExample {

    //居民区和备选点之间的距离矩阵【极其重要的一个原始素材，用来创建分配子算法中的边的】
    public static double INF = 10000.0;  //代表不连通
    public static double need = 0.7;  //每人每天所需物资总量
    public static int K = 15;  //选址点数量
    public static final int candidateNum = 21;
    public static final int communityNum = 40;
    public static final double D = 5.0;  //服务距离上限d
    public static double[][] disMatrix = new double[candidateNum][communityNum];
    //人口向量
    public static final int[] populationNums = new int[communityNum];
    //容量向量
//    public static final double[] capacities = new double[candidateNum];
    public static final double[] capacities = {9947.0, 9997.0, 8023.0, 7989.0, 8764.0, 9432.0, 8508.0, 8303.0, 7542.0, 8159.0, 7631.0, 7916.0, 9788.0, 8422.0, 9150.0, 9711.0, 9357.0, 8127.0, 8565.0, 8609.0, 9799.0};
    //备选点和居民点列表
    public static final List<Community> communities = new ArrayList<>();
    public static final List<Candidate> candidates = new ArrayList<>();

    public static void main(String[] args) throws Exception {
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

    //3.预处理全过程
    public static void preHandle() throws Exception {
        createEntity();
    }

    //2.创造实体
    public static void createEntity() throws Exception {
        DecimalFormat decimalFormat = new DecimalFormat("#.0000");
        Random random = new Random();
        capsulateDisMatrix();
        capsulatePopulation();
//        for (int i = 0; i < candidateNum; i++) {
//            capacities[i] = random.nextInt(2500) + 7500;
//        }
////        Arrays.fill(capacities, 5000);

        //封装居民点实体
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

        //封装备选点实体
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

    //1.2.导入人口向量
    public static void capsulatePopulation() throws Exception {
        String path = "E:\\Java\\IDEA_Project\\Papers\\Paper02-List\\src\\main\\resources\\";
        //1.创建一个工作簿，使用excel能操作的，它都能操作
        //(1)获取文件流
        FileInputStream fileInputStream = new FileInputStream(path + "论文案例数据20230215.xlsx");
        //(2)把这个流放到这个工作簿里
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        //2.获取一个工作表sheet，0代表第一个sheet
        Sheet sheet = workbook.getSheetAt(2);
        //循环读取表中的数据
        int rowCount = sheet.getPhysicalNumberOfRows();  //获取总行数
        for (int i = 0; i < rowCount; i++) {
            Row curRow = sheet.getRow(i);  //获取当前行
            int colCount = curRow.getPhysicalNumberOfCells();  //获取当前行的列数
            Cell curCell = curRow.getCell(1);  //第二列是人口数据
            if (curCell != null) {
                curCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                populationNums[i] = (int) curCell.getNumericCellValue();
            }
        }
    }

    //1.1.导入数据矩阵
    public static void capsulateDisMatrix() throws Exception {
        String path = "E:\\Java\\IDEA_Project\\Papers\\Paper02-List\\src\\main\\resources\\";
        //1.创建一个工作簿，使用excel能操作的，它都能操作
        //(1)获取文件流
        FileInputStream fileInputStream = new FileInputStream(path + "论文案例数据距离矩阵20230215.xlsx");
        //(2)把这个流放到这个工作簿里
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        //2.获取一个工作表sheet，0代表第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        //循环读取表中的数据
        int rowCount = sheet.getPhysicalNumberOfRows();  //获取总行数
        for (int i = 0; i < rowCount; i++) {
            Row curRow = sheet.getRow(i);  //获取当前行
            int colCount = curRow.getPhysicalNumberOfCells();  //获取当前行的列数
            for (int j = 0; j < colCount; j++) {
                Cell curCell = curRow.getCell(j);  //获取当前单元格
                if (curCell != null) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    curCell.setCellType(Cell.CELL_TYPE_NUMERIC);
                    disMatrix[i][j] = Double.parseDouble(decimalFormat.format(curCell.getNumericCellValue()));
                }
            }
        }
    }
}