package vat.home.card.service;

import java.util.List;

/**
 * 사업용신용카드 기업회원 조회 Service
 */
public interface VatCardPurchaseService {

    List<VatCardPurchaseVO> selectEntrprsMberList(
        VatCardPurchaseVO searchVO
    ) throws Exception;

    int selectEntrprsMberListTotCnt(
        VatCardPurchaseVO searchVO
    ) throws Exception;

    /**
     * 선택한 기업회원 정보를 서버에서 다시 조회한다.
     * 화면에서 비밀번호를 전달받지 않는다.
     */
    VatCardPurchaseVO selectEntrprsMberLoginInfo(
        String entrprsmberId
    ) throws Exception;
}
