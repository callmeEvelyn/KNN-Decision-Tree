import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shulin Huang
 * @date 4/4/18
 **/

// Helper class to store @data
public class DataSet{


    // mapKey: attribute name, mapValue: attributeSet
    private Map<String, AttributeSet> attributeMap;
    // mapKey: attribute name, mapValue: attribute value
    private Map<String, Double> valueMap;
    private double similarity;

    // Getters & Setters
    public Map<String, AttributeSet> getAttributeMap() {
        return attributeMap;
    }

    public Map<String, Double > getValueMap() {
        return valueMap;
    }

    public Double getValue(String key){
        return valueMap.get(key);
    }


    public Double getSimilarity(){
        return similarity;
    }

    public void setSimilarity(Double similarity){
        this.similarity = similarity;
    }

    public void setValue(String key, Double value){
        valueMap.put(key, value);
    }

    // @data: student,spend>saving,3,47,15.71,3.4022,C1
    public DataSet(String text, Map<String, AttributeSet> attributeMap, List<String> list){
        this.attributeMap = attributeMap;
        String[] texts = text.split(",");
        valueMap = new HashMap<String, Double>();
        Double tmp;
        for(int i=0; i<list.size(); i++){
//            System.out.println(list.get(i));
            if(attributeMap.get(list.get(i)).isRealNum()){
                tmp = Double.parseDouble(texts[i]);
            } else {
                Map<String, Double> tmpMap = attributeMap.get(list.get(i)).getTypeValue();
//                System.out.println(tmpMap);
                //{doctor=4, professor=3, student=0, librarian=2, engineer=1}
                //{spend>>saving=3, spend<<saving=0, spend>saving=2, spend<saving=1}
                //{C3=2, C4=3, C5=4, C1=0, C2=1}
                tmp = tmpMap.get(texts[i]);
            }
            valueMap.put(list.get(i), tmp);
        }
    }
    @Override
    public String toString() {
        return "[Data]: " + valueMap;
    }

    public static void main(String[] args) {
        AttributeSet attributeA = new AttributeSet("Type","{student,engineer,librarian,professor,doctor}");
        AttributeSet attributeB = new AttributeSet("LifeStyle", "{spend<<saving,spend<saving,spend>saving,spend>>saving}");
        AttributeSet attributeC = new AttributeSet("Vacation", "real");
        AttributeSet attributeD = new AttributeSet("eCredit", "real");
        AttributeSet attributeE = new AttributeSet("salary", "real");
        AttributeSet attributeF = new AttributeSet("property", "real");
        AttributeSet attributeG = new AttributeSet("label", "{C1,C2,C3,C4,C5}");
        List<String> list = new ArrayList<String>();
        list.add("Type");
        list.add("LifeStyle");
        list.add("Vacation");
        list.add("eCredit");
        list.add("salary");
        list.add("property");
        list.add("label");
        String text = "student,spend>saving,3,47,15.71,3.4022,C1";
        Map<String, AttributeSet> map = new HashMap<>();
        map.put(attributeA.getName(), attributeA);
        map.put(attributeB.getName(), attributeB);
        map.put(attributeC.getName(), attributeC);
        map.put(attributeD.getName(), attributeD);
        map.put(attributeE.getName(), attributeE);
        map.put(attributeF.getName(), attributeF);
        map.put(attributeG.getName(), attributeG);
        DataSet data = new DataSet(text, map, list);
        System.out.println(data);
    }
}
