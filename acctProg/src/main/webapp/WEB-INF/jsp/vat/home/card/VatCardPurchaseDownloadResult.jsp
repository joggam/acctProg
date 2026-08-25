<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>다운로드 처리 결과</title>
</head>
<body>

<script type="text/javascript">
(function() {

    var status =
        "<c:out value='${downloadStatus}'/>";

    var message =
        "<c:out value='${downloadMessage}'/>";

    if (status === "CANCELLED"
            && (!message || message.length === 0)) {
        message = "처리가 취소되었습니다.";
    }

    if (window.parent
            && typeof window.parent.fn_vat_downloadFinished === "function") {

        window.parent.fn_vat_downloadFinished(
            status,
            message
        );
    }
})();
</script>

</body>
</html>
