<%
 /**
  * @Class Name : VatCardPurchaseList.jsp
  * @Description : 사업용신용카드 매입세액 공제 확인/변경 화면
  */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
<title>사업용신용카드 매입세액 공제 확인/변경</title>
<meta http-equiv="content-type"
      content="text/html; charset=utf-8">

<link type="text/css"
      rel="stylesheet"
      href="<c:url value='/css/egovframework/com/com.css' />">

<link type="text/css"
      rel="stylesheet"
      href="<c:url value='/css/vat/home/card/VatCardPurchase.css' />">

<style type="text/css">
/* VatCardPurchase 화면 전용 페이징 강제 보정 */
.vat_paging {
    position: relative !important;
    display: block !important;
    min-height: 32px !important;
    margin-top: 20px !important;
    text-align: center !important;
}

.vat_paging .pagination {
    display: inline-block !important;
    margin: 0 !important;
    padding: 0 !important;
    text-align: center !important;
}

/* ui:pagination이 어떤 중첩 구조를 만들더라도 bullet 제거 */
.vat_paging ul,
.vat_paging ol {
    display: inline-block !important;
    margin: 0 !important;
    padding: 0 !important;
    list-style: none !important;
    list-style-type: none !important;
}

.vat_paging ul li,
.vat_paging ol li,
.vat_paging li {
    display: inline-block !important;
    float: none !important;
    margin: 0 2px !important;
    padding: 0 !important;
    list-style: none !important;
    list-style-type: none !important;
    vertical-align: middle !important;
}

.vat_paging li::before,
.vat_paging li::after,
.vat_paging li::marker {
    content: none !important;
    display: none !important;
}

/* 일반 페이지 번호 */
.vat_paging a,
.vat_paging strong {
    display: inline-block !important;
    box-sizing: border-box !important;
    min-width: 28px !important;
    height: 28px !important;
    margin: 0 !important;
    padding: 0 7px !important;
    border: 1px solid #d9dee7 !important;
    background: #fff !important;
    color: #666 !important;
    line-height: 26px !important;
    text-align: center !important;
    text-decoration: none !important;
    vertical-align: middle !important;
}

/* 현재 페이지 */
.vat_paging li.current a,
.vat_paging li.current strong,
.vat_paging strong {
    border-color: #334155 !important;
    background: #334155 !important;
    color: #fff !important;
    font-weight: 600 !important;
}

.vat_paging a:hover {
    border-color: #334155 !important;
    background: #334155 !important;
    color: #fff !important;
}

/* image 타입 처음/이전/다음/마지막 */
.vat_paging li.first a,
.vat_paging li.prev a,
.vat_paging li.next a,
.vat_paging li.last a {
    width: 28px !important;
    padding: 0 !important;
    overflow: hidden !important;
    text-indent: -9999px !important;
    background-color: #fff !important;
    background-position: center center !important;
    background-repeat: no-repeat !important;
}

.vat_paging li.first a {
    background-image: url("<c:url value='/images/egovframework/com/cmm/paging/pagination_first.gif'/>") !important;
}
.vat_paging li.prev a {
    background-image: url("<c:url value='/images/egovframework/com/cmm/paging/pagination_prev.gif'/>") !important;
}
.vat_paging li.next a {
    background-image: url("<c:url value='/images/egovframework/com/cmm/paging/pagination_next.gif'/>") !important;
}
.vat_paging li.last a {
    background-image: url("<c:url value='/images/egovframework/com/cmm/paging/pagination_last.gif'/>") !important;
}

/* 조회된 기업회원 건수 옆 페이지당 조회건수 */
.vat_total_area {
    display: inline-flex;
    align-items: center;
    gap: 10px;
}

.vat_page_unit {
    height: 30px;
    min-width: 82px;
    padding: 0 8px;
    border: 1px solid #ccd3dd;
    background: #fff;
    color: #333;
    box-sizing: border-box;
    vertical-align: middle;
}

/* 홈택스 다운로드 처리중 전체화면 로딩 */
.vat_loading_layer {
    display: none;
    position: fixed;
    z-index: 99999;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
}

.vat_loading_dim {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.38);
}

.vat_loading_box {
    position: absolute;
    left: 50%;
    top: 50%;
    width: 360px;
    padding: 32px 28px;
    transform: translate(-50%, -50%);
    box-sizing: border-box;
    border-radius: 10px;
    background: #fff;
    text-align: center;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.25);
}

