package vat.home.card.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.stereotype.Repository;

import vat.home.card.service.VatCardCondition1KeywordVO;
import vat.home.card.service.VatCardCondition1VO;

@Repository("vatCardCondition1DAO")
public class VatCardCondition1DAO extends EgovAbstractMapper {
    @Resource(name = "egov.sqlSession")
    public void setSqlSessionFactory(SqlSessionFactory sqlSession) {
        super.setSqlSessionFactory(sqlSession);
    }

    public List<VatCardCondition1VO> selectCondition1List(VatCardCondition1VO vo) { return selectList("vatCardCondition1DAO.selectCondition1List", vo); }
    public int selectCondition1ListTotCnt(VatCardCondition1VO vo) { Integer n = selectOne("vatCardCondition1DAO.selectCondition1ListTotCnt", vo); return n == null ? 0 : n; }
    public VatCardCondition1VO selectCondition1(Long seq) { return selectOne("vatCardCondition1DAO.selectCondition1", seq); }
    public void insertCondition1(VatCardCondition1VO vo) { insert("vatCardCondition1DAO.insertCondition1", vo); }
    public void updateCondition1(VatCardCondition1VO vo) { update("vatCardCondition1DAO.updateCondition1", vo); }
    public void deleteCondition1(Long seq) { delete("vatCardCondition1DAO.deleteCondition1", seq); }
    public void upsertCondition1(VatCardCondition1VO vo) { insert("vatCardCondition1DAO.upsertCondition1", vo); }

    public List<VatCardCondition1KeywordVO> selectKeywordList(VatCardCondition1KeywordVO vo) { return selectList("vatCardCondition1DAO.selectKeywordList", vo); }
    public int selectKeywordListTotCnt(VatCardCondition1KeywordVO vo) { Integer n = selectOne("vatCardCondition1DAO.selectKeywordListTotCnt", vo); return n == null ? 0 : n; }
    public VatCardCondition1KeywordVO selectKeyword(Long seq) { return selectOne("vatCardCondition1DAO.selectKeyword", seq); }
    public void insertKeyword(VatCardCondition1KeywordVO vo) { insert("vatCardCondition1DAO.insertKeyword", vo); }
    public void updateKeyword(VatCardCondition1KeywordVO vo) { update("vatCardCondition1DAO.updateKeyword", vo); }
    public void deleteKeyword(Long seq) { delete("vatCardCondition1DAO.deleteKeyword", seq); }

    public VatCardCondition1VO selectMatch(String bizcnd, String induty) {
        Map<String, Object> p = new HashMap<String, Object>(); p.put("bizcnd", bizcnd); p.put("induty", induty);
        return selectOne("vatCardCondition1DAO.selectMatch", p);
    }
    public VatCardCondition1KeywordVO selectKeywordMatch(String keywordType, String targetType, String keyword) {
        Map<String, Object> p = new HashMap<String, Object>(); p.put("keywordType", keywordType); p.put("targetType", targetType); p.put("keyword", keyword);
        return selectOne("vatCardCondition1DAO.selectKeywordMatch", p);
    }
}
