<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="任务详情"/>
<%@ include file="/WEB-INF/jsp/fragments/header.jsp" %>

<section class="card">
    <h2>${task.name}</h2>
    <p>${task.description}</p>
    <p>截止日期：${task.deadline} | 模板：
        <a href="/files?path=${task.templateFilePath}">${task.templateFileName}</a>
    </p>
    <div class="actions">
        <form method="post" action="/admin/templates/${task.id}/remind">
            <input type="text" name="message" placeholder="提醒内容" value="请尽快提交《${task.name}》">
            <button type="submit">一键催促未提交老师</button>
        </form>
        <form method="post" action="/admin/templates/${task.id}/aggregate">
            <button type="submit">生成最新汇总</button>
        </form>
    </div>
</section>

<section class="card">
    <h3>教师反馈进度</h3>
    <table>
        <thead>
        <tr>
            <th>教师</th>
            <th>系别</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>最后提醒</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${recipients}" var="recipient">
            <tr>
                <td>${recipient.recipient.fullName}</td>
                <td>${recipient.recipient.department.displayName}</td>
                <td>${recipient.recipient.email}</td>
                <td class="status ${recipient.status}">${recipient.status}</td>
                <td><c:out value="${recipient.lastReminderAt}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</section>

<section class="card">
    <h3>附件与汇总</h3>
    <table>
        <thead>
        <tr>
            <th>教师</th>
            <th>上传时间</th>
            <th>数据行数</th>
            <th>附件</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${submissions}" var="submission">
            <tr>
                <td>${submission.submitter.fullName}</td>
                <td>${submission.submittedAt}</td>
                <td>${submission.totalRows}</td>
                <td><a href="/files?path=${submission.filePath}">${submission.fileName}</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <c:if test="${not empty aggregation}">
        <div class="aggregation">
            <p>最近一次汇总：${aggregation.generatedAt} 行数：${aggregation.totalRows}</p>
            <a class="btn" href="/files?path=${aggregation.filePath}">下载汇总表</a>
        </div>
    </c:if>
</section>

</main>
</body>
</html>

