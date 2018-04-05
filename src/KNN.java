import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Shulin Huang
 * @date 4/4/18
 **/
public class KNN {

    // Liner Normalization
    // Step1 Find max & min values for each attribute
    /**
     * @param loadData data loaded from file
     * @return return min&max value of the attribute(real num attribute)
     * map-key: attribute name; map-value: list(min, max)
     */
    public Map<String, List<Double>> getMinMax(LoadData loadData) {
        Map<String, AttributeSet> attributeSetMap = loadData.getAttributeMap();
        Map<String, List<Double>> map = new HashMap<>();

        for(Map.Entry<String, AttributeSet> entry: attributeSetMap.entrySet()){
            double min = Integer.MAX_VALUE;
            double max = Integer.MIN_VALUE;
            if(entry.getValue().isRealNum()){
                for(DataSet dataSet: loadData.getDataSetList()){
                    max = Math.max(max, dataSet.getValue(entry.getKey()));
                    min = Math.min(min, dataSet.getValue(entry.getKey()));
                }
                map.put(entry.getKey(), new ArrayList<>());
                map.get(entry.getKey()).add(min);
                map.get(entry.getKey()).add(max);
            }
        }
        return map;
    }

    // Step2 Normalize each attribute (actual-min)/(max-min)
    /**
     * Nomarlize.
     *
     * @param minmaxMap the minmax map
     * @param loadData  the load data
     */
    public void nomarlize(Map<String,List<Double>> minmaxMap, LoadData loadData)  {
        for(Map.Entry<String, List<Double>> entry: minmaxMap.entrySet()) {
            for(DataSet dataSet: loadData.getDataSetList()){
                // res should fall in 0-1
                double res = ((dataSet.getValue(entry.getKey()))-entry.getValue().get(0))
                        /(entry.getValue().get(1)-entry.getValue().get(0));
                dataSet.setValue(entry.getKey(), res);
            }
        }
    }

    // Step3: calculate similarity based on formula
    // for non- numeric attributes, use the corresponding similarity value from the similarity matrix.
    // for numeric attributes, use Inverse Euclidean distance (L2)
    /**
     * @param dataSet1
     * @param dataSet2
     * @param target the target attribute
     * @param weightMap weight for different attributes
     * @param matirxMap similarity marix for attributes that don't have real num
     * @return
     */
    public double getSimilarity(DataSet dataSet1, DataSet dataSet2, String target, Map<String, Double> weightMap,
                                Map<String, double[][]> matirxMap){
        double res = 0;
        for(Map.Entry<String, AttributeSet> entry: dataSet1.getAttributeMap().entrySet()){
            String key = entry.getKey();
            if(!target.equals(key)){
                AttributeSet attributeSet = entry.getValue();
                double weight = weightMap.get(key);
                if(attributeSet.isRealNum()) {
                    res += Math.pow(dataSet1.getValue(key)-dataSet2.getValue(key), 2) * weight;
                } else {
//                    System.out.println(key);
                    double[][] calMatrix = matirxMap.get(key);
                    int data1_index = dataSet1.getValue(key).intValue();
                    int data2_index = dataSet2.getValue(key).intValue();
                    res += Math.pow((1-calMatrix[data1_index][data2_index]), 2) * weight;
//                    System.out.println(res);
                }
            }
        }
        return 1/Math.sqrt(res);
    }

    //Step4: calculate class for numeric attributes => average of top k datasets
    /**
     * @param pq
     * @param target the target attribute
     * @param k
     * @return the total similarity/k
     */
    public double calClass(PriorityQueue<DataSet> pq, String target, int k){
        double res = 0;
        for(int i=0; i<k; i++){
            DataSet dataSet = pq.poll();
            res+=dataSet.getValue(target);
        }
        return res/k;
    }

    // Step4 getClass for non-numeric attributes => find the max value
    /**
     * @param pq store the top k nearest datasets
     * @param map the map to match
     * @param target the target attribute
     * @param k
     * @return the class of the target
     */
    public String getClass(PriorityQueue<DataSet> pq, Map<String, Double> map, String target, int k){

        Map<String, Double> similarityMap = new HashMap<>();
        for(int i=0; i<k; i++){
            DataSet dataSet = pq.poll();
            String classType = null;
            for(Map.Entry<String, Double> entry: map.entrySet()){
                if(entry.getValue() == dataSet.getValue(target)) {
                    classType = entry.getKey();
//                    System.out.println(classType);
                }
            }

            // sum up similarity of all data sets
            double similarity = dataSet.getSimilarity();
            similarityMap.put(classType, similarityMap.getOrDefault(classType, 0.0) + similarity);
        }

        String res;
        // find the max value to decide the class type
        List<Map.Entry<String,Double>> list = new ArrayList<>(similarityMap.entrySet());
        Collections.sort(list, ((o1, o2) -> o2.getValue()-o1.getValue()>0?1:-1));
        res = list.get(0).getKey();
        return res;
    }


    /**
     * Instantiates a new Knn.
     *
     * @param train    the train data load from file
     * @param testSet  test data set
     * @param weightMap   the weightmap for attributes
     * @param marixMap the marix map for non-numeric attributes
     * @param k        the k
     */
    public String KNN(LoadData train, DataSet testSet, Map<String, Double> weightMap, Map<String, double[][]> marixMap, int k){
        String tagetType = train.getNameList().get(train.getNameList().size()-1); // set targettype="label"
        PriorityQueue<DataSet> pq = new PriorityQueue<>(k, ((o1, o2) -> o2.getSimilarity()-o1.getSimilarity()>0?1:-1));
        // Get the top K data sets with high similarity
        List<DataSet> list = train.getDataSetList();
        for(int i=0; i<list.size(); i++){
            DataSet dataSet1 = list.get(i);
            double sim = getSimilarity(testSet, dataSet1, tagetType, weightMap, marixMap);
            if(pq.size()<k) {
                pq.add(dataSet1);
            } else {
                if(pq.peek().getSimilarity()<sim){
                    pq.poll();
                    pq.add(dataSet1);
                }
            }
            dataSet1.setSimilarity(sim);
        }

        double score;
        String res;
        Map<String, Double> map = train.getAttributeMap().get(tagetType).getTypeValue();
        //System.out.println(map);// get {C3=2, C4=3, C5=4, C1=0, C2=1}
        if(train.getAttributeMap().get(tagetType).isRealNum()) {
            score = calClass(pq, tagetType, k);
            testSet.setValue(tagetType, score);
            res = String.valueOf(score);
        } else{
            res = getClass(pq, map, tagetType, k);
            testSet.setValue(tagetType, map.get(res)); //{C3=2, C4=3, C5=4, C1=0, C2=1}, set value to num
        }

        return res;
    }

    public static void main(String[] args){
        LoadData loadData = new LoadData();
        loadData.loadData("trainProdSelection.arff");
        KNN knn = new KNN();
        System.out.println(knn.getMinMax(loadData));
    }
}
