package com.eyeshare.Dag.functionality;

import com.eyeshare.Dag.profiles.Operation;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import com.eyeshare.Dag.profiles.OpType;
import com.eyeshare.Dag.profiles.Profile;

public class ExcelReformatter extends ExcelHandler {

    private Profile profile;

    public ExcelReformatter(String sourceFilePath, Profile profile) throws IOException {
        super(sourceFilePath, profile.getTemplatePath());
        this.profile = profile;
    }


    /**
     * Applies the given profile to the source workbook using the template workbook if it is provided.
     * @param profile
     */
    public void applyProfile() {
        for (Operation<?> operation : profile.getOperations()) {
            OpType type = operation.getType();
            Map<String, ?> parameters = operation.getParameters();
    
            System.out.println("Operation type: " + type);
            System.out.println("Parameters: " + parameters);
    
            switch (type) {
                case COPY_ROWS:
                    copyRows(((Double) parameters.get("srcSheet")).intValue(),
                    ((Double) parameters.get("dstSheet")).intValue(),
                    ((Double) parameters.get("startRow")).intValue(),
                    ((Double) parameters.get("numRows")).intValue());
                    break;
                case COPY_COLUMN:
                    copyColumn(((Double) parameters.get("srcSheet")).intValue(),
                    ((Double) parameters.get("srcCol")).intValue(),
                    ((Double) parameters.get("dstSheet")).intValue(), 
                    ((Double) parameters.get("dstCol")).intValue(),
                    1);
                    break;
                case COPY_SPLIT_ROW:
                System.out.println("colMap: " + parameters.get("colMap"));
                Map<?, ?> rawMap = (Map<?, ?>) parameters.get("colMap");
                
                Map<Integer, Integer> integerMap = new HashMap<>();
                if (rawMap != null) {
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                        System.out.println("Key type: " + entry.getKey().getClass() + ", Value type: " + entry.getValue().getClass());
                        integerMap.put(((Number) entry.getKey()).intValue(), ((Number) entry.getValue()).intValue());
                    }
                }

                
                copySplitRow(((Number) parameters.get("srcSheet")).intValue(),
                        ((Number) parameters.get("dstSheet")).intValue(),
                        ((Number) parameters.get("startRow")).intValue(),
                        integerMap,
                        (boolean) parameters.get("includeHeaders"),
                        ((Number) parameters.get("headerCol")).intValue());
                break;
                // Add other operation types here
            }
        }
    }
}
    
    
    

