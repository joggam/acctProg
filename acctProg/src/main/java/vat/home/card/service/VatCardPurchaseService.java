package vat.home.card.service;

import java.io.File;
import java.util.List;

public interface VatCardPurchaseService {

    List<VatCardPurchaseVO> selectEntrprsMberList(VatCardPurchaseVO searchVO) throws Exception;

    int selectEntrprsMberListTotCnt(VatCardPurchaseVO searchVO) throws Exception;

    VatCardPurchaseVO selectEntrprsMberLoginInfo(String entrprsmberId) throws Exception;

    File downloadHometaxExcel(String entrprsmberId, int year, int quarter) throws Exception;
}
