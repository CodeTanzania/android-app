package com.github.codetanzania.api;

import com.github.codetanzania.api.model.ApiServiceRequest;
import com.github.codetanzania.model.Service;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.model.Status;

import java.util.ArrayList;

/**
 * This is used to create app objects from what is returned from the server.
 */

public class ApiModelConverter {

    private static ServiceRequest convert(ApiServiceRequest apiRequest) {
        ServiceRequest request = new ServiceRequest();
        request.id = apiRequest._id;
        request.code = apiRequest.code;
        request.description = apiRequest.description;
        request.reporter = apiRequest.reporter;
        request.service = new Service(apiRequest.service.code,
                apiRequest.service.name, apiRequest.service.color);
        request.jurisdiction = apiRequest.jurisdiction.name;
        request.address = apiRequest.address;
        request.longitude = apiRequest.longitude;
        request.latitude = apiRequest.latitude;
        @Status.Type int type = "OPEN".equalsIgnoreCase(apiRequest.status.name) ? Status.OPEN : Status.CLOSED;
        request.status = new Status(type, apiRequest.status.color);
        request.createdAt = apiRequest.createdAt;
        request.updatedAt = apiRequest.updatedAt;
        request.resolvedAt = apiRequest.resolvedAt;
        request.setAttachments(apiRequest.attachments);
        request.comments = apiRequest.comments;
        return request;
    }

    public static ArrayList<ServiceRequest> convert(ApiServiceRequest[] apiRequests) {
        ArrayList<ServiceRequest> converted = new ArrayList<>();
        for (ApiServiceRequest apiRequest : apiRequests) {
            converted.add(convert(apiRequest));
        }
        return converted;
    }

}
