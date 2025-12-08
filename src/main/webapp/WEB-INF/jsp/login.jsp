<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>登录 - 科研数据汇总助手</title>
    <link rel="stylesheet" href="<c:url value='/css/main.css'/>">
</head>
<body class="login-page">
<div class="login-card">
    <h1>科研秘书登录</h1>
    <c:if test="${not empty param.error or not empty sessionScope['SPRING_SECURITY_LAST_EXCEPTION']}">
        <div class="alert alert-danger">
            <c:choose>
                <c:when test="${not empty sessionScope['SPRING_SECURITY_LAST_EXCEPTION']}">
                    ${sessionScope['SPRING_SECURITY_LAST_EXCEPTION'].message}
                </c:when>
                <c:otherwise>账号或密码错误</c:otherwise>
            </c:choose>
        </div>
        <c:remove var="SPRING_SECURITY_LAST_EXCEPTION" scope="session"/>
    </c:if>
    <c:if test="${not empty param.logout}">
        <div class="alert alert-success">已退出登录</div>
    </c:if>
    <form method="post" action="/login" class="login-form">
        <div class="input-row">
            <label for="username">用户名</label>
            <input id="username" type="text" name="username" required>
        </div>
        <div class="input-row">
            <label for="password">密码</label>
            <input id="password" type="password" name="password" required>
        </div>
        <button type="submit">登录</button>
    </form>
</div>
</body>
</html>
