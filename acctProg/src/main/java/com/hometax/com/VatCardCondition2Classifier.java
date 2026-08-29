package com.hometax.com;

/**
 * 사업용신용카드 일반 내려받기 2.0 - 조건2 전용 분류 클래스.
 *
 * 핵심 순서:
 *  1) prepare()에서 조건2부터 판정한다.
 *  2) 조건2만으로 종료 가능한 경우 즉시 종료한다.
 *  3) 조건2가 조건1 등록여부/값/키워드를 요구하는 경우에만
 *     호출측에서 조건1을 조회한다.
 *  4) finish()에서 조건2 우선순위를 유지하며 최종값을 만든다.
 */
public final class VatCardCondition2Classifier {

    private static final String BIZR_CORPORATE = "1";
    private static final String BIZR_PERSONAL = "2";

    private static final String EMPLOYEE_YES = "1";
    private static final String EMPLOYEE_NO = "2";

    private static final String VEHICLE_NONE = "1";
    private static final String VEHICLE_NON_DEDUCTIBLE = "2";
    private static final String VEHICLE_DEDUCTIBLE = "3";

    private VatCardCondition2Classifier() {
    }

    /** 기업회원 단위 조건값. */
    public static final class BusinessContext {
        private String bizrSeCode;
        private String emplSeCode;
        private String vhclSeCode;
        private Boolean condition1Registered;

        public String getBizrSeCode() {
            return bizrSeCode;
        }

        public BusinessContext setBizrSeCode(String bizrSeCode) {
            this.bizrSeCode = trim(bizrSeCode);
            return this;
        }

        public String getEmplSeCode() {
            return emplSeCode;
        }

        public BusinessContext setEmplSeCode(String emplSeCode) {
            this.emplSeCode = trim(emplSeCode);
            return this;
        }

        public String getVhclSeCode() {
            return vhclSeCode;
        }

        public BusinessContext setVhclSeCode(String vhclSeCode) {
            this.vhclSeCode = trim(vhclSeCode);
            return this;
        }

        public Boolean getCondition1Registered() {
            return condition1Registered;
        }

        public BusinessContext setCondition1Registered(Boolean condition1Registered) {
            this.condition1Registered = condition1Registered;
            return this;
        }
    }

    /** 홈택스 거래 1행에서 읽는 조건값. */
    public static final class RowContext {
        private String merchantType;
        private String merchantName;
        private double supplyAmount;

        // 조건1이 실제로 필요할 때만 사용한다.
        private String bizcnd;
        private String induty;
        private boolean bizcndColumnAvailable;
        private boolean indutyColumnAvailable;

        public String getMerchantType() {
            return merchantType;
        }

        public RowContext setMerchantType(String merchantType) {
            this.merchantType = merchantType;
            return this;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public RowContext setMerchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public double getSupplyAmount() {
            return supplyAmount;
        }

        public RowContext setSupplyAmount(double supplyAmount) {
            this.supplyAmount = supplyAmount;
            return this;
        }

        public String getBizcnd() {
            return bizcnd;
        }

        public RowContext setBizcnd(String bizcnd) {
            this.bizcnd = trim(bizcnd);
            return this;
        }

        public String getInduty() {
            return induty;
        }

        public RowContext setInduty(String induty) {
            this.induty = trim(induty);
            return this;
        }

        public boolean isBizcndColumnAvailable() {
            return bizcndColumnAvailable;
        }

        public RowContext setBizcndColumnAvailable(boolean bizcndColumnAvailable) {
            this.bizcndColumnAvailable = bizcndColumnAvailable;
            return this;
        }

        public boolean isIndutyColumnAvailable() {
            return indutyColumnAvailable;
        }

        public RowContext setIndutyColumnAvailable(boolean indutyColumnAvailable) {
            this.indutyColumnAvailable = indutyColumnAvailable;
            return this;
        }
    }

    /**
     * 조건2를 먼저 판정한 결과.
     * 이 단계에서는 조건1 DB를 조회하지 않는다.
     */
    public static final class Plan {
        private boolean terminal;
        private Result terminalResult;

        private boolean needCondition1Registration;
        private boolean corpKeyword;
        private boolean employeeKeyword;
        private boolean vehicleKeyword;

