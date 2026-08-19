<%
 /**
  * @Class Name : VatCardPurchaseList.jsp
  * @Description : 사업용신용카드 매입세액 공제 확인/변경 화면
  */
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

function fn_vat_checkAll(checkAll) {

    var rowChecks =
        document.getElementsByName(
            "selectedEntrprsMber"
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
            "checkAllEntrprsMber"
        );

    var rowChecks =
        document.getElementsByName(
            "selectedEntrprsMber"
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
 * 선택 기업회원 처리.
 *
 * 브라우저에서는 ENTRPRS_MBER_ID만 서버로 보낸다.
 * 비밀번호는 hidden input으로 생성하지 않는다.
 */
function fn_vat_processSelected() {

    var checked =
        document.querySelectorAll(
            'input[name="selectedEntrprsMber"]:checked'
        );

    if (checked.length === 0) {
        alert("처리할 기업회원을 선택해 주세요.");
        return;
    }

    var form =
        document.vatCardPurchaseForm;

    form.action =
        "<c:url value='/vat/home/card/processSelectedEntrprsMber.do'/>";

    form.submit();
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

        <div class="vat_total_amount">
            조회된 기업회원 :
            <strong>
                <fmt:formatNumber
                    value="${totalCount}"
                    pattern="#,##0" />
            </strong>
            건
        </div>

        <div>
            <input type="button"
                   class="vat_btn vat_btn_download"
                   value="선택처리"
                   onclick="fn_vat_processSelected();">
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
                           id="checkAllEntrprsMber"
                           title="전체 선택"
                           onclick="fn_vat_checkAll(this);">
                </th>
                <th scope="col">상호명</th>
                <th scope="col">아이디</th>
                <th scope="col">사업자등록번호</th>
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
                            화면에는 기업회원 ID만 전달한다.
                            ENTRPRS_MBER_PASSWORD hidden 없음.
                        -->
                        <input type="checkbox"
                               name="selectedEntrprsMber"
                               value="<c:out value='${resultInfo.entrprsmberId}'/>"
                               title="행 선택"
                               onclick="fn_vat_checkRow();">
                    </td>


                    <td class="vat_align_left">
                        <c:out value="${resultInfo.cmpnyNm}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.entrprsmberId}" />
                    </td>

                    <td>
                        <c:out value="${resultInfo.bizrno}" />
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

    <div class="vat_page_list">

        <!-- 이전 -->
        <c:if test="${searchVO.pageIndex > 1}">
            <a href="javascript:void(0);"
               class="vat_page_move"
               onclick="fn_vat_selectPage(${searchVO.pageIndex - 1});">
                이전
            </a>
        </c:if>

        <!-- 페이지 번호 -->
        <c:forEach var="pageNo"
                   begin="1"
                   end="${totalPageCount}">

            <c:choose>

                <c:when test="${pageNo == searchVO.pageIndex}">
                    <strong class="vat_page_current">
                        <c:out value="${pageNo}" />
                    </strong>
                </c:when>

                <c:otherwise>
                    <a href="javascript:void(0);"
                       class="vat_page_number"
                       onclick="fn_vat_selectPage(${pageNo});">
                        <c:out value="${pageNo}" />
                    </a>
                </c:otherwise>

            </c:choose>

        </c:forEach>

        <!-- 다음 -->
        <c:if test="${searchVO.pageIndex < totalPageCount}">
            <a href="javascript:void(0);"
               class="vat_page_move"
               onclick="fn_vat_selectPage(${searchVO.pageIndex + 1});">
                다음
            </a>
        </c:if>

    </div>

    <span class="vat_page_count">
        총 <strong><c:out value="${totalCount}" /></strong>건
    </span>

</div>

</div>

<input type="hidden"
       name="pageIndex"
       value="<c:out value='${searchVO.pageIndex}'/>">

</form>

</body>
</html>