.vat_loading_spinner {
    width: 46px;
    height: 46px;
    margin: 0 auto 18px auto;
    border: 5px solid #e5e7eb;
    border-top-color: #334155;
    border-radius: 50%;
    animation: vat_loading_spin 0.8s linear infinite;
}

.vat_loading_title {
    margin-bottom: 8px;
    color: #222;
    font-size: 17px;
    font-weight: 700;
}

.vat_loading_message {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
}

@keyframes vat_loading_spin {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}


.vat_progress_area {
    margin-top: 18px;
}

.vat_progress_summary {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    color: #444;
    font-size: 14px;
    font-weight: 600;
}

.vat_progress_track {
    overflow: hidden;
    width: 100%;
    height: 10px;
    margin-bottom: 14px;
    border-radius: 6px;
    background: #e5e7eb;
}

.vat_progress_bar {
    width: 0;
    height: 100%;
    border-radius: 6px;
    background: #334155;
    transition: width 0.4s ease;
}

.vat_progress_detail {
    margin-top: 6px;
    color: #555;
    font-size: 13px;
    line-height: 1.7;
}

.vat_progress_company {
    overflow: hidden;
    max-width: 100%;
    text-overflow: ellipsis;
    white-space: nowrap;
}

</style>

<script type="text/javascript">

function fn_vat_init() {
    fn_vat_changePeriodType();
}

function fn_vat_changePeriodType() {

    var periodType = document.querySelector(
        'input[name="searchPeriodType"]:checked'
    );

    var dayArea =
        document.getElementById("dayArea");

    var monthArea =
        document.getElementById("monthArea");

    var quarterArea =
        document.getElementById("quarterArea");

    dayArea.style.display = "none";
    monthArea.style.display = "none";
    quarterArea.style.display = "none";

    if (!periodType) {
        return;
    }

    if (periodType.value === "DAY") {
        dayArea.style.display = "inline-flex";
    } else if (periodType.value === "MONTH") {
        monthArea.style.display = "inline-flex";
    } else {
        quarterArea.style.display = "inline-flex";
    }
}

function fn_vat_search() {

    var form =
        document.vatCardPurchaseForm;

    form.pageIndex.value = 1;

    form.action =
        "<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>";

    form.submit();
}

function fn_vat_selectPage(pageNo) {

    if (pageNo < 1) {
        return;
    }

    var form =
        document.vatCardPurchaseForm;

    form.pageIndex.value = pageNo;

    form.action =
        "<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>";

    form.submit();
}

function fn_vat_changePageUnit() {

    var form =
        document.vatCardPurchaseForm;

    form.pageIndex.value = 1;

    form.action =
        "<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>";

    form.submit();
}

function fn_vat_checkAll(checkAll) {

    var rowChecks =
        document.getElementsByName(
            "selectedBizrSeq"
        );

    for (var i = 0;
         i < rowChecks.length;
         i++) {

        rowChecks[i].checked =
            checkAll.checked;
    }
}

function fn_vat_checkRow() {

    var checkAll =
        document.getElementById(
            "checkAllBizr"
        );

    var rowChecks =
        document.getElementsByName(
            "selectedBizrSeq"
        );

    if (!checkAll) {
        return;
    }

    if (rowChecks.length === 0) {
        checkAll.checked = false;
        return;
    }

    for (var i = 0;
         i < rowChecks.length;
         i++) {

        if (!rowChecks[i].checked) {
            checkAll.checked = false;
            return;
        }
    }

    checkAll.checked = true;
}

/**
 * 선택 기업회원 홈택스 자료 내려받기.
 *
 * 브라우저에서는 COMTNENTRPRSBIZR.BIZR_SEQ만 서버로 보낸다.
 * 비밀번호는 hidden input으로 생성하지 않는다.
 */
function fn_vat_downloadSelected() {

    var checked =
        document.querySelectorAll(
            'input[name="selectedBizrSeq"]:checked'
        );

    if (checked.length === 0) {
        alert("내려받을 사업자등록번호를 선택해 주세요.");
        return;
    }

    var form =
        document.vatCardPurchaseForm;

    var jobId =
        fn_vat_createJobId();

    document.getElementById(
        "vatDownloadJobId"
    ).value = jobId;

    form.target =
        "vatDownloadFrame";

    form.action =
        "<c:url value='/vat/home/card/downloadSelectedEntrprsMber.do'/>";

    fn_vat_showLoading(
        "홈택스 자료를 내려받는 중입니다.",
        "선택한 사업자 자료를 처리하고 있습니다."
    );

    fn_vat_startProgressPolling(
        jobId
    );

    form.submit();
}

