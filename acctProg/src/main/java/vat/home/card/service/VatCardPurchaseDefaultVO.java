package vat.home.card.service;

import java.io.Serializable;

/**
 * 사업용신용카드 매입세액 공제 확인/변경 검색조건 VO
 */
public class VatCardPurchaseDefaultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 조회기간 구분 : DAY / MONTH / QUARTER */
    private String searchPeriodType = "QUARTER";

    /** 일별 조회일자 */
    private String searchDate = "";

    /** 월별 조회년월 */
    private String searchMonth = "";

    /** 분기별 조회연도 */
    private String searchYear = "";

    /** 분기 */
    private String searchQuarter = "1";

    /** 공제여부 : ALL / Y / N */
    private String deductionType = "ALL";

    /** 현재페이지 */
    private int pageIndex = 1;

    /** 페이지당 건수 */
    private int pageUnit = 10;

    /** 페이지 사이즈 */
    private int pageSize = 10;

    /** 시작 인덱스 */
    private int firstIndex = 0;

    /** 마지막 인덱스 */
    private int lastIndex = 0;

    /** 페이지당 조회 건수 */
    private int recordCountPerPage = 10;

    public String getSearchPeriodType() {
        return searchPeriodType;
    }

    public void setSearchPeriodType(String searchPeriodType) {
        this.searchPeriodType = searchPeriodType;
    }

    public String getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(String searchDate) {
        this.searchDate = searchDate;
    }

    public String getSearchMonth() {
        return searchMonth;
    }

    public void setSearchMonth(String searchMonth) {
        this.searchMonth = searchMonth;
    }

    public String getSearchYear() {
        return searchYear;
    }

    public void setSearchYear(String searchYear) {
        this.searchYear = searchYear;
    }

    public String getSearchQuarter() {
        return searchQuarter;
    }

    public void setSearchQuarter(String searchQuarter) {
        this.searchQuarter = searchQuarter;
    }

    public String getDeductionType() {
        return deductionType;
    }

    public void setDeductionType(String deductionType) {
        this.deductionType = deductionType;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageUnit() {
        return pageUnit;
    }

    public void setPageUnit(int pageUnit) {
        this.pageUnit = pageUnit;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }

    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage;
    }
}
