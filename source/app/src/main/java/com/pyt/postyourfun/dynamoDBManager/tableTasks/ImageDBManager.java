package com.pyt.postyourfun.dynamoDBManager.tableTasks;

import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.pyt.postyourfun.dynamoDBClass.ImageMapper;
import com.pyt.postyourfun.dynamoDBClass.ImageQueryMapper;
import com.pyt.postyourfun.dynamoDBClass.ParkInformationMapper;
import com.pyt.postyourfun.dynamoDBClass.ParkMapper;
import com.pyt.postyourfun.dynamoDBClass.UserMapper;
import com.pyt.postyourfun.dynamoDBManager.DynamoDBManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Simon on 7/13/2015.
 */
public class ImageDBManager extends DynamoDBManager {

    private String TAG = "ImageDBManager";

    public static ImageMapper getImage(String image_ID){
        AmazonDynamoDBClient ddb = clientManager.ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            Map<String, Condition> scanFilter = new HashMap<String, Condition>();

            // Condition1: DeviceId
            Condition scanCondition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withS(image_ID));
            scanFilter.put("ImageId", scanCondition);
            scanExpression.setScanFilter(scanFilter);

            List<ImageMapper> results = mapper.scan(ImageMapper.class, scanExpression);
            if (results.size() > 0){
                ImageMapper imageMapper = results.get(0);
                return imageMapper;
            }else {
                return null;
            }
        } catch (AmazonServiceException ex) {
            clientManager.wipeCredentialsOnAuthError(ex);
        }
        return null;
    }
}
