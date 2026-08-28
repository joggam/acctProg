package egovframework.com.uss.umt.service;

import java.util.List;

/**
 * 기업회원 사업자등록번호 1:N 관리 Service.
 */
public interface EgovEntrprsBizrManageService {

    List<String> selectBizrnoList(String entrprsmberId) throws Exception;

    void replaceBizrnoList(String entrprsmberId, List<String> bizrnoList) throws Exception;

    void deleteBizrnoByUniqId(String uniqId) throws Exception;
}
