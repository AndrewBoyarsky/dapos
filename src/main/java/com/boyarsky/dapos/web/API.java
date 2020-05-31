package com.boyarsky.dapos.web;

import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.service.account.NotFoundException;

public class API {
    public static final String REST_ROOT_URL = "/api/rest/v1";

    public static Pagination initPagination(Pagination pagination) {
        if (pagination == null) {
            return new Pagination();
        }
        return pagination;
    }

    public static void throwNotFoundExceptionIfNull(Object object, String message) {
        if (object == null) {
            throw new NotFoundException(message);
        }
    }
}
