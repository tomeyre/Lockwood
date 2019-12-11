package com.example.lockwood.techTest.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class DynamoDb {

    private Table table;
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private DynamoDBMapper mapper;

    {
        client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1).build();
        dynamoDB = new DynamoDB(client);
        table = dynamoDB.getTable("ticket");
        mapper = new DynamoDBMapper(client);
    }

    public boolean createId(String ticket) {
        Item item=new Item()
                .withPrimaryKey("ticket",ticket)
                .withBoolean("valid",true);
        PutItemSpec itemSpec=new PutItemSpec()
                .withItem(item)
                .withConditionExpression("attribute_not_exists(#ps)")
                .withNameMap(new NameMap()
                        .with("#ps","ticket"));
        try{
            table.putItem(itemSpec);
            return true;
        }
        catch(ConditionalCheckFailedException e){
            logServiceError(e);
            return false;
        }
        catch (AmazonClientException ace) {
            logClientError(ace);
            return false;
        }
    }

    public Long getTotalTickets() {
        Long totalItemCount = 0l;
        ScanResult result = new ScanResult();
        do {
            ScanRequest req = new ScanRequest();
            req.setTableName("ticket");

            if (result != null) {
                req.setExclusiveStartKey(result.getLastEvaluatedKey());
            }

            result = client.scan(req);

            totalItemCount += result.getItems().size();

        } while (result.getLastEvaluatedKey() != null);

        System.out.println("Result size: " + totalItemCount);
        return totalItemCount;
    }

    public boolean findValidTicket(String ticket) {
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":val1", ticket);
        ItemCollection<ScanOutcome> items = null;
        try {
            items = table.scan("ticket = :val1",
                    "ticket, valid",
                    null,
                    expressionAttributeValues);
        } catch (AmazonServiceException ase) {
            logServiceError(ase);
        } catch (AmazonClientException ace) {
            logClientError(ace);
        }
        if (items != null) {
            Iterator<Item> iterator = items.iterator();
            if (iterator.hasNext()) {
                return iterator.next().getBoolean("valid");
            }
        }
        return false;
    }

    public boolean invalidateTicket(String ticket) {
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("ticket", ticket)
                .withConditionExpression("ticket = :ticket")
                .withUpdateExpression("set valid = :valid")
                .withValueMap(new ValueMap().withBoolean(":valid", false).withString( ":ticket", ticket))
                .withReturnValues(ReturnValue.UPDATED_NEW);

        try {
            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            if(outcome.getItem() == null){
                return false;
            }
            return true;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private void logServiceError(AmazonServiceException ase){
        System.err.println("Could not complete operation");
        System.err.println("Error Message:  " + ase.getMessage());
        System.err.println("HTTP Status:    " + ase.getStatusCode());
        System.err.println("AWS Error Code: " + ase.getErrorCode());
        System.err.println("Error Type:     " + ase.getErrorType());
        System.err.println("Request ID:     " + ase.getRequestId());
    }

    private void logClientError(AmazonClientException ace){
        System.err.println("Internal error occurred communicating with DynamoDB");
        System.out.println("Error Message:  " + ace.getMessage());
    }
}