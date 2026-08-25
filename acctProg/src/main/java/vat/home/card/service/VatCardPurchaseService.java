package vat.home.card.service;

import java.io.File;
import java.util.List;

public interface VatCardPurchaseService {

    List<VatCardPurchaseVO> selectEntrprsMberList(VatCardPurchaseVO searchVO) throws Exception;

    int selectEntrprsMberListTotCnt(VatCardPurchaseVO searchVO) throws Exception;

    VatCardPurchaseVO selectEntrprsMberLoginInfo(Long bizrSeq) throws Exception;

    File downloadHometaxExcel(Long bizrSeq, int year, int quarter) throws Exception;

    List<File> downloadMergedHometaxExcel(
            String[] selectedBizrSeq, int year, int quarter) throws Exception;
}
