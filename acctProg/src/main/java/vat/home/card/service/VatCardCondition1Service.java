package vat.home.card.service;

import java.util.List;

public interface VatCardCondition1Service {
    List<VatCardCondition1VO> selectCondition1List(VatCardCondition1VO vo) throws Exception;
    int selectCondition1ListTotCnt(VatCardCondition1VO vo) throws Exception;
    VatCardCondition1VO selectCondition1(Long condition1Seq) throws Exception;
    void insertCondition1(VatCardCondition1VO vo) throws Exception;
    void updateCondition1(VatCardCondition1VO vo) throws Exception;
    void deleteCondition1(Long condition1Seq) throws Exception;
    void saveCondition1Excel(VatCardCondition1VO vo) throws Exception;

    List<VatCardCondition1KeywordVO> selectKeywordList(VatCardCondition1KeywordVO vo) throws Exception;
    int selectKeywordListTotCnt(VatCardCondition1KeywordVO vo) throws Exception;
    VatCardCondition1KeywordVO selectKeyword(Long keywordSeq) throws Exception;
    void insertKeyword(VatCardCondition1KeywordVO vo) throws Exception;
    void updateKeyword(VatCardCondition1KeywordVO vo) throws Exception;
    void deleteKeyword(Long keywordSeq) throws Exception;
    void saveKeywordExcel(VatCardCondition1KeywordVO vo) throws Exception;

    VatCardCondition1VO selectMatch(String bizcnd, String induty) throws Exception;
    VatCardCondition1KeywordVO selectKeywordMatch(String keywordType, String targetType, String keyword) throws Exception;
}
