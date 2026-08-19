package vat.home.card.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import vat.home.card.service.VatCardPurchaseService;
import vat.home.card.service.VatCardPurchaseVO;

/**
 * 사업용신용카드 기업회원 조회 ServiceImpl
 */
@Service("vatCardPurchaseService")
public class VatCardPurchaseServiceImpl
        implements VatCardPurchaseService {

    @Resource(name = "vatCardPurchaseDAO")
    private VatCardPurchaseDAO vatCardPurchaseDAO;

    @Override
    public List<VatCardPurchaseVO> selectEntrprsMberList(
            VatCardPurchaseVO searchVO) throws Exception {

        return vatCardPurchaseDAO.selectEntrprsMberList(searchVO);
    }

    @Override
    public int selectEntrprsMberListTotCnt(
            VatCardPurchaseVO searchVO) throws Exception {

        return vatCardPurchaseDAO.selectEntrprsMberListTotCnt(searchVO);
    }

    @Override
    public VatCardPurchaseVO selectEntrprsMberLoginInfo(
            String entrprsmberId) throws Exception {

        return vatCardPurchaseDAO.selectEntrprsMberLoginInfo(
            entrprsmberId
        );
    }
}