        private boolean vatDeductionOverrideSet;
        private String vatDeductionOverride;

        private boolean vatTypeOverrideSet;
        private Integer vatTypeOverride;

        private RowColor rowColor = RowColor.NONE;

        public boolean isTerminal() {
            return terminal;
        }

        public Result getTerminalResult() {
            return terminalResult;
        }

        public boolean isNeedCondition1Registration() {
            return needCondition1Registration;
        }

        public boolean isCorpKeyword() {
            return corpKeyword;
        }

        public boolean isEmployeeKeyword() {
            return employeeKeyword;
        }

        public boolean isVehicleKeyword() {
            return vehicleKeyword;
        }

        public RowColor getRowColor() {
            return rowColor;
        }
    }

    /** 조건1 조회 완료값. */
    public static final class Condition1Context {
        private boolean registered;
        private String vatDeduction;
        private Integer vatType;
        private Integer accountCode;
        private Integer corpKeywordAccount;
        private Integer employeeKeywordAccount;
        private Integer vehicleKeywordAccount;

        public boolean isRegistered() {
            return registered;
        }

        public Condition1Context setRegistered(boolean registered) {
            this.registered = registered;
            return this;
        }

        public String getVatDeduction() {
            return vatDeduction;
        }

        public Condition1Context setVatDeduction(String vatDeduction) {
            this.vatDeduction = vatDeduction;
            return this;
        }

        public Integer getVatType() {
            return vatType;
        }

        public Condition1Context setVatType(Integer vatType) {
            this.vatType = vatType;
            return this;
        }

        public Integer getAccountCode() {
            return accountCode;
        }

        public Condition1Context setAccountCode(Integer accountCode) {
            this.accountCode = accountCode;
            return this;
        }

        public Integer getCorpKeywordAccount() {
            return corpKeywordAccount;
        }

        public Condition1Context setCorpKeywordAccount(Integer corpKeywordAccount) {
            this.corpKeywordAccount = corpKeywordAccount;
            return this;
        }

        public Integer getEmployeeKeywordAccount() {
            return employeeKeywordAccount;
        }

        public Condition1Context setEmployeeKeywordAccount(Integer employeeKeywordAccount) {
            this.employeeKeywordAccount = employeeKeywordAccount;
            return this;
        }

        public Integer getVehicleKeywordAccount() {
            return vehicleKeywordAccount;
        }

        public Condition1Context setVehicleKeywordAccount(Integer vehicleKeywordAccount) {
            this.vehicleKeywordAccount = vehicleKeywordAccount;
            return this;
        }
    }

    /** null은 엑셀에서 빈 셀로 기록한다. */
    public static final class Result {
        private String vatDeduction;
        private Integer vatType;
        private Integer accountCode;
        private RowColor rowColor;

        public Result(String vatDeduction, Integer vatType, Integer accountCode) {
            this(vatDeduction, vatType, accountCode, RowColor.NONE);
        }

        public Result(
                String vatDeduction,
                Integer vatType,
                Integer accountCode,
                RowColor rowColor) {

            this.vatDeduction = vatDeduction;
            this.vatType = vatType;
            this.accountCode = accountCode;
            this.rowColor = rowColor == null ? RowColor.NONE : rowColor;
        }

        public String getVatDeduction() {
            return vatDeduction;
        }

        public Integer getVatType() {
            return vatType;
        }

        public Integer getAccountCode() {
            return accountCode;
        }

        public RowColor getRowColor() {
            return rowColor;
        }

        private void setVatDeduction(String vatDeduction) {
            this.vatDeduction = vatDeduction;
        }

        private void setVatType(Integer vatType) {
            this.vatType = vatType;
        }

        private void setAccountCode(Integer accountCode) {
            this.accountCode = accountCode;
        }

        private void setRowColor(RowColor rowColor) {
            this.rowColor = rowColor == null ? RowColor.NONE : rowColor;
        }
    }

    public enum RowColor {
        NONE,
        YELLOW,
        ORANGE,
        GREEN
    }

