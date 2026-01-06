package cloud.marton.hostup_dns_client.exceptions;

import cloud.marton.hostup_dns_client.model.ApiResponse;

public class LegoArgumentResult {
    private final ApiResponse apiResponse;
    private final String message;
    private final boolean success;

    public LegoArgumentResult(boolean success, String message, ApiResponse apiResponse) {
        this.success = success;
        this.message = message;
        this.apiResponse = apiResponse;
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
