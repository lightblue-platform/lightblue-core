package com.redhat.lightblue.mediator;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.stopwatch.SizeCalculator;

public class ResponsePayloadSizeCalculator implements SizeCalculator<Response>{

    @Override
    public int size(Response response) {
        return JsonUtils.size(response.getEntityData());
    }

}
