package com.monew.monew_api.useractivity.service;

import com.monew.monew_api.useractivity.dto.UserActivityDto;


public interface UserActivityService {

    UserActivityDto getUserActivity(String userId);

    /**
     * 사용자 활동내역 조회 (단일 쿼리)
     * PostgreSQL에서 직접 조회
     */
    UserActivityDto getUserActivitySingleQuery(String userId);
}