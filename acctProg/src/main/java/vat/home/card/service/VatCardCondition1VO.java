package vat.home.card.service;

import java.io.Serializable;

public class VatCardCondition1VO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long condition1Seq;
    private String bizcnd;
    private String induty;
    private String vatDeductYn;
    private String vatTypeCode;
    private String accountCode;
    private String useAt = "Y";
    private String searchKeyword;
    private int pageIndex = 1;
    private int pageUnit = 20;
    private int pageSize = 10;
    private int firstIndex;
    private int recordCountPerPage;

    public Long getCondition1Seq() { return condition1Seq; }
    public void setCondition1Seq(Long condition1Seq) { this.condition1Seq = condition1Seq; }
    public String getBizcnd() { return bizcnd; }
    public void setBizcnd(String bizcnd) { this.bizcnd = bizcnd; }
    public String getInduty() { return induty; }
    public void setInduty(String induty) { this.induty = induty; }
    public String getVatDeductYn() { return vatDeductYn; }
    public void setVatDeductYn(String vatDeductYn) { this.vatDeductYn = vatDeductYn; }
    public String getVatTypeCode() { return vatTypeCode; }
    public void setVatTypeCode(String vatTypeCode) { this.vatTypeCode = vatTypeCode; }
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