/**
 * 선택 기업회원 홈택스 자료 분류내려받기.
 *
 * 선택한 기업들의 홈택스 엑셀을 최종 XLS 한 파일로 합친다.
 */
function fn_vat_downloadMerged() {

    var checked =
        document.querySelectorAll(
            'input[name="selectedBizrSeq"]:checked'
        );

    if (checked.length === 0) {
        alert("분류내려받기 할 사업자등록번호를 선택해 주세요.");
        return;
    }

    var periodType = document.querySelector(
        'input[name="searchPeriodType"]:checked'
    );

    if (!periodType || periodType.value !== "QUARTER") {
        alert("분류내려받기는 현재 분기별 조회만 지원합니다.");
        return;
    }

    var form =
        document.vatCardPurchaseForm;

    var jobId =
        fn_vat_createJobId();

    document.getElementById(
        "vatDownloadJobId"
    ).value = jobId;

    form.target =
        "vatDownloadFrame";

    form.action =
        "<c:url value='/vat/home/card/downloadMergedEntrprsMber.do'/>";

    fn_vat_showLoading(
        "홈택스 자료를 분류내려받는 중입니다.",
        "선택한 사업자 자료를 순차 처리하고 있습니다."
    );

    fn_vat_startProgressPolling(
        jobId
    );

    form.submit();
}


var vatProgressTimer = null;

function fn_vat_createJobId() {

    return "VAT_"
        + new Date().getTime()
        + "_"
        + Math.floor(
            Math.random() * 1000000
        );
}

function fn_vat_showLoading(title, message) {

    var layer =
        document.getElementById(
            "vatLoadingLayer"
        );

    var titleElement =
        document.getElementById(
            "vatLoadingTitle"
        );

    var messageElement =
        document.getElementById(
            "vatLoadingMessage"
        );

    if (titleElement && title) {
        titleElement.innerHTML = title;
    }

    if (messageElement && message) {
        messageElement.innerHTML = message;
    }

    fn_vat_updateProgressView(
        0,
        0,
        0,
        "",
        -1
    );

    if (layer) {
        layer.style.display = "block";
    }

    var buttons =
        document.querySelectorAll(
            ".vat_btn_download"
        );

    for (var i = 0;
         i < buttons.length;
         i++) {

        buttons[i].disabled = true;
    }
}

function fn_vat_hideLoading() {

    var layer =
        document.getElementById(
            "vatLoadingLayer"
        );

    if (layer) {
        layer.style.display = "none";
    }

    var buttons =
        document.querySelectorAll(
            ".vat_btn_download"
        );

    for (var i = 0;
         i < buttons.length;
         i++) {

        buttons[i].disabled = false;
    }
}

function fn_vat_startProgressPolling(jobId) {

    fn_vat_stopProgressPolling();

    fn_vat_loadProgress(jobId);

    vatProgressTimer =
        window.setInterval(
            function() {
                fn_vat_loadProgress(jobId);
            },
            1000
        );
}

function fn_vat_stopProgressPolling() {

    if (vatProgressTimer != null) {

        window.clearInterval(
            vatProgressTimer
        );

        vatProgressTimer = null;
    }
}

function fn_vat_loadProgress(jobId) {

    if (!jobId) {
        return;
    }

    var xhr =
        new XMLHttpRequest();

    xhr.open(
        "GET",
        "<c:url value='/vat/home/card/downloadProgress.do'/>"
        + "?jobId="
        + encodeURIComponent(jobId)
        + "&_="
        + new Date().getTime(),
        true
    );

    xhr.onreadystatechange =
        function() {

            if (xhr.readyState !== 4
                    || xhr.status !== 200) {

                return;
            }

            try {

                var data =
                    JSON.parse(
                        xhr.responseText
                    );

                if (!data
                        || data.exists !== true) {

                    return;
                }

                fn_vat_updateProgressView(
                    data.completedCount || 0,
                    data.totalCount || 0,
                    data.percent || 0,
                    data.currentCompany || "",
                    typeof data.remainSeconds === "number"
                        ? data.remainSeconds
                        : -1
                );

            } catch (e) {
            }
        };

    xhr.send(null);
}

