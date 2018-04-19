import java.util.*;

/**
 * @author Fangjie Ding
 * @date 4/7/2018
 */

 /**
  * Reference:
  * https://github.com/wihoho/KNN
  * https://github.com/badlogic/knn
  * https://github.com/Stephaniefan/knn
  **/
  
public class Task2 {

    public static void main(String[] args) {
        LoadData testData = new LoadData();
        LoadData trainData = new LoadData();
        System.out.println("Please input the train and test files: [1] binary class label [2] real number label");
        Scanner scanner = new Scanner(System.in);
        int choose = scanner.nextInt();
        System.out.println("Please input train data:");
        Scanner scanner1 = new Scanner(System.in);
        String trainDataName = scanner1.nextLine();
        System.out.println("Please input test data:");
        Scanner scanner2 = new Scanner(System.in);
        String testDataName = scanner2.nextLine();
        // load Data
        loadData(trainData, testData, choose, trainDataName, testDataName);
        KNN knn = new KNN();
        // get the min&max for attributes
        Map<String, List<Double>> minmaxMap = knn.getMinMax(trainData);
        // matrix map for non numeric attributes
        Map<String, double[][]> matrixMap = new HashMap<>();
        double[][] serviceType = {{1.0, 0.2, 0.2, 0.0, 0.1}, {0.2, 1.0, 0.1, 0.0, 0.3},
                {0.2, 0.1, 1.0, 0.0, 0.2}, {0.0, 0.0, 0.0, 1.0, 0.0}, {0.1, 0.3, 0.2, 0.0, 1.0}};
        double[][] customer = {{1.0, 0.1, 0.0, 0.1, 0.2}, {0.1, 1.0, 0.0, 0.2, 0.2},
                {0.0, 0.0, 1.0, 0.0, 0.0}, {0.1, 0.2, 0.0, 1.0, 0.1}, {0.2, 0.2, 0.0, 0.1, 1.0}};
        double[][] size = {{1.0, 0.0, 0.1}, {0.0, 1.0, 0.1}, {0.1, 0.1, 1.0}};
        double[][] promotion = {{1.0, 0.8, 0.1, 0.5}, {0.8, 1.0, 0.0, 0.0},
                {0.1, 0.0, 1.0, 0.4}, {0.5, 0.0, 0.4, 1.0}};
        matrixMap.put("Service_type", serviceType);
        matrixMap.put("Customer", customer);
        matrixMap.put("Size", size);
        matrixMap.put("Promotion", promotion);

        Map<String, Double> weightedMap = new HashMap<>();
        weightedMap.put("Service_type", 0.0465);
        weightedMap.put("Customer", 0.0698);
        weightedMap.put("Monthly_fee", 0.2326);
        weightedMap.put("Advertisement_budget", 0.4650);
        weightedMap.put("Size", 0.0233);
        weightedMap.put("Promotion", 0.0465);
        weightedMap.put("Interest_rate", 0.0465);
        weightedMap.put("Period", 0.0698);

        switch (choose) {
            case 1:
                LoadData train_Data = new LoadData();
                LoadData test_Data = new LoadData();
                loadData(train_Data, test_Data, choose, trainDataName, testDataName);
                //normalize
                knn.nomarlize(minmaxMap, test_Data);
                knn.nomarlize(minmaxMap, train_Data);
                double accuracy = checkAccuracy(train_Data, knn, 5, weightedMap, matrixMap);
                System.out.println("Accuracy is " + accuracy);
                for (DataSet dataSet : testData.getDataSetList()) {
                    String label = knn.KNN(trainData, dataSet, weightedMap, matrixMap, 5);
                    System.out.println(label);
                }
                break;
            case 2:
                train_Data = new LoadData();
                test_Data = new LoadData();
                loadData(train_Data, test_Data, choose, trainDataName, testDataName);
                //normalize
                knn.nomarlize(minmaxMap, test_Data);
                knn.nomarlize(minmaxMap, train_Data);
                double mse = checkMSE(train_Data, knn, 5, weightedMap, matrixMap);
                System.out.println("MSE is " + mse);
                for (DataSet dataSet : testData.getDataSetList()) {
                    String score = knn.KNN(trainData, dataSet, weightedMap, matrixMap, 5);
                    System.out.println(score);
                }
                break;
        }
    }

    private static void loadData(LoadData trainData, LoadData testData, int choose, String trainDataName, String testDataName) {
        switch (choose) {
            case 1:
                testData.loadData(testDataName);
                trainData.loadData(trainDataName);
                break;
            case 2:
                testData.loadData(testDataName);
                trainData.loadData(trainDataName);
                break;
        }
    }

    // shuffle list for validation
    private static List<DataSet> shuffle(LoadData data) {
        List<DataSet> shuffle = new ArrayList<>(data.getDataSetList());
        Collections.shuffle(shuffle);
        return shuffle;
    }

    private static double checkMSE(LoadData loadData, KNN knn, int k,
                                   Map<String, Double> weight,
                                   Map<String, double[][]> matrixMap) {

        loadData.setData(shuffle(loadData));
        List<DataSet> shuffleList = loadData.getDataSetList();

        double mse = 0;
        double currentMse;
        String targetType = loadData.getNameList().get(loadData.getNameList().size() - 1); // for now => label

        // folding validation
        for (int i = 0; i < k; i++) {
            currentMse = 0;
            List<DataSet> testData = new ArrayList<>();
            List<DataSet> trainData = new ArrayList<>(loadData.getDataSetList());
            int fold_num = shuffleList.size() / k;
            // get k-th fold
            for (int j = fold_num * (i + 1) - 1; j >= fold_num * i; j--) {
                testData.add(trainData.remove(j));
            }
            // update load data to folded train data
            loadData.setData(trainData);
            for (DataSet dataSet : testData) {
                double targetValue = dataSet.getValue(targetType);
                double predictValue = Double.parseDouble(knn.KNN(loadData, dataSet, weight, matrixMap, 5));
                currentMse += Math.pow(predictValue - targetValue, 2);
            }
            //update load data back to the shuffled list
            if (i != k - 1) loadData.setData(shuffleList);
            mse += currentMse / testData.size();
        }
        return mse / k;
    }

    private static double checkAccuracy(LoadData loadData, KNN knn, int k,
                                        Map<String, Double> weight,
                                        Map<String, double[][]> matrixMap) {
        loadData.setData(shuffle(loadData));
        List<DataSet> shuffleList = loadData.getDataSetList();

        double accuracy = 0;
        double correct_count;

        String targetType = loadData.getNameList().get(loadData.getNameList().size() - 1); // for now => label
        Map<String, Double> map = loadData.getAttributeMap().get(targetType).getTypeValue();

        // folding validation
        for (int i = 0; i < k; i++) {
            correct_count = 0;
            List<DataSet> testData = new ArrayList<>();
            List<DataSet> trainData = new ArrayList<>(loadData.getDataSetList());
            int fold_num = shuffleList.size() / k;
            // get k-th fold
            for (int j = fold_num * (i + 1) - 1; j >= fold_num * i; j--) {
                testData.add(trainData.remove(j));
            }
            // update load data to folded train data
            loadData.setData(trainData);
            for (DataSet dataSet : testData) {
                if (dataSet.getValue(targetType).equals(map.get(knn.KNN(loadData, dataSet, weight, matrixMap, 5)))) {
                    correct_count++;
                }
            }
            //update load data back to the shuffled list
            if (i != k - 1) loadData.setData(shuffleList);
            accuracy += correct_count / testData.size();
        }
        return accuracy / k;
    }
}
