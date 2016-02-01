package com.pyt.postyourfun.dynamoDBManager.tableTasks;

import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.pyt.postyourfun.dynamoDBClass.ImageMapper;
import com.pyt.postyourfun.dynamoDBClass.ImageQueryMapper;
import com.pyt.postyourfun.dynamoDBClass.ParkInformationMapper;
import com.pyt.postyourfun.dynamoDBManager.DynamoDBManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Simon on 7/14/2015.
 */
public class ImageQueryDBManager extends DynamoDBManager {
	private static String TAG = "ImageQueryDBManager";

	public static ImageQueryMapper getImage(String device_id, String display_id) {

		AmazonDynamoDBClient ddb = clientManager.ddb();
		DynamoDBMapper mapper = new DynamoDBMapper(ddb);

		try {
			DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

			Map<String, Condition> scanFilter = new HashMap<String, Condition>();

			// Condition1: DeviceId
			Condition scanCondition =
					new Condition().withComparisonOperator(ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(device_id));
			scanFilter.put("DeviceId", scanCondition);
			scanExpression.setScanFilter(scanFilter);

			// Condition2: DisplayId
			Condition scanCondition1 =
					new Condition().withComparisonOperator(ComparisonOperator.EQ.toString()).withAttributeValueList(new AttributeValue().withS(display_id));
			scanFilter.put("DisplayId", scanCondition1);

			scanExpression.setScanFilter(scanFilter);

			List<ImageQueryMapper> results = mapper.scan(ImageQueryMapper.class, scanExpression);
			if (results.size() > 0) {
				ImageQueryMapper imageQueryMapper = results.get(0);
				Log.d("Imagescan result:", imageQueryMapper.getImage_id());
				return imageQueryMapper;
			} else {
				return null;
			}
		} catch (AmazonServiceException ex) {
			clientManager.wipeCredentialsOnAuthError(ex);
		}
		return null;
	}
}
