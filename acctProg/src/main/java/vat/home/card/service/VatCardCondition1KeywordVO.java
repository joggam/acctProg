package vat.home.card.service;

import java.io.Serializable;

public class VatCardCondition1KeywordVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long keywordSeq;
    private String keywordType;
    private String targetType;
    private String keyword;
    private String accountCode;
    private String useAt = "Y";
    private String searchKeyword;
    private int pageIndex = 1;
    private int pageUnit = 20;
    private int pageSize = 10;
    private int firstIndex;
    private int recordCountPerPage;

    public Long getKeywordSeq() { return keywordSeq; }
    public void setKeywordSeq(Long keywordSeq) { this.keywordSeq = keywordSeq; }
    public String getKeywordType() { return keywordType; }
    public void setKeywordType(String keywordType) { this.keywordType = keywordType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getUseAt() { return useAt; }
    public void setUseAt(String useAt) { this.useAt = useAt; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }
    public int getPageUnit() { return pageUnit; }
    public void setPageUnit(int pageUnit) { this.pageUnit = pageUnit; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public int getFirstIndex() { return firstIndex; }
    public void setFirstIndex(int firstIndex) { this.firstIndex = firstIndex; }
    public int getRecordCountPerPage() { return recordCountPerPage; }
    public void setRecordCountPerPage(int recordCountPerPage) { this.recordCountPerPage = recordCountPerPage; }
}
