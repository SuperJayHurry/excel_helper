<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="教师面板"/>
<%@ include file="/WEB-INF/jsp/fragments/header.jsp" %>

<section class="card">
    <h2>我的任务</h2>
    <table>
        <thead>
        <tr>
            <th>任务</th>
            <th>截止日期</th>
            <th>模板</th>
            <th>状态</th>
            <th>提交</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${assignments}" var="assignment">
            <tr>
                <td>${assignment.templateTask.name}</td>
                <td>${assignment.templateTask.deadline}</td>
                <td><a href="/files?path=${assignment.templateTask.templateFilePath}">下载</a></td>
                <td class="status ${assignment.status}">${assignment.status}</td>
                <td>
                    <form method="post" action="/teacher/templates/${assignment.templateTask.id}/submit"
                          enctype="multipart/form-data">
                        <input type="file" name="submissionFile" accept=".xls,.xlsx" required>
                        <button type="submit">上传</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</section>

</main>
</body>
</html>

