package vat.home.card.service;

/**
 * 사업용신용카드 매입세액 공제 확인/변경 VO
 */
public class VatCardPurchaseVO extends VatCardPurchaseDefaultVO {

    private static final long serialVersionUID = 1L;

    /** 승인일자 */
    private String approvalDate;

    /** 가맹점 사업자번호 */
    private String merchantBusinessNo;

    /** 가맹점명 */
    private String merchantName;

    /** 공급가액 */
    private Long supplyAmount;

    /** 세액 */
    private Long taxAmount;

    /** 비과세 */
    private Long taxFreeAmount;

    /** 합계 */
    private Long totalAmount;

    /** 가맹점유형 */
    private String merchantType;

    /** 업태 */
    private String businessStatus;

    /** 업종 */
    private String businessType;

    /** 공제여부 결정 */
    private String deductionYn;

    /** 비고 */
    private String remark;

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getMerchantBusinessNo() {
        return merchantBusinessNo;
    }

    public void setMerchantBusinessNo(String merchantBusinessNo) {
        this.merchantBusinessNo = merchantBusinessNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Long getSupplyAmount() {
        return supplyAmount;
    }

    public void setSupplyAmount(Long supplyAmount) {
        this.supplyAmount = supplyAmount;
    }

    public Long getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Long taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Long getTaxFreeAmount() {
        return taxFreeAmount;
    }

    public void setTaxFreeAmount(Long taxFreeAmount) {
        this.taxFreeAmount = taxFreeAmount;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getDeductionYn() {
        return deductionYn;
    }

    public void setDeductionYn(String deductionYn) {
        this.deductionYn = deductionYn;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
