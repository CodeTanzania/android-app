package com.github.codetanzania.utils;

import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.model.Status;
import com.github.codetanzania.util.ServiceRequestsUtil;
import com.ibm.icu.text.SimpleDateFormat;

import junit.framework.Assert;

/**
 * This shows responses as they are expected from the server, and is used for mocking in tests.
 */

public class Mocks {
    final static public String validRequest =
            "{\"servicerequests\": [\n" +
            "{\n" +
            "    \"jurisdiction\": {\n" +
            "        \"code\": \"H\",\n" +
            "        \"name\": \"HQ\",\n" +
            "        \"phone\": \"255714999888\",\n" +
            "        \"email\": \"N/A\",\n" +
            "        \"domain\": \"dawasco.org\",\n" +
            "        \"_id\": \"592029e5e8dd8e00048c184b\",\n" +
            "        \"longitude\": 0,\n" +
            "        \"latitude\": 0,\n" +
            "        \"uri\": \"https://dawasco.herokuapp.com/jurisdictions/592029e5e8dd8e00048c184b\"\n" +
            "    },\n" +
            "    \"group\": {\n" +
            "        \"code\": \"N\",\n" +
            "        \"name\": \"Non Commercial\",\n" +
            "        \"color\": \"#960F1E\",\n" +
            "        \"_id\": \"592029e6e8dd8e00048c184d\",\n" +
            "        \"uri\": \"https://dawasco.herokuapp.com/servicegroups/592029e6e8dd8e00048c184d\"\n" +
            "    },\n" +
            "    \"service\": {\n" +
            "        \"code\": \"LW\",\n" +
            "        \"name\": \"Lack of Water\",\n" +
            "        \"color\": \"#960F1E\",\n" +
            "        \"_id\": \"592029e6e8dd8e00048c1852\",\n" +
            "        \"uri\": \"https://dawasco.herokuapp.com/services/592029e6e8dd8e00048c1852\"\n" +
            "    },\n" +
            "    \"call\": {\n" +
            "        \"startedAt\": \"2017-06-18T15:49:48.483Z\",\n" +
            "        \"endedAt\": \"2017-06-18T15:49:48.483Z\",\n" +
            "        \"duration\": 0\n" +
            "    },\n" +
            "    \"reporter\": {\n" +
            "        \"name\": \"Lally Elias\",\n" +
            "        \"phone\": \"255714095061\"\n" +
            "    },\n" +
            "    \"operator\": {\n" +
            "        \"name\": \"Lally Elias\",\n" +
            "        \"phone\": \"255714095061\",\n" +
            "        \"_id\": \"592029e6e8dd8e00048c185d\",\n" +
            "        \"permissions\": [],\n" +
            "        \"email\": \"lallyelias87@gmail.com\",\n" +
            "        \"uri\": \"https://dawasco.herokuapp.com/parties/592029e6e8dd8e00048c185d\"\n" +
            "    },\n" +
            "    \"code\": \"HLW170026\",\n" +
            "    \"description\": \"Test New Apk\",\n" +
            "    \"address\": \"Some address\",\n" +
            "    \"method\": \"Call\",\n" +
            "    \"location\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [120, 110]\n" +
            "    },\n" +
            "    \"status\": {\n" +
            "        \"name\": \"Open\",\n" +
            "        \"weight\": -5,\n" +
            "        \"color\": \"#0D47A1\",\n" +
            "        \"_id\": \"592029e5e8dd8e00048c180d\",\n" +
            "        \"createdAt\": \"2017-05-20T11:35:01.059Z\",\n" +
            "        \"updatedAt\": \"2017-05-20T11:35:01.059Z\",\n" +
            "        \"uri\": \"https://dawasco.herokuapp.com/statuses/592029e5e8dd8e00048c180d\"\n" +
            "    },\n" +
            "    \"priority\": {\n" +
            "        \"name\": \"Normal\",\n" +
            "        \"weight\": 5,\n" +
            "        \"color\": \"#4CAF50\",\n" +
            "        \"_id\": \"592029e5e8dd8e00048c1817\",\n" +
            "        \"createdAt\": \"2017-05-20T11:35:01.601Z\",\n" +
            "        \"updatedAt\": \"2017-05-20T11:35:01.601Z\",\n" +
            "        \"uri\": \"https://dawasco.herokuapp.com/priorities/592029e5e8dd8e00048c1817\"\n" +
            "    },\n" +
            "    \"attachments\": [],\n" +
            "    \"ttr\": 0,\n" +
            "    \"_id\": \"5946a11c593d370004dbfcf3\",\n" +
            "    \"createdAt\": \"2017-06-18T15:49:48.571Z\",\n" +
            "    \"updatedAt\": \"2017-06-18T15:55:58.183Z\",\n" +
            "    \"ttrSeconds\": 0,\n" +
            "    \"ttrMinutes\": 0,\n" +
            "    \"ttrHours\": 0,\n" +
            "    \"longitude\": 120,\n" +
            "    \"latitude\": 110,\n" +
            "    \"uri\": \"https://dawasco.herokuapp.com/servicerequests/5946a11c593d370004dbfcf3\"\n" +
            "}" +
            "]}";

    public static void isSame(ServiceRequest request) {
        Assert.assertEquals("Id should be the same", "5946a11c593d370004dbfcf3", request.id);
        Assert.assertEquals("Code should be the same", "HLW170026", request.code);
        Assert.assertEquals("Description should be the same", "Test New Apk", request.description);
        Assert.assertEquals("Reporter should be the same", "Lally Elias", request.reporter.name);
        Assert.assertEquals("Service code should be the same", "LW", request.service.code);
        Assert.assertEquals("Service name should be the same", "Lack of Water", request.service.name);
        Assert.assertEquals("Service color should be the same", "#960F1E", request.service.color);
        Assert.assertEquals("Jurisdiction name should be the same", "HQ", request.jurisdiction);
        Assert.assertEquals("Address name should be the same", "Some address", request.address);
        Assert.assertEquals("Longitude name should be the same", 120f, request.longitude);
        Assert.assertEquals("Latitude name should be the same", 110f, request.latitude);
        Assert.assertEquals("Status name should be the same", Status.OPEN, request.status.type);
        Assert.assertEquals("Status color should be the same", "#0D47A1", request.status.color);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Assert.assertEquals("Date created should be the same", "2017-06-18T15:49:48", formatter.format(request.createdAt));
        Assert.assertEquals("Date updated should be the same", "2017-06-18T15:55:58", formatter.format(request.updatedAt));
        Assert.assertEquals("Date resolved should be the same", null, request.resolvedAt);
        Assert.assertEquals("Attachments should be the same", 0, request.attachments.size());
        //Assert.assertEquals("Comments should be the same", 0, request.comments.size());
    }
}