function fn_vat_updateProgressView(
        completed,
        total,
        percent,
        currentCompany,
        remainSeconds) {

    var countElement =
        document.getElementById(
            "vatProgressCount"
        );

    var percentElement =
        document.getElementById(
            "vatProgressPercent"
        );

    var barElement =
        document.getElementById(
            "vatProgressBar"
        );

    var companyElement =
        document.getElementById(
            "vatProgressCompany"
        );

    var remainElement =
        document.getElementById(
            "vatProgressRemain"
        );

    if (countElement) {

        countElement.innerHTML =
            completed
            + " / "
            + total
            + " 업체";
    }

    if (percentElement) {

        percentElement.innerHTML =
            percent
            + "%";
    }

    if (barElement) {

        barElement.style.width =
            percent
            + "%";
    }

    if (companyElement) {

        companyElement.innerHTML =
            currentCompany
            ? fn_vat_escapeHtml(currentCompany)
            : "처리 대기중";
    }

    if (remainElement) {

        if (remainSeconds == null
                || remainSeconds < 0) {

            remainElement.innerHTML =
                "예상 잔여시간 계산중";

        } else {

            remainElement.innerHTML =
                "예상 잔여시간 "
                + fn_vat_formatRemainTime(
                    remainSeconds
                );
        }
    }
}

function fn_vat_formatRemainTime(seconds) {

    seconds =
        Math.max(
            0,
            parseInt(seconds, 10) || 0
        );

    var hour =
        Math.floor(
            seconds / 3600
        );

    var minute =
        Math.floor(
            (seconds % 3600) / 60
        );

    var second =
        seconds % 60;

    if (hour > 0) {

        return "약 "
            + hour
            + "시간 "
            + minute
            + "분";
    }

    if (minute > 0) {

        return "약 "
            + minute
            + "분 "
            + second
            + "초";
    }

    return "약 "
        + second
        + "초";
}

