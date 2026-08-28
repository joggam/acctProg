package vat.home.card.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vat.home.card.service.VatCardCondition1KeywordVO;
import vat.home.card.service.VatCardCondition1Service;
import vat.home.card.service.VatCardCondition1VO;

@Service("vatCardCondition1Service")
public class VatCardCondition1ServiceImpl extends EgovAbstractServiceImpl implements VatCardCondition1Service {
    @Resource(name = "vatCardCondition1DAO")
    private VatCardCondition1DAO dao;

    public List<VatCardCondition1VO> selectCondition1List(VatCardCondition1VO vo) { return dao.selectCondition1List(vo); }
    public int selectCondition1ListTotCnt(VatCardCondition1VO vo) { return dao.selectCondition1ListTotCnt(vo); }
    public VatCardCondition1VO selectCondition1(Long seq) { return dao.selectCondition1(seq); }
    public void insertCondition1(VatCardCondition1VO vo) { dao.insertCondition1(vo); }
    public void updateCondition1(VatCardCondition1VO vo) { dao.updateCondition1(vo); }
    public void deleteCondition1(Long seq) { dao.deleteCondition1(seq); }
    @Transactional
    public void saveCondition1Excel(VatCardCondition1VO vo) { dao.upsertCondition1(vo); }

    public List<VatCardCondition1KeywordVO> selectKeywordList(VatCardCondition1KeywordVO vo) { return dao.selectKeywordList(vo); }
    public int selectKeywordListTotCnt(VatCardCondition1KeywordVO vo) { return dao.selectKeywordListTotCnt(vo); }
    public VatCardCondition1KeywordVO selectKeyword(Long seq) { return dao.selectKeyword(seq); }
    public void insertKeyword(VatCardCondition1KeywordVO vo) { dao.insertKeyword(vo); }
    public void updateKeyword(VatCardCondition1KeywordVO vo) { dao.updateKeyword(vo); }
    public void deleteKeyword(Long seq) { dao.deleteKeyword(seq); }
    public VatCardCondition1VO selectMatch(String bizcnd, String induty) { return dao.selectMatch(bizcnd, induty); }
    public VatCardCondition1KeywordVO selectKeywordMatch(String keywordType, String targetType, String keyword) { return dao.selectKeywordMatch(keywordType, targetType, keyword); }
}
