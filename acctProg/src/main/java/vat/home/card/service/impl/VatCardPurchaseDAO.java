package vat.home.card.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import vat.home.card.service.VatCardPurchaseVO;

@Repository("vatCardPurchaseDAO")
public class VatCardPurchaseDAO extends EgovAbstractMapper {

    @Resource(name = "egov.sqlSession")
    public void setSqlSessionFactory(SqlSessionFactory sqlSession) {
        super.setSqlSessionFactory(sqlSession);
    }

    public List<VatCardPurchaseVO> selectEntrprsMberList(VatCardPurchaseVO searchVO) {
        return selectList("vatCardPurchaseDAO.selectEntrprsMberList", searchVO);
    }

    public int selectEntrprsMberListTotCnt(VatCardPurchaseVO searchVO) {
        Integer result = selectOne(
                "vatCardPurchaseDAO.selectEntrprsMberListTotCnt", searchVO);
        return result == null ? 0 : result.intValue();
    }

    public VatCardPurchaseVO selectEntrprsMberLoginInfo(Long bizrSeq) {
        return selectOne(
                "vatCardPurchaseDAO.selectEntrprsMberLoginInfo", bizrSeq);
    }
}