function fn_vat_escapeHtml(value) {

    if (value == null) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

/**
 * 숨김 iframe의 다운로드 처리 응답이 호출한다.
 *
 * 중요:
 * Controller에서 목록 조회를 먼저 실행하지 않는다.
 * 이 함수에서 처리완료 팝업의 확인을 누른 뒤에만
 * selectVatCardPurchaseList.do를 호출한다.
 */
function fn_vat_downloadFinished(
        status,
        message) {

    fn_vat_stopProgressPolling();

    var form =
        document.vatCardPurchaseForm;

    // 다운로드 요청에 사용했던 iframe target 해제.
    form.target = "_self";

    if (status === "SUCCESS") {

        fn_vat_updateProgressView(
            1,
            1,
            100,
            "처리 완료",
            0
        );

        fn_vat_hideLoading();

        // alert는 사용자가 [확인]을 누를 때까지
        // 아래 목록 조회 submit이 실행되지 않는다.
        alert(
            message
            ? message
            : "처리완료되었습니다."
        );

        form.pageIndex.value = 1;

        form.action =
            "<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>";

        form.submit();

        return;
    }

    fn_vat_hideLoading();

    alert(
        message
        ? message
        : "처리 중 오류가 발생했습니다."
    );
}


function fn_vat_excelDownload() {
    return false;
}

</script>
</head>

<body onload="fn_vat_init();">

<form name="vatCardPurchaseForm"
      action="<c:url value='/vat/home/card/selectVatCardPurchaseList.do'/>"
      method="post">

<input type="hidden"
       name="jobId"
       id="vatDownloadJobId"
       value="">



<div class="vat_card_wrap">

    <h1>사업용신용카드 매입세액 공제 확인/변경</h1>

    <div class="vat_search_box">

        <div class="vat_search_row">

            <div class="vat_search_title">
                <span class="vat_required">*</span>
                조회기간
            </div>

            <div class="vat_search_content">

                <label>
                    <input type="radio"
                           name="searchPeriodType"
                           value="DAY"
                           onclick="fn_vat_changePeriodType();"
                           <c:if test="${searchVO.searchPeriodType == 'DAY'}">checked="checked"</c:if>>
                    일별
                </label>

                <label>
                    <input type="radio"
                           name="searchPeriodType"
                           value="MONTH"
                           onclick="fn_vat_changePeriodType();"
                           <c:if test="${searchVO.searchPeriodType == 'MONTH'}">checked="checked"</c:if>>
                    월별
                </label>

                <label>
                    <input type="radio"
                           name="searchPeriodType"
                           value="QUARTER"
                           onclick="fn_vat_changePeriodType();"
                           <c:if test="${searchVO.searchPeriodType == 'QUARTER'}">checked="checked"</c:if>>
                    분기별
                </label>

                <span id="dayArea"
                      class="vat_period_area">

                    <input type="date"
                           name="searchDate"
                           value="<c:out value='${searchVO.searchDate}'/>"
                           title="조회일자">

                </span>

                <span id="monthArea"
                      class="vat_period_area">

                    <input type="month"
                           name="searchMonth"
                           value="<c:out value='${searchVO.searchMonth}'/>"
                           title="조회년월">

                </span>

                <span id="quarterArea"
                      class="vat_period_area">

                    <select name="searchYear"
                            title="년도 선택">

                        <c:forEach items="${yearList}"
                                   var="year">

                            <option value="${year}"
                                <c:if test="${searchVO.searchYear == year}">selected="selected"</c:if>>
                                ${year}년
                            </option>

                        </c:forEach>

                    </select>

                    <select name="searchQuarter"
                            title="분기 선택">

                        <option value="1"
                            <c:if test="${searchVO.searchQuarter == '1'}">selected="selected"</c:if>>
                            1분기
                        </option>

                        <option value="2"
                            <c:if test="${searchVO.searchQuarter == '2'}">selected="selected"</c:if>>
                            2분기
                        </option>

                        <option value="3"
                            <c:if test="${searchVO.searchQuarter == '3'}">selected="selected"</c:if>>
                            3분기
                        </option>

                        <option value="4"
                            <c:if test="${searchVO.searchQuarter == '4'}">selected="selected"</c:if>>
                            4분기
                        </option>

                    </select>

                </span>

            </div>

        </div>

        <div class="vat_search_row">

            <div class="vat_search_title">
                공제여부
            </div>

            <div class="vat_search_content">

                <select name="deductionType"
                        title="공제여부 선택">

                    <option value="ALL"
                        <c:if test="${searchVO.deductionType == 'ALL'}">selected="selected"</c:if>>
                        -전체-
                    </option>

                    <option value="Y"
                        <c:if test="${searchVO.deductionType == 'Y'}">selected="selected"</c:if>>
                        공제대상
                    </option>

                    <option value="N"
                        <c:if test="${searchVO.deductionType == 'N'}">selected="selected"</c:if>>
                        불공제대상
                    </option>

                </select>

            </div>

        </div>

        <div class="vat_search_row">

            <div class="vat_search_title">
                사업자등록번호
            </div>

            <div class="vat_search_content">

                <input type="text"
                       class="input2"
                       name="searchBusinessNo"
                       value="<c:out value='${searchVO.searchBusinessNo}'/>"
                       title="사업자등록번호">

            </div>

        </div>

        <div class="vat_search_row">

            <div class="vat_search_title">
                상호
            </div>

            <div class="vat_search_content">

                <input type="text"
                       class="input2"
                       name="searchBusinessName"
                       value="<c:out value='${searchVO.searchBusinessName}'/>"
                       title="상호">

            </div>

            <div class="vat_search_button_area">

                <input type="button"
                       class="vat_btn vat_btn_search"
                       value="조회"
                       onclick="fn_vat_search();">

            </div>

        </div>

    </div>

    <div class="vat_result_top">

        <div class="vat_total_area">
            <div class="vat_total_amount">
                조회된 기업회원 :
                <strong>
                    <fmt:formatNumber
                        value="${totalCount}"
                        pattern="#,##0" />
                </strong>
                건
            </div>

            <select name="pageUnit"
                    class="vat_page_unit"
                    title="페이지당 조회 건수"
                    onchange="fn_vat_changePageUnit();">
                <option value="10"
                    <c:if test="${searchVO.pageUnit == 10}">selected="selected"</c:if>>10개</option>
                <option value="30"
                    <c:if test="${searchVO.pageUnit == 30}">selected="selected"</c:if>>30개</option>
                <option value="50"
                    <c:if test="${searchVO.pageUnit == 50}">selected="selected"</c:if>>50개</option>
                <option value="100"
                    <c:if test="${searchVO.pageUnit == 100}">selected="selected"</c:if>>100개</option>
                <option value="200"
                    <c:if test="${searchVO.pageUnit == 200}">selected="selected"</c:if>>200개</option>
                <option value="0"
                    <c:if test="${searchVO.pageUnit == 0}">selected="selected"</c:if>>ALL</option>
            </select>
        </div>

        <div>
            <input type="button"
                   class="vat_btn vat_btn_download"
                   value="내려받기"
                   onclick="fn_vat_downloadSelected();">

            <input type="button"
                   class="vat_btn vat_btn_download"
                   value="분류내려받기"
                   onclick="fn_vat_downloadMerged();">
        </div>

    </div>

    <div class="vat_grid_area">

        <table class="vat_grid">

            <caption>기업회원 선택 목록</caption>

            <colgroup>
                <col style="width:60px;">
                <col style="width:180px;">
                <col style="width:260px;">
                <col style="width:220px;">
                <col style="width:260px;">
            </colgroup>

            <thead>
            <tr>
                <th scope="col">
                    <input type="checkbox"
                           id="checkAllBizr"
                           title="전체 선택"
                           onclick="fn_vat_checkAll(this);">
                </th>
                <th scope="col">사업자등록번호</th>
                <th scope="col">상호명</th>
                <th scope="col">아이디</th>
                <th scope="col">
                    신청자 주민등록번호 2번째 값
                </th>
            </tr>
            </thead>

            <tbody>

            <c:if test="${fn:length(resultList) == 0}">

                <tr>
                    <td colspan="5"
                        class="vat_no_data">
                        조회된 결과가 없습니다.
                    </td>
                </tr>

            </c:if>

            <c:forEach items="${resultList}"
                       var="resultInfo">

                <tr>

                    <td>
                        <!--
                            안전한 방식:
                            화면에는 사업자번호 PK(BIZR_SEQ)만 전달한다.
                            ENTRPRS_MBER_PASSWORD hidden 없음.
                        -->
                        <input type="checkbox"
                               name="selectedBizrSeq"
                               value="<c:out value='${resultInfo.bizrSeq}'/>"
                               title="행 선택"
                               onclick="fn_vat_checkRow();">
                    </td>

                    <td>
                        <c:out value="${resultInfo.bizrno}" />
                    </td>

                    <td class="vat_align_left">
                        <c:out value="${resultInfo.cmpnyNm}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.entrprsmberId}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.applcntIhidnum2}" />
                    </td>

                </tr>

            </c:forEach>

            </tbody>

        </table>

    </div>

    <div class="vat_paging">

        <div class="pagination">
            <ui:pagination
                paginationInfo="${paginationInfo}"
                type="image"
                jsFunction="fn_vat_selectPage" />
        </div>

    </div>

</div>

<input type="hidden"
       name="pageIndex"
       value="<c:out value='${searchVO.pageIndex}'/>">

</form>

<div id="vatLoadingLayer"
     class="vat_loading_layer"
     aria-live="polite"
     aria-busy="true">

    <div class="vat_loading_dim"></div>

    <div class="vat_loading_box">
        <div class="vat_loading_spinner"></div>

        <div id="vatLoadingTitle"
             class="vat_loading_title">
            처리중입니다.
        </div>

        <div id="vatLoadingMessage"
             class="vat_loading_message">
            잠시만 기다려 주세요.
        </div>

        <div class="vat_progress_area">

            <div class="vat_progress_summary">
                <span id="vatProgressCount">0 / 0 업체</span>
                <span id="vatProgressPercent">0%</span>
            </div>

            <div class="vat_progress_track">
                <div id="vatProgressBar"
                     class="vat_progress_bar"></div>
            </div>

            <div class="vat_progress_detail">
                <div id="vatProgressCompany"
                     class="vat_progress_company">
                    처리 대기중
                </div>

                <div id="vatProgressRemain">
                    예상 잔여시간 계산중
                </div>
            </div>

        </div>
    </div>

</div>

<iframe name="vatDownloadFrame"
        id="vatDownloadFrame"
        title="홈택스 다운로드 처리"
        style="display:none;"></iframe>

</body>
</html>