    /**
     * 1단계: 조건2부터 판정.
     * 조건1 DB 조회는 절대 하지 않는다.
     */
    public static Plan prepare(
            BusinessContext business,
            RowContext row) {

        if (business == null) {
            business = new BusinessContext();
        }

        if (row == null) {
            row = new RowContext();
        }

        validateBusinessContext(business);

        Plan plan = new Plan();

        // -----------------------------------------------------
        // 조건2 #1 사업자
        // 개인 = null / null / null, 노란색 -> 여기서 종료
        // -----------------------------------------------------
        if (BIZR_PERSONAL.equals(business.getBizrSeCode())) {
            plan.terminal = true;
            plan.terminalResult =
                    new Result(
                            null,
                            null,
                            null,
                            RowColor.YELLOW
                    );
            return plan;
        }

        // 법인 = 불공제 / null / (키워드 142, 그 외 조건1)
        plan.rowColor = RowColor.YELLOW;
        plan.vatDeductionOverrideSet = true;
        plan.vatDeductionOverride = "불공제";
        plan.vatTypeOverrideSet = true;
        plan.vatTypeOverride = null;
        plan.corpKeyword = true;

        // -----------------------------------------------------
        // 조건2 #2 상대(결제)업체
        // 간이/면세 = 불공제 / null / 조건1
        // 법인/일반 = 조건1
        //
        // 현재 기존 조건2 우선순위를 그대로 유지한다.
        // -----------------------------------------------------
        if (isSimplifiedOrExempt(row.getMerchantType())) {
            plan.vatDeductionOverrideSet = true;
            plan.vatDeductionOverride = "불공제";
            plan.vatTypeOverrideSet = true;
            plan.vatTypeOverride = null;
        }

        // -----------------------------------------------------
        // 조건2 #3 직원
        // 직원 O = 계정과목 일부만 키워드 811
        // -----------------------------------------------------
        if (EMPLOYEE_YES.equals(business.getEmplSeCode())) {
            plan.employeeKeyword = true;
        }

        // -----------------------------------------------------
        // 조건2 #4 차량
        // 불공차량 O = 계정과목 일부만 키워드 822
        // 공제차량 O = 공제 / 57 / 계정과목 일부만 키워드 822
        // -----------------------------------------------------
        if (VEHICLE_NON_DEDUCTIBLE.equals(business.getVhclSeCode())
                || VEHICLE_DEDUCTIBLE.equals(business.getVhclSeCode())) {
            plan.vehicleKeyword = true;
        }

        if (VEHICLE_DEDUCTIBLE.equals(business.getVhclSeCode())) {
            plan.vatDeductionOverrideSet = true;
            plan.vatDeductionOverride = "공제";
            plan.vatTypeOverrideSet = true;
            plan.vatTypeOverride = Integer.valueOf(57);
        }

        // -----------------------------------------------------
        // 조건2 #5 등록유무
        // 법인 ROW에서는 등록 O/X 확인이 필요하다.
        // 이 플래그를 확인한 뒤 호출측이 조건1을 조회한다.
        // -----------------------------------------------------
        plan.needCondition1Registration = true;

        // -----------------------------------------------------
        // 조건2 #6 금액기준
        // 100만원 초과 = 초록색.
        // 색은 뒤 조건이므로 앞 색보다 우선한다.
        // -----------------------------------------------------
        if (row.getSupplyAmount() > 1000000d) {
            plan.rowColor = RowColor.GREEN;
        }

        return plan;
    }

    /**
     * 2단계: 조건2 판정 후 필요한 조건1 조회까지 끝난 상태에서 최종값 생성.
     */
    public static Result finish(
            Plan plan,
            Condition1Context condition1) {

        if (plan == null) {
            throw new IllegalArgumentException(
                    "조건2 Plan이 없습니다."
            );
        }

        if (plan.isTerminal()) {
            return plan.getTerminalResult();
        }

        if (condition1 == null) {
            throw new IllegalArgumentException(
                    "조건2가 조건1 판정을 요구하지만 Condition1Context가 없습니다."
            );
        }

        // -----------------------------------------------------
        // 조건2 #5 등록 X
        // 공제 / 57 / 830, 주황색
        // 단 #6 100만원 초과 초록색이 최종 우선
        // -----------------------------------------------------
        if (!condition1.isRegistered()) {
            RowColor color =
                    plan.getRowColor() == RowColor.GREEN
                            ? RowColor.GREEN
                            : RowColor.ORANGE;

            return new Result(
                    "공제",
                    Integer.valueOf(57),
                    Integer.valueOf(830),
                    color
            );
        }

        // 등록 O이면 조건1 값에서 시작.
        Result result =
                new Result(
                        condition1.getVatDeduction(),
                        condition1.getVatType(),
                        condition1.getAccountCode(),
                        plan.getRowColor()
                );

        // 조건2 직접값은 조건1보다 우선.
        if (plan.vatDeductionOverrideSet) {
            result.setVatDeduction(
                    plan.vatDeductionOverride
            );
        }

        if (plan.vatTypeOverrideSet) {
            result.setVatType(
                    plan.vatTypeOverride
            );
        }

        // -----------------------------------------------------
        // 계정과목 키워드
        // 조건2 표 순서 #1 -> #3 -> #4 순으로 적용.
        // 뒤 조건이 일치하면 앞 키워드 결과를 덮어쓴다.
        // -----------------------------------------------------
        if (plan.isCorpKeyword()
                && condition1.getCorpKeywordAccount() != null) {
            result.setAccountCode(
                    condition1.getCorpKeywordAccount()
            );
        }

        if (plan.isEmployeeKeyword()
                && condition1.getEmployeeKeywordAccount() != null) {
            result.setAccountCode(
                    condition1.getEmployeeKeywordAccount()
            );
        }

        if (plan.isVehicleKeyword()
                && condition1.getVehicleKeywordAccount() != null) {
            result.setAccountCode(
                    condition1.getVehicleKeywordAccount()
            );
        }

        return result;
    }

    /**
     * 기존 호출 호환용.
     * 조건1이 연결되지 않은 코드에서 호출될 경우 기존 임시값을 유지한다.
     * 실제 일반 내려받기는 VatCardDownloadClassifier를 통해 prepare -> 조건1 -> finish 순서로 간다.
     */
    public static Result classify(
            BusinessContext business,
            RowContext row) {

        if (business == null) {
            business = new BusinessContext();
        }

        if (row == null) {
            row = new RowContext();
        }

        if (isEmpty(business.getBizrSeCode())
                && isEmpty(business.getEmplSeCode())
                && isEmpty(business.getVhclSeCode())) {
            return new Result(
                    "공제",
                    Integer.valueOf(57),
                    Integer.valueOf(830),
                    RowColor.NONE
            );
        }

        Plan plan = prepare(business, row);

        if (plan.isTerminal()) {
            return plan.getTerminalResult();
        }

        Condition1Context temporary =
                new Condition1Context()
                        .setRegistered(
                                !Boolean.FALSE.equals(
                                        business.getCondition1Registered()
                                )
                        )
                        .setVatDeduction("공제")
                        .setVatType(Integer.valueOf(57))
                        .setAccountCode(Integer.valueOf(830));

        return finish(plan, temporary);
    }

    private static void validateBusinessContext(
            BusinessContext business) {

        if (!BIZR_CORPORATE.equals(business.getBizrSeCode())
                && !BIZR_PERSONAL.equals(business.getBizrSeCode())) {
            throw new IllegalArgumentException(
                    "사업자구분 코드가 올바르지 않습니다. BIZR_SE_CODE="
                    + business.getBizrSeCode()
            );
        }

        if (!EMPLOYEE_YES.equals(business.getEmplSeCode())
                && !EMPLOYEE_NO.equals(business.getEmplSeCode())) {
            throw new IllegalArgumentException(
                    "직원여부 코드가 올바르지 않습니다. EMPL_SE_CODE="
                    + business.getEmplSeCode()
            );
        }

        if (!VEHICLE_NONE.equals(business.getVhclSeCode())
                && !VEHICLE_NON_DEDUCTIBLE.equals(business.getVhclSeCode())
                && !VEHICLE_DEDUCTIBLE.equals(business.getVhclSeCode())) {
            throw new IllegalArgumentException(
                    "차량구분 코드가 올바르지 않습니다. VHCL_SE_CODE="
                    + business.getVhclSeCode()
            );
        }
    }

    private static boolean isSimplifiedOrExempt(String merchantType) {
        String value = trim(merchantType).replace(" ", "");
        return value.contains("간이")
                || value.contains("면세");
    }

    private static boolean isEmpty(String value) {
        return trim(value).length() == 0;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
